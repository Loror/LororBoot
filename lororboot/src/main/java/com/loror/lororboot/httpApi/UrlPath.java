package com.loror.lororboot.httpApi;

import com.loror.lororUtil.convert.UrlUtf8Util;

public class UrlPath {
    protected String name;
    protected String value;

    public UrlPath(String name, Object value) {
        this.name = name;
        this.value = value == null ? "" : UrlUtf8Util.toUrlString(String.valueOf(value));
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
