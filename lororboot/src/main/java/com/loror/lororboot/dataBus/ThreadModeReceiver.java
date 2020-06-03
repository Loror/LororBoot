package com.loror.lororboot.dataBus;

import android.content.Intent;
import android.os.Looper;

import com.loror.lororUtil.flyweight.ObjectPool;
import com.loror.lororboot.annotation.RunThread;
import com.loror.lororboot.annotation.DataRun;

import java.lang.reflect.Method;

public class ThreadModeReceiver {
    private DataBusReceiver receiver;
    @RunThread
    private int thread = RunThread.LASTTHREAD;
    private boolean sticky;

    public ThreadModeReceiver(DataBusReceiver receiver) {
        this.receiver = receiver;
        if (receiver != null) {
            try {
                Method method = receiver.getClass().getMethod("receiveData", String.class, Intent.class);
                DataRun dataRun = method.getAnnotation(DataRun.class);
                if (dataRun != null) {
                    this.thread = dataRun.thread();
                    this.sticky = dataRun.sticky();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void receiveData(final String name, final Intent data) {
        if (receiver != null) {
            switch (thread) {
                case RunThread.MAINTHREAD:
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        receiver.receiveData(name, data);
                    } else {
                        ObjectPool.getInstance().getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                receiver.receiveData(name, data);
                            }
                        });
                    }
                    break;
                case RunThread.NEWTHREAD:
                    new Thread() {
                        @Override
                        public void run() {
                            receiver.receiveData(name, data);
                        }
                    }.start();
                    break;
                case RunThread.LASTTHREAD:
                default:
                    receiver.receiveData(name, data);
                    break;
            }
        }
    }

    public boolean isSticky() {
        return sticky;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ThreadModeReceiver) {
            return receiver == ((ThreadModeReceiver) obj).receiver;
        }
        return false;
    }
}
