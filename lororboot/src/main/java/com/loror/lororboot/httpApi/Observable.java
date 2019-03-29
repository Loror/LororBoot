package com.loror.lororboot.httpApi;

public class Observable<T> {
    private ApiClient apiClient;
    private ApiRequest apiRequest;

    protected void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    protected void setApiRequest(ApiRequest apiRequest) {
        this.apiRequest = apiRequest;
    }

    public ApiRequest getApiRequest() {
        return apiRequest;
    }

    public void subscribe(final Observer<T> observer) {
        apiClient.asyncConnect(apiRequest, observer);
    }
}
