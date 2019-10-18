package com.loror.lororboot.httpApi;

import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.http.Responce;

public class ApiResult {

    //基本信息
    protected String url;
    protected RequestParams params;
    protected Responce responce;

    protected int type;//0，同步，1，异步
    protected boolean accept;//是否已经重新请求

    //框架需使用的参数及拦截所需参数
    protected ApiRequest request;
    protected ApiClient client;
    protected TypeInfo typeInfo;
    protected Observer observer;
    protected Object responceObject;

    public String getUrl() {
        return url;
    }

    public RequestParams getParams() {
        return params;
    }

    public Responce getResponce() {
        return responce;
    }

    public int getType() {
        return type;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }

    public ApiRequest getRequest() {
        return request;
    }

    public ApiClient getClient() {
        return client;
    }

    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    public void setResponceObject(Object responce) {
        this.responceObject = responce;
    }

    public Observer getObserver() {
        return observer;
    }

    /**
     * json转对象
     */
    public Object parseObject(String json, TypeInfo typeInfo) {
        return client.parseObject(json, typeInfo);
    }

    //通知重新请求，结束后请求将接收到新的结果
    public void requestAgain() {
        if (type == 0) {
            accept = true;
            responceObject = client.connect(request, typeInfo.getType());
        } else if (type == 1) {
            accept = true;
            client.asyncConnect(request, observer);
        }
    }
}
