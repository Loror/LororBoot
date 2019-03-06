package com.loror.lororboot.dataBus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.loror.lororUtil.flyweight.ObjectPool;

import java.util.ArrayList;
import java.util.List;

public class DataBus {
    private static List<ThreadModeReceiver> receivers = new ArrayList<>();
    private static StickEvent stickEvent;//粘性事件，仅保留一个

    /**
     * 添加接收者
     */
    public static void addReceiver(DataBusReceiver receiver) {
        final ThreadModeReceiver threadModeReceiver = new ThreadModeReceiver(receiver);
        if (!receivers.contains(threadModeReceiver)) {
            receivers.add(threadModeReceiver);
            if (DataBus.stickEvent != null && threadModeReceiver.isSticky()) {
                ObjectPool.getInstance().getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        threadModeReceiver.receiveData(DataBus.stickEvent.name, DataBus.stickEvent.data);
                    }
                });
            }
        }
    }

    /**
     * 移除接收者
     */
    public static void removeReceiver(DataBusReceiver receiver) {
        receivers.remove(new ThreadModeReceiver(receiver));
    }

    /**
     * 发送数据给接收者
     */
    public static void notifyReceivers(String name, Intent data, Context context) {
        for (int i = 0; i < receivers.size(); i++) {
            receivers.get(i).receiveData(name, data);
        }
        DataBus.stickEvent = new StickEvent(name, data);
        try {
            if (data == null) {
                data = new Intent();
                data.putExtra("loror.RemoteDataBusReceiver.tag", "empty");
            } else {
                data = new Intent(data);
            }
            data.setAction("loror.RemoteDataBusReceiver");
            data.setPackage(context.getPackageName());
            data.putExtra("loror.RemoteDataBusReceiver.name", name);
            context.sendBroadcast(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建BroadcastReceiver
     */
    public static BroadcastReceiver createBroadcastReceiver(final RemoteDataBusReceiver dataBusReceiver) {
        final ThreadModeReceiver threadModeReceiver = new ThreadModeReceiver(dataBusReceiver);
        if (DataBus.stickEvent != null && threadModeReceiver.isSticky()) {
            ObjectPool.getInstance().getHandler().post(new Runnable() {
                @Override
                public void run() {
                    threadModeReceiver.receiveData(DataBus.stickEvent.name, DataBus.stickEvent.data);
                }
            });
        }
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String name = intent.getStringExtra("loror.RemoteDataBusReceiver.name");
                if (name != null) {
                    boolean isNullData = intent.getStringExtra("loror.RemoteDataBusReceiver.tag") != null;
                    DataBus.stickEvent = new StickEvent(name, isNullData ? null : intent);
                    if (!isNullData) {
                        intent.removeExtra("loror.RemoteDataBusReceiver.name");
                        intent.removeExtra("loror.RemoteDataBusReceiver.tag");
                    }
                    threadModeReceiver.receiveData(name, isNullData ? null : intent);
                }
            }
        };
    }
}
