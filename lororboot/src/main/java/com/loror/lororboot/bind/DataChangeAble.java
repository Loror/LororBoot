package com.loror.lororboot.bind;

import android.support.annotation.IdRes;

public interface DataChangeAble extends BindAble {
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
