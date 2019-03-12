package com.loror.lororboot.httpApi;

import com.loror.lororUtil.http.HttpClient;
import com.loror.lororUtil.http.RequestParams;

public interface OnRequestListener {
    void onRequestBegin(HttpClient client, RequestParams params, String url);
}
