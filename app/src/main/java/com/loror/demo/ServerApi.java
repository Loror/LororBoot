package com.loror.demo;

import com.loror.lororboot.httpApi.ByteArray;
import com.loror.lororboot.httpApi.Field;
import com.loror.lororboot.httpApi.GET;
import com.loror.lororboot.httpApi.Observable;

public interface ServerApi {
    @GET("")
    Observable<ByteArray> getResult(@Field("id") String id);
    //支持返回类型字节数组（需使用ByteArray），字符串（String），对象（将使用Json解释器生成对象）
}
