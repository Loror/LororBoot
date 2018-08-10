package com.loror.lororboot.annotation;

import com.loror.lororboot.autoRun.AutoRunHolder;

public @interface AutoRun {

    @RunTime int when();//执行时机

    String name();//切入点名字

    String relationMethod() default "";//指定BEFOREMETHOD或AFTERMETHOD关联的方法

    @RunThread int thread() default AutoRunHolder.MAINTHREAD;//运行线程
}
