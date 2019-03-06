package com.loror.lororboot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//Receuver运行所在线程
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DataRun {
    @RunThread int thread() default RunThread.LASTTHREAD;//运行线程

    boolean sticky() default false;//是否粘性
}
