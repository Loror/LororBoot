package com.loror.lororboot.httpApi;

public interface Observer<T> {
    void success(T data);

    void failed(int code, Throwable e);
}
