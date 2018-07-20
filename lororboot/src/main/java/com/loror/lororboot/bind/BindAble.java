package com.loror.lororboot.bind;

import android.support.annotation.IdRes;

public interface BindAble {
    /**
     * 找到bind触发,返回值控制是否拦截首次绑定显示
     */
    boolean onBindFind(BindHolder holder);

    /**
     * 更新显示
     */
    void beginBind(Object tag);

    /**
     * 事件触发
     */
    void event(BindHolder holder, String oldValue, String newValue);

    /**
     * 修改数据并刷新
     */
    void setData(@IdRes int id, Object value);

    /**
     * 修改数据并刷新
     */
    void setData(String fieldName, Object value);

    /**
     * 刷新List
     */
    void notifyListDataChangeById(@IdRes int id);

}
