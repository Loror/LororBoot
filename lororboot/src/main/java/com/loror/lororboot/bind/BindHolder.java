package com.loror.lororboot.bind;

import android.view.View;

import com.loror.lororUtil.image.BitmapConverter;
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
    protected int errorPlace;
    protected int imageWidth;
    protected boolean gif;
    protected BitmapConverter bitmapConverter;
    protected boolean disableItem;
    protected boolean onlyEvent;
    protected Object compareTag;
    private Object tag;
    protected boolean isFirst = true;
    protected List<Field> connections;
    protected List<Field> unions;

    public View getView() {
        return view;
    }

    public Field getField() {
        return field;
    }

    public String getEvent() {
        return event;
    }

    public void setBitmapConverter(BitmapConverter bitmapConverter) {
        this.bitmapConverter = bitmapConverter;
    }

    public BitmapConverter getBitmapConverter() {
        return bitmapConverter;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public void setEmpty(String empty) {
        this.empty = empty;
    }

    public String getEmpty() {
        return empty;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    public void resetCompareTag() {
        if (field.getType() == List.class || field.getType() == ArrayList.class) {
            compareTag = -1;
        } else {
            compareTag = null;
        }
    }

    protected String union(BindAble bindAble, String vol) {
        if (unions != null) {
            for (Field field : unions) {
                String name = field.getName();
                try {
                    field.setAccessible(true);
                    Object value = field.get(bindAble);
                    if (value == null) {
                        vol = vol.replace("${" + name + "}", "");
                    } else {
                        vol = vol.replace("${" + name + "}", value.toString());
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return vol;
    }

    protected BindHolder cloneOne() {
        BindHolder clone = new BindHolder();
        clone.view = view;
        clone.field = field;
        clone.format = format;
        clone.event = event;
        clone.empty = empty;
        clone.visibility = visibility;
        clone.imagePlace = imagePlace;
        clone.errorPlace = errorPlace;
        clone.imageWidth = imageWidth;
        clone.gif = gif;
        clone.bitmapConverter = bitmapConverter;
        clone.disableItem = disableItem;
        clone.onlyEvent = onlyEvent;
        clone.isFirst = true;
        clone.unions = unions;
        return clone;
    }
}
