package com.loror.lororboot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//Receuver运行所在线程
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface WitchThread {
    @RunThread int value();
}
