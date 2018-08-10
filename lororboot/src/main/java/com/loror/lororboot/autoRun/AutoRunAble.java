package com.loror.lororboot.autoRun;

import com.loror.lororboot.annotation.RunThread;

public interface AutoRunAble {

    void collectAutoRun();

    void run(@RunThread int thread, Runnable runnable);
}
