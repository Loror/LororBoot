package com.loror.lororboot.httpApi;

import com.loror.lororUtil.http.FileBody;
import com.loror.lororUtil.http.RequestParams;
import com.loror.lororboot.annotation.Header;
import com.loror.lororboot.annotation.Param;
import com.loror.lororboot.annotation.ParamJson;
import com.loror.lororboot.annotation.ParamObject;

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

    public RequestParams getParams() {
        return params;
    }

    /**
     * 创建RequestParams
     */
    public void generateParams(Method method, Object[] args) throws Throwable {
        params = new RequestParams();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            if (args[i] instanceof RequestParams) {
                HashMap<String, String> old = params.getParams();
                List<FileBody> oldFile = params.getFiles();
                params = (RequestParams) args[i];
                if (old.size() > 0) {
                    for (String key : old.keySet()) {
                        params.addParams(key, old.get(key));
                    }
                }
                if (oldFile.size() > 0) {
                    for (FileBody file : oldFile) {
                        params.addParams(file.getKey(), file);
                    }
                }
            } else {
                addField(params, annotations[i], args[i]);
            }
        }
    }

    /**
     * 添加Field
     */
    private void addField(RequestParams params, Annotation[] annotations, Object arg) {
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].annotationType() == Param.class) {
                String name = ((Param) annotations[i]).value();
                if (name.length() > 0) {
                    if (arg instanceof FileBody) {
                        params.addParams(name, (FileBody) arg);
                    } else if (arg instanceof File) {
                        params.addParams(name, new FileBody(((File) arg).getAbsolutePath()));
                    } else {
                        params.addParams(name, String.valueOf(arg));
                    }
                }
                break;
            } else if (annotations[i].annotationType() == ParamObject.class) {
                if (arg != null) {
                    params.fromObject(arg);
                }
                break;
            } else if (annotations[i].annotationType() == ParamJson.class) {
                if (arg != null) {
                    if (arg instanceof String) {
                        params.asJson((String) arg);
                    } else if (ApiClient.jsonParser != null) {
                        params.asJson(ApiClient.jsonParser.objectToJson(arg));
                    } else {
                        params.asJson(String.valueOf(arg));
                    }
                }
                break;
            } else if (annotations[i].annotationType() == Header.class) {
                String name = ((Header) annotations[i]).value();
                if (name.length() > 0) {
                    params.addHeader(name, String.valueOf(arg));
                }
                break;
            }
        }
    }
}
