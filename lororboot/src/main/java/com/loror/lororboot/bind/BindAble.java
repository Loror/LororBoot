package com.loror.lororboot.bind;

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

}
