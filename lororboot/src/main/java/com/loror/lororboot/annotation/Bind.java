package com.loror.lororboot.annotation;

import android.support.annotation.DrawableRes;

import com.loror.lororUtil.image.BitmapConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Bind {

    int id();//控件id

    String format() default "";//格式化显示

    String event() default "";//注册事件名

    String empty() default "";//空值显示

    @DrawableRes int imagePlace() default 0;//ImageView加载占位图

    int imageWidth() default 0;//指定ImageView缓存图宽度

    Class<? extends BitmapConverter> bitmapConverter() default BitmapConverter.class;//图片预处理

    @Visibility int visibility() default Visibility.NOTCHANGE;//控件显示状态

    boolean onlyEvent() default false;//是否不显示变量到控件只触发事件
}
