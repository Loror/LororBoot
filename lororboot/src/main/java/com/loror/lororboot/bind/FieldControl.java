package com.loror.lororboot.bind;

import java.lang.ref.WeakReference;

public class FieldControl {
    private BindAble bindAble;
    private WeakReference<BindHolder> holderWeakReference;

    public FieldControl(BindAble bindAble, BindHolder holder) {
        this.bindAble = bindAble;
        this.holderWeakReference = new WeakReference<>(holder);
    }

    public BindAble getBindAble() {
        return bindAble;
    }

    public void setField(Object value) {
        BindHolder holder = holderWeakReference.get();
        if (holder != null) {
            try {
                holder.field.set(bindAble, value);
                holder.compareTag = value;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
