package com.loror.lororboot.httpApi;

import com.loror.lororUtil.http.ProgressListener;

import java.lang.reflect.Type;

public class Observable<T> {

    private ApiClient apiClient;
    private ApiRequest apiRequest;
    private Type returnType;
    private Observer<T> observer;
    protected ObservableManager observableManager;

    protected void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    protected void setApiRequest(ApiRequest apiRequest) {
        this.apiRequest = apiRequest;
    }

    public ApiRequest getApiRequest() {
        return apiRequest;
    }

    protected void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Observer<T> getObserver() {
        return observer;
    }

    /**
     * 监听进度，上传文件时有效
     */
    public Observable<T> listen(ProgressListener listener) {
        if (apiRequest != null) {
            apiRequest.progressListener = listener;
        }
        return this;
    }

    /**
     * 开始任务并提交监听
     */
    public Observable<T> subscribe(Observer<T> observer) {
        this.observer = observer;
        apiClient.asyncConnect(apiRequest, returnType, this);
        return this;
    }

    /**
     * 注册管理
     */
    public Observable<T> manage(ObservableManager observableManager) {
        this.observableManager = observableManager;
        return this;
    }

    /**
     * 注销监听
     */
    public void unSubscribe() {
        this.observer = null;
    }

    /**
     * 注销监听并关闭连接
     */
    public void cancel() {
        unSubscribe();
        if (apiRequest != null) {
            apiRequest.client.cancel();
        }
    }
}
