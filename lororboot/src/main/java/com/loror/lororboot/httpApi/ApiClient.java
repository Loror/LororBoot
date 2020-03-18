package com.loror.lororboot.httpApi;

import android.support.annotation.Nullable;

import com.loror.lororUtil.http.AsyncClient;
import com.loror.lororUtil.http.DefaultAsyncClient;
import com.loror.lororUtil.http.HttpClient;
import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.http.Responce;
import com.loror.lororboot.annotation.BaseUrl;
import com.loror.lororboot.annotation.DELETE;
import com.loror.lororboot.annotation.GET;
import com.loror.lororboot.annotation.KeepStream;
import com.loror.lororboot.annotation.MOCK;
import com.loror.lororboot.annotation.POST;
import com.loror.lororboot.annotation.PUT;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

public class ApiClient {

    private boolean mockEnable = true;
    private String baseUrl;

    protected static JsonParser jsonParser;
    private OnRequestListener onRequestListener;
    private CodeFilter codeFilter;
    private Charset charset;

    /**
     * 设置是否开启mock功能
     */
    public ApiClient setMockEnable(boolean mockEnable) {
        this.mockEnable = mockEnable;
        return this;
    }

    /**
     * 设置baseUrl
     */
    public ApiClient setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public ApiClient setOnRequestListener(OnRequestListener onRequestListener) {
        this.onRequestListener = onRequestListener;
        return this;
    }

    public ApiClient setCodeFilter(CodeFilter codeFilter) {
        this.codeFilter = codeFilter;
        return this;
    }

    public ApiClient setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public static void setJsonParser(JsonParser jsonParser) {
        ApiClient.jsonParser = jsonParser;
    }

    public static JsonParser getJsonParser() {
        return jsonParser;
    }

    /**
     * 创建Api对象
     */
    public <T> T create(final Class<T> service) {
        BaseUrl baseUrl = service.getAnnotation(BaseUrl.class);
        final String anoBaseUrl;
        if (baseUrl != null) {
            anoBaseUrl = baseUrl.value();
        } else {
            anoBaseUrl = null;
        }
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, @Nullable Object[] args)
                            throws Throwable {
                        // If the method is a method from Object then defer to normal invocation.
                        Class<?> declaringClass = method.getDeclaringClass();
                        if (declaringClass == Object.class) {
                            return method.invoke(this, args);
                        }
                        ApiRequest apiRequest = getApiRequest(method);
                        if (apiRequest.getType() != 0) {
                            apiRequest.setAnoBaseUrl(anoBaseUrl);
                            apiRequest.generateParams(method, args);
                            apiRequest.apiName = (declaringClass != null ? declaringClass.getName() : service.getName())
                                    + "." + method.getName();
                            if (method.getReturnType() == Observable.class) {
                                Observable observable = generateObservable(method, args);
                                observable.setApiRequest(apiRequest);
                                observable.setReturnType(method.getGenericReturnType());
                                return observable;
                            } else {
                                return connect(apiRequest, method.getGenericReturnType());
                            }
                        }
                        return null;
                    }
                });
    }

    /**
     * 创建Observable对象
     */
    private Observable generateObservable(Method method, @Nullable Object[] args) throws Throwable {
        Class<?> type = method.getReturnType();
        Observable observable = (Observable) type.newInstance();
        observable.setApiClient(this);
        return observable;
    }

    /**
     * 获取ApiRequest
     */
    private ApiRequest getApiRequest(Method method) {
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setBaseUrl(baseUrl);
        GET get = method.getAnnotation(GET.class);
        if (get != null) {
            apiRequest.setType(1);
            apiRequest.setUrl(get.value());
        } else {
            POST post = method.getAnnotation(POST.class);
            if (post != null) {
                apiRequest.setType(2);
                apiRequest.setUrl(post.value());
            } else {
                DELETE delete = method.getAnnotation(DELETE.class);
                if (delete != null) {
                    apiRequest.setType(3);
                    apiRequest.setUrl(delete.value());
                } else {
                    PUT put = method.getAnnotation(PUT.class);
                    if (put != null) {
                        apiRequest.setType(4);
                        apiRequest.setUrl(put.value());
                    }
                }
            }
        }
        KeepStream stream = method.getAnnotation(KeepStream.class);
        if (stream != null) {
            apiRequest.setKeepStream(true);
        }
        MOCK mock = method.getAnnotation(MOCK.class);
        if (mock != null && mock.enable()) {
            apiRequest.mockType = mock.type();
            apiRequest.mockData = mock.value();
        }
        return apiRequest;
    }

    /**
     * 异步请求
     */
    protected void asyncConnect(final ApiRequest apiRequest, final Type returnType, final Observable observable) {
        final TypeInfo typeInfo = new TypeInfo(returnType);
        //使用mock数据
        if (mockEnable && apiRequest.mockData != null) {
            new MockData(apiRequest, onRequestListener, charset).getResult(new MockData.OnResult() {
                @Override
                public void result(String data, Responce responce) {
                    final Observer observer = observable.getObserver();
                    if (observer == null) {
                        return;
                    }
                    if (typeInfo.getType() == Responce.class) {
                        if (responce == null) {
                            responce = new Responce();
                            try {
                                Field field = Responce.class.getDeclaredField("code");
                                field.setAccessible(true);
                                field.set(responce, 200);
                            } catch (Exception ignore) {
                            }
                            responce.result = data == null ? null : data.getBytes();
                        }
                        observer.success(responce);
                    } else if (typeInfo.getType() == String.class) {
                        observer.success(data);
                    } else {
                        observer.success(parseObject(data, typeInfo));
                    }
                }
            });
            return;
        }
        ++apiRequest.useTimes;
        final HttpClient client = new HttpClient();
        final RequestParams params = apiRequest.getParams();
        final String url = apiRequest.getUrl();
        if (apiRequest.isKeepStream() && typeInfo.getType() == Responce.class) {
            client.setKeepStream(true);
        }
        apiRequest.client = client;
        client.setProgressListener(apiRequest.progressListener);
        if (onRequestListener != null) {
            onRequestListener.onRequestBegin(client, apiRequest);
        }
        int type = apiRequest.getType();
        if (type != 0) {
            if (observable.observableManager != null) {
                observable.observableManager.registerObservable(observable);
            }
            AsyncClient<Responce> asyncClient = new DefaultAsyncClient() {
                @Override
                public void callBack(Responce responce) {
                    if (observable.observableManager != null) {
                        observable.observableManager.unRegisterObservable(observable);
                    }
                    ApiResult result = null;
                    if (onRequestListener != null) {
                        result = new ApiResult();
                        result.url = url;
                        result.params = params;
                        result.responce = responce;
                        result.typeInfo = typeInfo;
                        result.observable = observable;
                        result.type = 1;
                        result.request = apiRequest;
                        result.apiClient = ApiClient.this;
                        onRequestListener.onRequestEnd(client, result);
                    }
                    if (result != null && result.accept) {
                        //已被处理拦截
                    } else {
                        result(responce, typeInfo, observable.getObserver());
                    }
                }
            };
            switch (type) {
                case 1:
                    client.asyncGet(url, params, asyncClient);
                    break;
                case 2:
                    client.asyncPost(url, params, asyncClient);
                    break;
                case 3:
                    client.asyncDelete(url, params, asyncClient);
                    break;
                case 4:
                    client.asyncPut(url, params, asyncClient);
                    break;
            }
        }
    }

    /**
     * 处理返回结果
     */
    private void result(Responce responce, TypeInfo typeInfo, Observer observer) {
        if (observer == null) {
            return;
        }
        Type classType = typeInfo.getType();
        //优先外部筛选器通过尝试解析，否则200系列解析，返回类型Responce通过success返回
        if (classType == Responce.class || (codeFilter != null ? codeFilter.isSuccessCode(responce.getCode()) : responce.getCode() / 100 == 2)) {
            try {
                Object bean = classType == String.class ? (charset == null ? responce.toString() : new String(responce.result, charset)) :
                        classType == Responce.class ? responce :
                                parseObject((charset == null ? responce.toString() : new String(responce.result, charset)), typeInfo);
                observer.success(bean);
            } catch (Exception e) {
                e.printStackTrace();
                observer.failed(responce.getCode(), e);
            }
        } else {
            observer.failed(responce.getCode(), new ResultException(responce));
        }
    }

    /**
     * 同步请求
     */
    protected Object connect(ApiRequest apiRequest, Type typeClass) {
        TypeInfo typeInfo = new TypeInfo(typeClass);
        //使用mock数据
        if (mockEnable && apiRequest.mockData != null) {
            MockData mockData = new MockData(apiRequest, onRequestListener, charset);
            String result = mockData.getResult();
            if (typeInfo.getType() == Responce.class) {
                Responce responce = mockData.getResponce();
                if (responce == null) {
                    responce = new Responce();
                    try {
                        Field field = Responce.class.getDeclaredField("code");
                        field.setAccessible(true);
                        field.set(responce, 200);
                    } catch (Exception ignore) {
                    }
                    responce.result = result == null ? null : result.getBytes();
                }
                return responce;
            } else if (typeInfo.getType() == String.class) {
                return result;
            } else {
                return parseObject(result, typeInfo);
            }
        }
        ++apiRequest.useTimes;
        final HttpClient client = new HttpClient();
        final RequestParams params = apiRequest.getParams();
        final String url = apiRequest.getUrl();
        if (apiRequest.isKeepStream() && typeInfo.getType() == Responce.class) {
            client.setKeepStream(true);
        }
        apiRequest.client = client;
        client.setProgressListener(apiRequest.progressListener);
        if (onRequestListener != null) {
            onRequestListener.onRequestBegin(client, apiRequest);
        }
        int type = apiRequest.getType();
        Responce responce = null;
        if (type == 1) {
            responce = client.get(url, params);
        } else if (type == 2) {
            responce = client.post(url, params);
        } else if (type == 3) {
            responce = client.delete(url, params);
        } else if (type == 4) {
            responce = client.put(url, params);
        }
        ApiResult result = null;
        if (onRequestListener != null) {
            result = new ApiResult();
            result.url = url;
            result.params = params;
            result.responce = responce;
            result.typeInfo = typeInfo;
            result.type = 0;
            result.request = apiRequest;
            result.apiClient = ApiClient.this;
            onRequestListener.onRequestEnd(client, result);
        }
        if (result != null && result.accept) {
            return result.responceObject;
        } else {
            if (responce != null) {
                return result(responce, typeInfo);//同步方式不支持返回集合
            }
            return null;
        }
    }

    /**
     * 处理返回结果
     */
    private Object result(Responce responce, TypeInfo typeInfo) {
        try {
            Type classType = typeInfo.getType();
            return classType == String.class ? (charset == null ? responce.toString() : new String(responce.result, charset)) :
                    classType == Responce.class ? responce :
                            parseObject((charset == null ? responce.toString() : new String(responce.result, charset)), typeInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * json转对象
     */
    protected Object parseObject(String json, TypeInfo typeInfo) {
        return jsonParser == null ? null : jsonParser.jsonToObject(json, typeInfo);
    }

}
