package com.loror.lororboot.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({RunTime.TOP, RunTime.BEFOREMETHOD, RunTime.AFTERMETHOD})
@Retention(RetentionPolicy.CLASS)
public @interface RunTime {

    //顶点
    int TOP = 0;

    //关联型运行
    int BEFOREMETHOD = 10;
    int AFTERMETHOD = 11;
}
