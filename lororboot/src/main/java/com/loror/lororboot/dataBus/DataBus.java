package com.loror.lororboot.dataBus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class DataBus {
    private static List<DataBusReceiver> receivers = new ArrayList<>();

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
    public static BroadcastReceiver createBroadcastReceiver(final DataBusReceiver dataBusReceiver) {
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
                    dataBusReceiver.receiveData(name, isNullData ? null : intent);
                }
            }
        };
    }
}
