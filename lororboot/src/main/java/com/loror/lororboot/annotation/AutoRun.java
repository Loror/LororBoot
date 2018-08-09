package com.loror.lororboot.annotation;

import com.loror.lororboot.autoRun.AutoRunHolder;

public @interface AutoRun {

    @RunTime int when();//执行时机

    String relationMethod() default "";//指定BEFOREMETHOD或AFTERMETHOD关联的方法

    @RunThread int thread() default AutoRunHolder.MAINTHRED;//运行线程

    String callbackMethod() default "";//运行结束后回调方法名，回调方法必须@AutoRunCallback修饰
}
