package com.loror.lororboot.dataBus;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class DataBus {
    private static List<DataBusReceiver> receivers = new ArrayList<>();
    private static int remoteReceiver;

    /**
     * RemoteReceiver计数增加
     */
    public static void addRemoteReceiverCount() {
        remoteReceiver++;
    }

    /**
     * RemoteReceiver计数减少
     */
    public static void reduceRemoteReceiverCount() {
        remoteReceiver--;
    }

    /**
     * 添加接收者
     */
    public static void addReceiver(DataBusReceiver receiver) {
        if (!receivers.contains(receiver)) {
            receivers.add(receiver);
        }
    }

    /**
     * 移除接收者
     */
    public static void removeReceiver(DataBusReceiver receiver) {
        receivers.remove(receiver);
    }

    /**
     * 发送数据给接收者
     */
    public static void notifyReceivers(String name, Intent data, Context context) {
        for (int i = 0; i < receivers.size(); i++) {
            receivers.get(i).receiveData(name, data);
        }
        if (remoteReceiver > 0) {
            data.setAction("loror.RemoteDataBusReceiver");
            data.putExtra("loror.RemoteDataBusReceiver.name", name);
            context.sendBroadcast(data);
        }
    }
}
