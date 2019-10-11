package com.loror.lororboot.httpApi;

public interface JsonParser {
    Object jsonToObject(String json, Class<?> classType, boolean array);

    String objectToJson(Object object);
}
