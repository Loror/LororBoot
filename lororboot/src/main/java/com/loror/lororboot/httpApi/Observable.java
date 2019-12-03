package com.loror.lororboot.httpApi;

import com.loror.lororUtil.http.ProgressListener;

import java.lang.reflect.Type;

public class Observable<T> {

    private ApiClient apiClient;
    private ApiRequest apiRequest;
    private Type returnType;

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

    public void listen(ProgressListener listener) {
        if (apiRequest != null) {
            apiRequest.setProgressListener(listener);
        }
    }

    public void subscribe(final Observer<T> observer) {
        apiClient.asyncConnect(apiRequest, returnType, observer);
    }
}
