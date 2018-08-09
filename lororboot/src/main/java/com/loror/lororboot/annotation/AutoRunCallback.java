package com.loror.lororboot.annotation;

import com.loror.lororboot.autoRun.AutoRunHolder;

public @interface AutoRunCallback {
    @RunThread int thread() default AutoRunHolder.MAINTHRED;//运行线程
}
