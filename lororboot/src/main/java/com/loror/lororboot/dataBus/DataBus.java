package com.loror.lororboot.dataBus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.loror.lororUtil.flyweight.ObjectPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataBus {

    private static int stickCount = 10;
    private static List<ThreadModeReceiver> receivers = new ArrayList<>();
    private static List<StickEvent> stickEvents = new CopyOnWriteArrayList<>();//粘性事件，仅保留stickCount个

    /**
     * 设置粘性事件个数
     */
    public static void setStickCount(int stickCount) {
        DataBus.stickCount = stickCount;
    }

    /**
     * 添加接收者
     */
    public static void addReceiver(DataBusReceiver receiver) {
        final ThreadModeReceiver threadModeReceiver = new ThreadModeReceiver(receiver);
        if (!receivers.contains(threadModeReceiver)) {
            receivers.add(threadModeReceiver);
            if (DataBus.stickEvents.size() > 0 && threadModeReceiver.isSticky()) {
                ObjectPool.getInstance().getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        for (StickEvent stickEvent : stickEvents) {
                            threadModeReceiver.receiveData(stickEvent.name, stickEvent.data);
                        }
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

        addStickEvent(new StickEvent(name, data));
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
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String name = intent.getStringExtra("loror.RemoteDataBusReceiver.name");
                if (name != null) {
                    boolean isNullData = intent.getStringExtra("loror.RemoteDataBusReceiver.tag") != null;
                    if (!isNullData) {
                        intent.removeExtra("loror.RemoteDataBusReceiver.name");
                        intent.removeExtra("loror.RemoteDataBusReceiver.tag");
                    }
                    addStickEvent(new StickEvent(name, isNullData ? null : intent));
                    threadModeReceiver.receiveData(name, isNullData ? null : intent);
                }
            }
        };
    }

    /**
     * 添加粘性事件
     */
    private static synchronized void addStickEvent(StickEvent stickEvent) {
        DataBus.stickEvents.add(stickEvent);
        while (DataBus.stickEvents.size() > stickCount) {
            DataBus.stickEvents.remove(DataBus.stickEvents.size() - 1);
        }
    }
}
