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
    protected TypeInfo typeInfo;
    protected ApiClient apiClient;
    protected ApiRequest request;
    //异步
    protected Observable observable;
    //同步
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

    public void setResponceObject(Object responce) {
        this.responceObject = responce;
    }

    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    public Observer getObserver() {
        return observable == null ? null : observable.getObserver();
    }

    //通知重新请求，结束后请求将接收到新的结果
    public void requestAgain() {
        if (type == 0) {
            accept = true;
            responceObject = apiClient.connect(request, typeInfo.getType());
        } else if (type == 1) {
            accept = true;
            apiClient.asyncConnect(request, typeInfo.getType(), observable);
        }
    }

}
