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

    public void run(@RunThread int thread, final int delay, final Runnable runnable) {
        Handler handler = LororActivity.handler;
        if (thread == RunThread.MAINTHREAD) {
            if (delay > 0) {
                handler.postDelayed(runnable, delay);
            } else {
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    runnable.run();
                } else {
                    handler.post(runnable);
                }
            }
        } else if (thread == RunThread.NEWTHREAD) {
            if (delay > 0) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runnable.run();
                    }
                }.start();
            } else {
                new Thread(runnable).start();
            }
        } else {
            if (delay > 0) {
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    handler.postDelayed(runnable, delay);
                } else {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runnable.run();
                }
            } else {
                runnable.run();
            }
        }
    }

    /**
     * 注册data监听
     */
    private void registerDataBusReceiverIfNot() {
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
    private void unregisterDataBusReceiverIfRegistered() {
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
        registerDataBusReceiverIfNot();
    }

    public void onResumeOrStart() {
        if (createState == 1) {
            createState = 2;
            AutoRunUtil.runAutoRunHolderByPenetration(RunTime.AFTERONCREATE, autoRunHolders, autoRunAble);
        }
        registerDataBusReceiverIfNot();
    }

    public void onStop() {
        unregisterDataBusReceiverIfRegistered();
    }

    public void onDestroy() {
        AutoRunUtil.runAutoRunHolderByPenetration(RunTime.BEFOREONDESTROY, autoRunHolders, autoRunAble);
        release();
    }

    public void release() {
        autoRunHolders.clear();
        unregisterDataBusReceiverIfRegistered();
    }
}
