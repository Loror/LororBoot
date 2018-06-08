package com.loror.lororboot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ItemClick {
    int id() default 0;//控件id

    long clickSpace() default 0;//点击允许间隔
}
