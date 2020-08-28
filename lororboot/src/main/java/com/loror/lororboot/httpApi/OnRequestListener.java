package com.loror.lororboot.httpApi;

import com.loror.lororUtil.http.HttpClient;

public interface OnRequestListener {
    void onRequestBegin(HttpClient client, ApiRequest request);

    void onRequestEnd(HttpClient client, ApiResult result);
}
