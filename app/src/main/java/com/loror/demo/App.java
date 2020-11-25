package com.loror.demo;

import com.alibaba.fastjson.JSON;
import com.loror.lororUtil.http.api.ApiClient;
import com.loror.lororUtil.http.api.JsonParser;
import com.loror.lororUtil.http.api.TypeInfo;
import com.loror.lororboot.LororApplication;

public class App extends LororApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        //如要使用注解形式网络访问，必须实现Json解释器
        ApiClient.setJsonParser(new JsonParser() {
            @Override
            public Object jsonToObject(String json, TypeInfo typeInfo) {
                return typeInfo.isList() ? JSON.parseArray(json, typeInfo.getTypeClass()) : JSON.parseObject(json, typeInfo.getTypeClass());
            }

            @Override
            public String objectToJson(Object object) {
                return JSON.toJSONString(object);
            }
        });
    }

    @Override
    protected Class<?> getIdClass() {
        return R.id.class;
    }
}
