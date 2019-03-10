package com.loror.lororboot.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IntDef({LaunchMode.STANDARD, LaunchMode.SINGLEINACTIVITY})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface LaunchMode {
    int value();//加载模式

    int STANDARD = 0;
    int SINGLEINACTIVITY = 1;
}
