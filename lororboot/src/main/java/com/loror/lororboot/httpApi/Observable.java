package com.loror.lororboot.httpApi;

public class Observable<T> {
    private ApiClient apiClient;

    protected void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void subscribe(final Observer<T> observer) {
        apiClient.asyncConnect(observer);
    }
}
