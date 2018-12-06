package com.loror.lororboot.bind;

import com.loror.lororboot.annotation.FrameCall;

public interface BindAble {
    /**
     * 找到bind触发,返回值控制是否拦截首次绑定显示
     */
    boolean onBindFind(BindHolder holder);

    /**
     * 查找Bind
     */
    @FrameCall
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
