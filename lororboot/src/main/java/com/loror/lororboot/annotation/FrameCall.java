package com.loror.lororboot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 框架中使用的方法，外部请勿调用
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface FrameCall {
}
