package com.loror.lororboot.httpApi;

public class Path {
    protected String name;
    protected String value;

    public Path(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
