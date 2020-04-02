package com.loror.lororboot.httpApi;

import com.loror.lororUtil.convert.UrlUtf8Util;
import com.loror.lororUtil.http.FileBody;
import com.loror.lororUtil.http.HttpClient;
import com.loror.lororUtil.http.ProgressListener;
import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.text.TextUtil;
import com.loror.lororboot.annotation.AsJson;
import com.loror.lororboot.annotation.DefaultHeaders;
import com.loror.lororboot.annotation.DefaultParams;
import com.loror.lororboot.annotation.Multipart;
import com.loror.lororboot.annotation.Gzip;
import com.loror.lororboot.annotation.Header;
import com.loror.lororboot.annotation.Param;
import com.loror.lororboot.annotation.ParamJson;
import com.loror.lororboot.annotation.ParamKeyValue;
import com.loror.lororboot.annotation.ParamObject;
import com.loror.lororboot.annotation.Path;
import com.loror.lororboot.annotation.Query;
import com.loror.lororboot.annotation.Url;
import com.loror.lororboot.annotation.UrlEnCode;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiRequest {

    private String baseUrl, anoBaseUrl;
    private RequestParams params;
    protected HttpClient client;
    private int type;//1,get;2,post;3,delete;4,put
    private String url;
    protected ProgressListener progressListener;
    private boolean keepStream;
    protected int useTimes;//计数Request使用次数
    protected String apiName;
    protected int mockType;
    protected String mockData;
    private HashMap<String, String> querys;//Query注解指定的参数
    private List<com.loror.lororboot.httpApi.Path> paths;//Path注解指定的参数
    private String anoUrl;//Url指定的url地址
    private boolean anoUseValueUrl;

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
        String finalBaseUrl = !TextUtil.isEmpty(anoBaseUrl) ? anoBaseUrl
                : !TextUtil.isEmpty(baseUrl) ? baseUrl :
                "";
        String finalUrl = finalBaseUrl + url;
        if (!TextUtil.isEmpty(anoUrl)) {
            if (anoUseValueUrl) {
                finalUrl += anoUrl;
            } else {
                finalUrl = anoUrl;
            }
        }
        if (paths != null) {
            for (com.loror.lororboot.httpApi.Path path : paths) {
                finalUrl = finalUrl.replace("{" + path.name + "}", path.value);
            }
        }
        if (querys != null) {
            StringBuilder builder = new StringBuilder();
            for (String key : querys.keySet()) {
                builder.append(key)
                        .append(params != null ? params.getSplicing(null, 1) : "=")
                        .append(UrlUtf8Util.toUrlString(querys.get(key)))
                        .append(params != null ? params.getSplicing(null, 2) : "&");
            }
            if (builder.length() > 0) {
                builder.deleteCharAt(builder.length() - 1);
                finalUrl += params != null ? params.getSplicing(finalUrl, 0) : (finalUrl.contains("?") ? "&" : "?");
                finalUrl += builder.toString();
            }
        }
        return finalUrl;
    }

    public void setKeepStream(boolean keepStream) {
        this.keepStream = keepStream;
    }

    public boolean isKeepStream() {
        return keepStream;
    }

    public ProgressListener getProgressListener() {
        return progressListener;
    }

    public int getUseTimes() {
        return useTimes;
    }

    public String getApiName() {
        return apiName;
    }

    public RequestParams getParams() {
        return params;
    }

    public HttpClient getClient() {
        return client;
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
        Multipart multipart = method.getAnnotation(Multipart.class);
        if (multipart != null) {
            params.setUserMultiForPost(true);
        }
        AsJson asJson = method.getAnnotation(AsJson.class);
        if (asJson != null) {
            params.setAsJson(true);
        }
        UrlEnCode urlEnCode = method.getAnnotation(UrlEnCode.class);
        if (urlEnCode != null) {
            params.setUseDefaultConverterInPost(true);
        }
        Gzip gzip = method.getAnnotation(Gzip.class);
        if (gzip != null) {
            params.setGzip(true);
        }
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
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
        if (annotations == null) {
            return;
        }
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
            } else if (annotations[i].annotationType() == ParamKeyValue.class) {
                if (arg != null) {
                    params.fromKeyValue(arg.toString());
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
            } else if (annotations[i].annotationType() == Query.class) {
                String name = ((Query) annotations[i]).value();
                if (querys == null) {
                    querys = new HashMap<>();
                }
                querys.put(name, arg == null ? "" : String.valueOf(arg));
                break;
            } else if (annotations[i].annotationType() == Path.class) {
                String name = ((Path) annotations[i]).value();
                String value = arg == null ? "" : String.valueOf(arg);
                if (paths == null) {
                    paths = new ArrayList<>();
                }
                com.loror.lororboot.httpApi.Path path = new com.loror.lororboot.httpApi.Path(name, value);
                paths.add(path);
                break;
            } else if (annotations[i].annotationType() == Url.class) {
                if (!TextUtil.isEmpty(anoUrl)) {
                    throw new IllegalArgumentException("只能指定一个Url注解");
                }
                anoUseValueUrl = ((Url) annotations[i]).useValueUrl();
                anoUrl = arg == null ? "" : String.valueOf(arg);
                break;
            }
        }
    }
}
