package com.loror.lororboot.httpApi;

public interface JsonParser {
    Object jsonToObject(String json, Class<?> classType);

    String objectToJson(Object object);
}
