package com.loror.lororboot.annotation;

import android.support.annotation.IntDef;

import com.loror.lororboot.autoRun.AutoRunHolder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({AutoRunHolder.MAINTHRED, AutoRunHolder.NEWTHRED})
@Retention(RetentionPolicy.CLASS)
public @interface RunThread {
}
