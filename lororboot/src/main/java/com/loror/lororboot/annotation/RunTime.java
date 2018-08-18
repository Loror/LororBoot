package com.loror.lororboot.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({RunTime.AFTERONCREATE, RunTime.USERCALL, RunTime.BEFOREMETHOD, RunTime.AFTERMETHOD})
@Retention(RetentionPolicy.CLASS)
public @interface RunTime {
    //切入点
    int USERCALL = 0;
    int AFTERONCREATE = 1;

    //关联型运行
    int BEFOREMETHOD = 2;
    int AFTERMETHOD = 3;
}
