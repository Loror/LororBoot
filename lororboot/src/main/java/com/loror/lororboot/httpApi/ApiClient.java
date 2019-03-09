package com.loror.lororboot.httpApi;

import android.support.annotation.Nullable;

import com.loror.lororUtil.http.DefaultAsyncClient;
import com.loror.lororUtil.http.FileBody;
import com.loror.lororUtil.http.HttpClient;
import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.http.Responce;
import com.loror.lororboot.annotation.GET;
import com.loror.lororboot.annotation.POST;
import com.loror.lororboot.annotation.Param;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;

public class ApiClient {

    private String baseUrl;
    private Res res;
    private RequestParams params;
    private static JsonParser jsonParser;

    public ApiClient setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public static void setJsonParser(JsonParser jsonParser) {
        ApiClient.jsonParser = jsonParser;
    }

    public static JsonParser getJsonParser() {
        return jsonParser;
    }

    public int getType() {
        return res.type;
    }

    public String getUrl() {
        return baseUrl != null ? (baseUrl + res.url) : res.url;
    }

    public RequestParams getParams() {
        return params;
    }

    private static class Res {
        private int type;
        private String url;

        private Res(int type, String url) {
            this.type = type;
            this.url = url;
        }
    }

    /**
     * 创建Api对象
     */
    public <T> T create(Class<T> service) {
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, @Nullable Object[] args)
                            throws Throwable {
                        // If the method is a method from Object then defer to normal invocation.
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }
                        res = getRequestType(method);
                        if (res != null) {
                            params = generateParams(method, args);
                            if (method.getReturnType() == Observable.class) {
                                return generateObservable(method, args);
                            } else {
                                return connect(method.getReturnType());
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
     * 创建RequestParams
     */
    private RequestParams generateParams(Method method, Object[] args) throws Throwable {
        RequestParams params = new RequestParams();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            if (args[i] instanceof RequestParams) {
                HashMap<String, String> old = params.getParmas();
                params = (RequestParams) args[i];
                if (old.size() > 0) {
                    for (String key : old.keySet()) {
                        params.addParams(key, old.get(key));
                    }
                }
            } else {
                String name = getFieldName(annotations[i]);
                if (name != null) {
                    if (args[i] instanceof FileBody) {
                        params.addParams(name, (FileBody) args[i]);
                    } else if (args[i] instanceof File) {
                        params.addParams(name, new FileBody(((File) args[i]).getAbsolutePath()));
                    } else {
                        params.addParams(name, String.valueOf(args[i]));
                    }
                }
            }
        }
        return params;
    }

    /**
     * 获取键名
     */
    private String getFieldName(Annotation[] annotations) {
        Param field = null;
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].annotationType() == Param.class) {
                field = (Param) annotations[i];
                break;
            }
        }
        return field == null ? null : field.value();
    }

    /**
     * 获取type
     */
    private Res getRequestType(Method method) {
        Res res = null;
        GET get = method.getAnnotation(GET.class);
        if (get != null) {
            res = new Res(1, get.value());
        } else {
            POST post = method.getAnnotation(POST.class);
            if (post != null) {
                res = new Res(2, post.value());
            }
        }
        if (res == null) {
            res = new Res(0, "");
        }
        return res;
    }

    /**
     * 异步请求
     */
    protected void asyncConnect(final Observer observer) {
        final HttpClient client = new HttpClient();
        int type = getType();
        if (type == 1) {
            client.asyncGet(getUrl(), params, new DefaultAsyncClient() {
                @Override
                public void callBack(Responce responce) {
                    result(responce, getTClass(observer), observer);
                }
            });
        } else if (type == 2) {
            client.asyncPost(getUrl(), params, new DefaultAsyncClient() {
                @Override
                public void callBack(Responce responce) {
                    result(responce, getTClass(observer), observer);
                }
            });
        }
    }

    /**
     * 动态获取泛型
     */
    private Class<?> getTClass(Object observer) {
        Type[] superClass = observer.getClass().getGenericInterfaces();
        if (superClass[0] instanceof ParameterizedType) {
            Type type = ((ParameterizedType) superClass[0]).getActualTypeArguments()[0];
            return (Class<?>) type;
        } else {
            System.out.println("Not ParameterizedType");
        }
        return null;
    }

    /**
     * 处理返回结果
     */
    private void result(Responce responce, Class<?> classType, Observer observer) {
        if (responce.getCode() == 200) {
            try {
                Object bean = classType == String.class ? responce.toString() : classType == Responce.class ? responce : jsonParser == null ? null : jsonParser.jsonToObject(responce.toString(), classType);
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
    private Object connect(Class<?> classType) {
        final HttpClient client = new HttpClient();
        int type = res.type;
        if (type == 1) {
            Responce responce = client.get(getUrl(), params);
            return result(responce, classType);
        } else if (type == 2) {
            Responce responce = client.post(getUrl(), params);
            return result(responce, classType);
        }
        return null;
    }

    /**
     * 处理返回结果
     */
    private Object result(Responce responce, Class<?> classType) {
        if (responce.getCode() == 200) {
            try {
                return classType == String.class ? responce.toString() : classType == Responce.class ? responce : jsonParser == null ? null : jsonParser.jsonToObject(responce.toString(), classType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
