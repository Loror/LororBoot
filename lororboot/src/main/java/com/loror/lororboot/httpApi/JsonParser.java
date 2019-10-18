package com.loror.lororboot.httpApi;

public interface JsonParser {
    Object jsonToObject(String json, TypeInfo typeInfo);

    String objectToJson(Object object);
}
