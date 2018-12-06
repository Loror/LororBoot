package com.loror.lororboot.bind;

public interface BindAble {
    /**
     * 找到bind触发,返回值控制是否拦截首次绑定显示
     */
    boolean onBindFind(BindHolder holder);

    /**
     * 查找Bind
     */
    void updateBind(Object tag);

    /**
     * 更新显示
     */
    void changeState(Runnable runnable);

    /**
     * 事件触发
     */
    void event(BindHolder holder, String oldValue, String newValue);

}
