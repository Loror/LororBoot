package com.loror.demo;

import com.loror.lororUtil.http.Responce;
import com.loror.lororboot.httpApi.Field;
import com.loror.lororboot.httpApi.GET;
import com.loror.lororboot.httpApi.Observable;

public interface ServerApi {
    @GET("")
    Observable<Responce> getResult(@Field("id") String id);
    //支持返回类型原生responce（Responce），字符串（String），对象（将使用Json解释器生成对象）
}
