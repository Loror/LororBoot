package com.loror.lororboot.httpApi;

import com.loror.lororUtil.http.Responce;

public class ResultException extends RuntimeException {

    private Responce responce;

    public ResultException(Responce responce) {
        this.responce = responce;
    }

    public Responce getResponce() {
        return responce;
    }
}
