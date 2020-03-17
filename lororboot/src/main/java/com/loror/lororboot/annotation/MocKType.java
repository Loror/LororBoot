package com.loror.lororboot.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({MocKType.AUTO, MocKType.ASSERT, MocKType.NET, MocKType.DATA})
@Retention(RetentionPolicy.CLASS)
public @interface MocKType {
    //MocK方式
    int AUTO = 0;
    int ASSERT = 1;
    int NET = 2;
    int DATA = 3;
}
