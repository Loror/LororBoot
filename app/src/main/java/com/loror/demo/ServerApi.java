package com.loror.demo;

import com.loror.lororUtil.http.Responce;
import com.loror.lororboot.annotation.BaseUrl;
import com.loror.lororboot.annotation.DefaultParams;
import com.loror.lororboot.annotation.Param;
import com.loror.lororboot.annotation.GET;
import com.loror.lororboot.httpApi.Observable;

@BaseUrl("http://www.bejson.com/")
public interface ServerApi {
    @GET("")
    @DefaultParams(keys = "key", values = "123")//用于指定固定参数，key，value位置需一一对应
    Observable<Responce> getResult(@Param("id") String id);
    //支持返回类型原生responce（Responce），字符串（String），对象（将使用Json解释器生成对象）
    //请求类型@GET，@POST，@DELETE，@PUT
    //参数@Header，@Param，@ParamObject，@ParamJson
}
