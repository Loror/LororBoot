package com.loror.lororboot.httpApi;

import com.loror.lororUtil.http.HttpClient;
import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.http.Responce;

public interface OnRequestListener {
    void onRequestBegin(HttpClient client, RequestParams params, String url);

    void onRequestEnd(Responce responce, RequestParams params, String url);
}
