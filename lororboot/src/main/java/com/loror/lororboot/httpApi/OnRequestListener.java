package com.loror.lororboot.httpApi;

import com.loror.lororUtil.http.HttpClient;
import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.http.Responce;

public interface OnRequestListener {
    void onRequestBegin(HttpClient client, ApiRequest request);

    void onRequestEnd(HttpClient client, ApiResult result);
}
