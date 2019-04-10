package com.loror.lororboot.httpApi;

public class ApiHook {
    protected int type;//0，同步，1，异步
    protected boolean accept;//是否已经重新请求

    //框架需使用的参数
    protected ApiRequest request;
    protected ApiClient client;
    protected Class<?> classType;
    protected Observer observer;
    protected Object responce;

    //hook后允许处理的方法

    public int getType() {
        return type;
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

    public void setAccept(boolean accept) {
        this.accept = accept;
    }

    public void setResponce(Object responce) {
        this.responce = responce;
    }

    public Observer getObserver() {
        return observer;
    }

    //通知重新请求，结束后请求将接收到新的结果
    public void requestAgain() {
        if (type == 0) {
            accept = true;
            responce = client.connect(request, classType);
        } else if (type == 1) {
            accept = true;
            client.asyncConnect(request, observer);
        }
    }
}
