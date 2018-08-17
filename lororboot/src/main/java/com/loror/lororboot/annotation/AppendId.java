package com.loror.lororboot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface AppendId {

    int[] id();//控件id

    boolean onlyEvent() default false;//是否不显示变量到控件只触发事件
}
