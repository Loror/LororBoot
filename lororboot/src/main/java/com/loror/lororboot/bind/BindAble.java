package com.loror.lororboot.bind;

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

}
