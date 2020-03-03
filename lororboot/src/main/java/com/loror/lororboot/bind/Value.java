package com.loror.lororboot.bind;

public class Value {
    public String format;
    public String empty;
    public Object value;

    public Value(BindHolder holder, Object value) {
        this.format = holder.format;
        this.empty = holder.empty;
        this.value = value;
    }
}
