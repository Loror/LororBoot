package com.loror.lororboot.annotation;

import android.support.annotation.IntDef;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({View.VISIBLE, View.INVISIBLE, View.GONE, Visibility.NOTCHANGE})
@Retention(RetentionPolicy.CLASS)
public @interface Visibility {

    int NOTCHANGE = -1;
    int VISIBLE = View.VISIBLE;
    int INVISIBLE = View.INVISIBLE;
    int GONE = View.GONE;
}
