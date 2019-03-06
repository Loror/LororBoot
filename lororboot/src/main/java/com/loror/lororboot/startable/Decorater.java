package com.loror.lororboot.startable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import com.loror.lororboot.annotation.RunThread;
import com.loror.lororboot.annotation.RunTime;
import com.loror.lororboot.autoRun.AutoRunAble;
import com.loror.lororboot.autoRun.AutoRunHolder;
import com.loror.lororboot.autoRun.AutoRunUtil;
import com.loror.lororboot.dataBus.DataBus;
import com.loror.lororboot.dataBus.DataBusReceiver;
import com.loror.lororboot.dataBus.RemoteDataBusReceiver;

import java.util.ArrayList;
import java.util.List;

public class Decorater {
    private Context context;
    private AutoRunAble autoRunAble;

    private int createState;
    private List<AutoRunHolder> autoRunHolders = new ArrayList<>();
    private BroadcastReceiver receiver;

    public Decorater(Context context, AutoRunAble autoRunAble) {
        this.context = context;
        this.autoRunAble = autoRunAble;
        autoRunHolders.clear();
        autoRunHolders.addAll(AutoRunUtil.findAutoRunHolders(autoRunAble));
    }

    public void runAutoRunByPenetration(String methodName) {
        AutoRunUtil.runAutoRunHolderByPenetration(methodName, autoRunHolders, autoRunAble);
    }

    public void run(@RunThread int thread, Runnable runnable, Handler handler) {
        if (thread == RunThread.MAINTHREAD) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                runnable.run();
            } else {
                if (handler == null) {
                    handler = new Handler();
                }
                handler.post(runnable);
            }
        } else if (thread == RunThread.NEWTHREAD) {
            new Thread(runnable).start();
        } else {
            runnable.run();
        }
    }

    /**
     * 注册data监听
     */
    private void registerDataBusReceiver() {
        if (autoRunAble instanceof DataBusReceiver) {
            if (autoRunAble instanceof RemoteDataBusReceiver) {
                if (receiver == null) {
                    receiver = DataBus.createBroadcastReceiver((RemoteDataBusReceiver) autoRunAble);
                    context.registerReceiver(receiver, new IntentFilter("loror.RemoteDataBusReceiver"));
                }
            } else {
                DataBus.addReceiver((DataBusReceiver) autoRunAble);
            }
        }
    }

    /**
     * 注销data监听
     */
    private void unregisterDataBusReceiver() {
        if (autoRunAble instanceof DataBusReceiver) {
            if (autoRunAble instanceof RemoteDataBusReceiver) {
                if (receiver != null) {
                    context.unregisterReceiver(receiver);
                    receiver = null;
                }
            } else {
                DataBus.removeReceiver((DataBusReceiver) autoRunAble);
            }
        }
    }

    /**
     * 生命周期
     */
    public void onCreate() {
        createState = 1;
        registerDataBusReceiver();
    }

    public void onResume() {
        if (createState == 1) {
            createState = 2;
            AutoRunUtil.runAutoRunHolderByPenetration(RunTime.AFTERONCREATE, autoRunHolders, autoRunAble);
        }
    }

    public void onStart() {
        if (createState == 1) {
            createState = 2;
            AutoRunUtil.runAutoRunHolderByPenetration(RunTime.AFTERONCREATE, autoRunHolders, autoRunAble);
        }
        registerDataBusReceiver();
    }

    public void onStop() {
        unregisterDataBusReceiver();
    }

    public void onDestroy() {
        AutoRunUtil.runAutoRunHolderByPenetration(RunTime.BEFOREONDESTROY, autoRunHolders, autoRunAble);
        autoRunHolders.clear();
        unregisterDataBusReceiver();
    }

    public void release() {
        autoRunHolders.clear();
        unregisterDataBusReceiver();
    }
}
