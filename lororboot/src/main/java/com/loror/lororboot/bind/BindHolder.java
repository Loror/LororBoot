package com.loror.lororboot.bind;

import android.view.View;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BindHolder {
    public static final int NOTCHANGE = -1;

    protected View view;
    protected String format;
    protected Field field;
    protected String event;
    protected String empty;
    protected int visibility = NOTCHANGE;
    protected int imagePlace;
    protected int imageWidth;
    protected boolean onlyEvent;
    protected Object tag;

    public View getView() {
        return view;
    }

    public Field getField() {
        return field;
    }

    public String getEvent() {
        return event;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public void resetListTag() {
        if (field.getType() == List.class || field.getType() == ArrayList.class) {
            tag = -1;
        }
    }
}
