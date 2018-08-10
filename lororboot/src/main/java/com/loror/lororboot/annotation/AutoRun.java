package com.loror.lororboot.annotation;

import com.loror.lororboot.autoRun.AutoRunHolder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AutoRun {

    @RunTime int when();//执行时机

    String relationMethod() default "";//指定BEFOREMETHOD或AFTERMETHOD关联的方法

    @RunThread int thread() default AutoRunHolder.MAINTHREAD;//运行线程
}
