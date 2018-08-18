package com.loror.lororboot.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({RunThread.CURENTTHREAD, RunThread.MAINTHREAD, RunThread.NEWTHREAD})
@Retention(RetentionPolicy.CLASS)
public @interface RunThread {
    //运行所在线程
    int CURENTTHREAD = 0;
    int MAINTHREAD = 1;
    int NEWTHREAD = 2;
}
