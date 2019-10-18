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
import com.loror.lororboot.annotation.POST;
import com.loror.lororboot.annotation.PUT;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

public class ApiClient {

    private String baseUrl;

    protected static JsonParser jsonParser;
    private OnRequestListener onRequestListener;
    private CodeFilter codeFilter;
    private Charset charset;

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
    public <T> T create(Class<T> service) {
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
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }
                        ApiRequest apiRequest = getApiRequest(method);
                        if (apiRequest.getType() != 0) {
                            apiRequest.setAnoBaseUrl(anoBaseUrl);
                            apiRequest.generateParams(method, args);
                            if (method.getReturnType() == Observable.class) {
                                Observable observable = generateObservable(method, args);
                                observable.setApiRequest(apiRequest);
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
        return apiRequest;
    }

    /**
     * 异步请求
     */
    protected void asyncConnect(final ApiRequest apiRequest, final Observer observer) {
        ++apiRequest.useTimes;
        final HttpClient client = new HttpClient();
        final RequestParams params = apiRequest.getParams();
        final String url = apiRequest.getUrl();
        if (onRequestListener != null) {
            onRequestListener.onRequestBegin(client, apiRequest);
        }
        int type = apiRequest.getType();
        if (type != 0) {
            AsyncClient<Responce> asyncClient = new DefaultAsyncClient() {
                @Override
                public void callBack(Responce responce) {
                    ApiResult result = null;
                    TypeInfo typeInfo = new TypeInfo(observer);
                    if (onRequestListener != null) {
                        result = new ApiResult();
                        result.url = url;
                        result.params = params;
                        result.responce = responce;
                        result.observer = observer;
                        result.typeInfo = typeInfo;
                        result.type = 1;
                        result.request = apiRequest;
                        result.client = ApiClient.this;
                        onRequestListener.onRequestEnd(client, result);
                    }
                    if (result != null && result.accept) {
                        //已被处理拦截
                    } else {
                        result(responce, typeInfo, observer);
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
        Class<?>[] types = typeInfo.getTClass();
        Class<?> classType = null;
        if (types.length == 1) {
            classType = types[0];
        }
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
            observer.failed(responce.getCode(), responce.getThrowable());
        }
    }

    /**
     * 同步请求
     */
    protected Object connect(ApiRequest apiRequest, Type typeClass) {
        ++apiRequest.useTimes;
        final HttpClient client = new HttpClient();
        final RequestParams params = apiRequest.getParams();
        final String url = apiRequest.getUrl();
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
        TypeInfo typeInfo = new TypeInfo(typeClass);
        ;
        if (onRequestListener != null) {
            result = new ApiResult();
            result.url = url;
            result.params = params;
            result.responce = responce;
            result.typeInfo = typeInfo;
            result.request = apiRequest;
            result.client = ApiClient.this;
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
            Class<?>[] types = typeInfo.getTClass();
            Class<?> classType = null;
            if (types.length == 1) {
                classType = types[0];
            }
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
