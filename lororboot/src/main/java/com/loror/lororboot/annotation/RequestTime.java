package com.loror.lororboot.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({RequestTime.ONCREATE, RequestTime.ONRESUME})
@Retention(RetentionPolicy.CLASS)
public @interface RequestTime {
    int ONCREATE = 0;
    int ONRESUME = 1;
}
