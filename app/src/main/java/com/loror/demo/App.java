package com.loror.demo;

import com.alibaba.fastjson.JSON;
import com.loror.lororboot.LororApplication;
import com.loror.lororboot.httpApi.ApiClient;
import com.loror.lororboot.httpApi.JsonParser;

public class App extends LororApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        //如要使用注解形式网络访问，必须实现Json解释器
        ApiClient.setJsonParser(new JsonParser() {
            @Override
            public Object jsonToObject(String json, Class<?> classType) {
                return JSON.parseObject(json, classType);
            }
        });
    }

    @Override
    protected Class<?> getIdClass() {
        return R.id.class;
    }
}
