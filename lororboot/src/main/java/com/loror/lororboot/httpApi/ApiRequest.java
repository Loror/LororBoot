package com.loror.lororboot.httpApi;

import com.loror.lororUtil.http.FileBody;
import com.loror.lororUtil.http.RequestParams;
import com.loror.lororboot.annotation.AsJson;
import com.loror.lororboot.annotation.DefaultHeaders;
import com.loror.lororboot.annotation.DefaultParams;
import com.loror.lororboot.annotation.ForceForm;
import com.loror.lororboot.annotation.Header;
import com.loror.lororboot.annotation.Param;
import com.loror.lororboot.annotation.ParamJson;
import com.loror.lororboot.annotation.ParamObject;
import com.loror.lororboot.annotation.UrlEnCode;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class ApiRequest {

    private String baseUrl, anoBaseUrl;
    private RequestParams params;
    private int type;//1,get;2,post;3,delete;4,put
    private String url;
    protected int useTimes;//计数Request使用次数

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setAnoBaseUrl(String anoBaseUrl) {
        this.anoBaseUrl = anoBaseUrl;
    }

    public String getAnoBaseUrl() {
        return anoBaseUrl;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return anoBaseUrl != null && anoBaseUrl.length() != 0 ?
                (anoBaseUrl + url) : baseUrl != null && baseUrl.length() != 0 ?
                (baseUrl + url) :
                url;
    }

    public int getUseTimes() {
        return useTimes;
    }

    public RequestParams getParams() {
        return params;
    }

    /**
     * 创建RequestParams
     */
    protected void generateParams(Method method, Object[] args) throws Throwable {
        params = new RequestParams();
        DefaultParams defaultParams = method.getAnnotation(DefaultParams.class);
        if (defaultParams != null) {
            int size = Math.min(defaultParams.keys().length, defaultParams.values().length);
            for (int i = 0; i < size; i++) {
                params.addParams(defaultParams.keys()[i], defaultParams.values()[i]);
            }
        }
        DefaultHeaders defaultHeaders = method.getAnnotation(DefaultHeaders.class);
        if (defaultHeaders != null) {
            int size = Math.min(defaultHeaders.keys().length, defaultHeaders.values().length);
            for (int i = 0; i < size; i++) {
                params.addHeader(defaultHeaders.keys()[i], defaultHeaders.values()[i]);
            }
        }
        ForceForm forceForm = method.getAnnotation(ForceForm.class);
        if (forceForm != null) {
            params.setUserFormForPost(true);
        }
        AsJson asJson = method.getAnnotation(AsJson.class);
        if (asJson != null) {
            params.setAsJson(true);
        }
        UrlEnCode urlEnCode = method.getAnnotation(UrlEnCode.class);
        if (urlEnCode != null) {
            params.setUseDefaultConverterInPost(true);
        }
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < annotations.length; i++) {
            if (types[i] == RequestParams.class) {
                HashMap<String, Object> old = params.getParams();
                List<FileBody> oldFile = params.getFiles();
                params = (RequestParams) args[i];
                if (old.size() > 0) {
                    for (String key : old.keySet()) {
                        Object value = old.get(key);
                        if (value != null) {
                            if (value instanceof Integer) {
                                params.addParams(key, (Integer) value);
                            } else if (value instanceof Long) {
                                params.addParams(key, (Long) value);
                            } else if (value instanceof Float) {
                                params.addParams(key, (Float) value);
                            } else if (value instanceof Double) {
                                params.addParams(key, (Double) value);
                            } else if (value instanceof Boolean) {
                                params.addParams(key, (Boolean) value);
                            } else if (value.getClass().isArray()) {
                                params.addParams(key, (Object[]) value);
                            } else {
                                params.addParams(key, String.valueOf(value));
                            }
                        } else {
                            params.addParams(key, (String) null);
                        }
                    }
                }
                if (oldFile.size() > 0) {
                    for (FileBody file : oldFile) {
                        params.addParams(file.getKey(), file);
                    }
                }
            } else {
                addField(params, annotations[i], types[i], args[i]);
            }
        }
    }

    /**
     * 添加Field
     */
    private void addField(RequestParams params, Annotation[] annotations, Class<?> type, Object arg) {
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].annotationType() == Param.class) {
                String name = ((Param) annotations[i]).value();
                if (type == FileBody.class) {
                    params.addParams(name, (FileBody) arg);
                } else if (type == File.class) {
                    params.addParams(name, new FileBody(arg == null ? null : ((File) arg).getAbsolutePath()));
                } else if (type.isArray()) {
                    if (arg != null) {
                        Object[] array = (Object[]) arg;
                        if (type.getComponentType() == FileBody.class) {
                            for (int j = 0; j < array.length; j++) {
                                params.addParams(name, (FileBody) array[j]);
                            }
                        } else if (type.getComponentType() == File.class) {
                            for (int j = 0; j < array.length; j++) {
                                params.addParams(name, new FileBody(array[j] == null ? null : ((File) array[j]).getAbsolutePath()));
                            }
                        } else {
                            params.addParams(name, array);
                        }
                    }
                } else {
                    params.addParams(name, arg == null ? null : String.valueOf(arg));
                }
                break;
            } else if (annotations[i].annotationType() == ParamObject.class) {
                if (arg != null) {
                    params.fromObject(arg);
                }
                break;
            } else if (annotations[i].annotationType() == ParamJson.class) {
                if (arg != null) {
                    if (type == String.class) {
                        params.setJson((String) arg);
                    } else if (ApiClient.jsonParser != null) {
                        params.setJson(ApiClient.jsonParser.objectToJson(arg));
                    } else {
                        params.setJson(String.valueOf(arg));
                    }
                }
                break;
            } else if (annotations[i].annotationType() == Header.class) {
                String name = ((Header) annotations[i]).value();
                params.addHeader(name, arg == null ? "" : String.valueOf(arg));
                break;
            }
        }
    }
}
