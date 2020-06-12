package com.loror.lororboot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Aop {

    @RunTime int when() default RunTime.TOP;//执行时机

    String relationMethod() default "";//指定BEFOREMETHOD或AFTERMETHOD关联的方法

    @RunThread int thread() default RunThread.LASTTHREAD;//运行线程

    int delay() default 0;//延时
}
