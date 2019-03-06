package com.loror.lororboot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RequestPermission {
    String[] value();//需要申请的权限

    @RequestTime int when() default RequestTime.ONCREATE;//申请时间

    boolean requestAnyway() default false;//是否以前被拒绝仍然申请
}
