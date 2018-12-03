package com.loror.lororboot;

import android.app.Application;

import com.loror.lororUtil.view.ViewUtil;

public abstract class LororApplication extends Application {

    public static boolean NoImageSdCardCache = false;

    @Override
    public void onCreate() {
        super.onCreate();
        ViewUtil.setGlobalIdClass(getIdClass());
    }

    protected abstract Class<?> getIdClass();

}
