package com.loror.lororboot.bind;

import android.support.annotation.IdRes;

public interface BindAble {
    /**
     * 找到bind触发
     */
    void onBindFind(BindHolder holder);

    /**
     * 更新显示
     */
    void beginBind(Object tag);

    /**
     * 事件触发
     */
    void event(BindHolder holder, String oldValue, String newValue);


    void setData(@IdRes int id, Object value);

    void setData(String fieldName, Object value);

}
