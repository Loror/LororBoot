package com.loror.lororboot.bind;

import android.view.View;

import com.loror.lororboot.annotation.Visibility;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BindHolder {

    protected View view;
    protected String format;
    protected Field field;
    protected String event;
    protected String empty;
    protected int visibility = Visibility.NOTCHANGE;
    protected int imagePlace;
    protected int imageWidth;
    protected boolean disableItem;
    protected boolean onlyEvent;
    protected Object compareTag;
    private Object tag;

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

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    public void resetListCompareTag() {
        if (field.getType() == List.class || field.getType() == ArrayList.class) {
            compareTag = -1;
        }
    }

    protected BindHolder cloneOne(){
        BindHolder clone = new BindHolder();
        clone.view = view;
        clone.field = field;
        clone.format = format;
        clone.event = event;
        clone.empty = empty;
        clone.visibility = visibility;
        clone.imagePlace = imagePlace;
        clone.imageWidth = imageWidth;
        clone.onlyEvent = onlyEvent;
        clone.disableItem = disableItem;
        return clone;
    }
}
