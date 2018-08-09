package com.loror.lororboot.annotation;

import com.loror.lororboot.autoRun.AutoRunHolder;

public @interface AutoRunCallback {
    @RunThread int thread() default AutoRunHolder.MAINTHREAD;//运行线程
}
