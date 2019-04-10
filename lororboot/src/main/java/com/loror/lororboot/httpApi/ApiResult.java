package com.loror.lororboot.httpApi;

import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.http.Responce;

public class ApiResult {

    protected int type;//0，同步，1，异步
    protected boolean accept;//是否已经重新请求
    private String url;
    private RequestParams params;
    private Responce responce;

    //框架需使用的参数
    protected ApiRequest request;
    protected ApiClient client;
    protected Class<?> classType;
    protected Observer observer;
    protected Object responceObject;

    public ApiResult(String url, RequestParams params, Responce responce) {
        this.url = url;
        this.params = params;
        this.responce = responce;
    }

    public String getUrl() {
        return url;
    }

    public RequestParams getParams() {
        return params;
    }

    public Responce getResponce() {
        return responce;
    }

    public void setResponce(Responce responce) {
        this.responce = responce;
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

    public Class<?> getClassType() {
        return classType;
    }

    public void setResponceObject(Object responce) {
        this.responceObject = responce;
    }

    public Observer getObserver() {
        return observer;
    }

    //通知重新请求，结束后请求将接收到新的结果
    public void requestAgain() {
        if (type == 0) {
            accept = true;
            responceObject = client.connect(request, classType);
        } else if (type == 1) {
            accept = true;
            client.asyncConnect(request, observer);
        }
    }
}
