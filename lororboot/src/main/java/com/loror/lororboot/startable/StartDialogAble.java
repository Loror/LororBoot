package com.loror.lororboot.startable;

import android.content.Intent;

public interface StartDialogAble {
    /**
     * 打开dialog
     */
    void startDialog(Intent intent);

    /**
     * 打开dialog并请求返回
     */
    void startDialogForResult(Intent intent, final int requestCode);
}
