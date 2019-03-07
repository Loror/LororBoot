package com.loror.demo;

import com.loror.lororboot.httpApi.Field;
import com.loror.lororboot.httpApi.GET;
import com.loror.lororboot.httpApi.Observable;

public interface ServerApi {
    @GET("")
    Observable<String> getResult(@Field("id") String id);
}
