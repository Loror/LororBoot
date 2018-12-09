package com.loror.lororboot.bind;

import java.lang.reflect.Field;

public class FieldControl {
    private BindAble bindAble;
    private Field field;

    public FieldControl(BindAble bindAble, Field field) {
        this.bindAble = bindAble;
        this.field = field;
    }

    public BindAble getBindAble() {
        return bindAble;
    }

    public void setField(Object value)  {
        try {
            field.set(bindAble, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
