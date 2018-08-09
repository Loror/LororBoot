package com.loror.lororboot.annotation;

import android.support.annotation.IntDef;
import android.view.View;

import com.loror.lororboot.bind.BindHolder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({View.VISIBLE, View.INVISIBLE, View.GONE, BindHolder.NOTCHANGE})
@Retention(RetentionPolicy.CLASS)
public @interface Visibility {
}
