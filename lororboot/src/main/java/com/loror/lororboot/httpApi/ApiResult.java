package com.loror.lororboot.httpApi;

import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.http.Responce;

public class ApiResult {

    private String url;
    private RequestParams params;
    private Responce responce;
    protected ApiHook hook;//可hook住返回，待处理后再返回

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

    public ApiHook hook() {
        if (hook == null) {
            hook = new ApiHook();
        }
        return hook;
    }

}
