package com.loror.lororboot.dataBus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import com.loror.lororboot.bind.BindAble;

public class DataBusUtil {
    private Context context;
    private BindAble bindAble;

    private BroadcastReceiver receiver;

    public DataBusUtil(Context context, BindAble bindAble) {
        this.context = context;
        this.bindAble = bindAble;
    }

    /**
     * 注册data监听
     */
    private void registerDataBusReceiverIfNot() {
        if (bindAble instanceof DataBusReceiver) {
            if (bindAble instanceof RemoteDataBusReceiver) {
                if (receiver == null) {
                    receiver = DataBus.createBroadcastReceiver((RemoteDataBusReceiver) bindAble);
                    context.registerReceiver(receiver, new IntentFilter("loror.RemoteDataBusReceiver"));
                }
            } else {
                DataBus.addReceiver((DataBusReceiver) bindAble);
            }
        }
    }

    /**
     * 注销data监听
     */
    private void unregisterDataBusReceiverIfRegistered() {
        if (bindAble instanceof DataBusReceiver) {
            if (bindAble instanceof RemoteDataBusReceiver) {
                if (receiver != null) {
                    context.unregisterReceiver(receiver);
                    receiver = null;
                }
            } else {
                DataBus.removeReceiver((DataBusReceiver) bindAble);
            }
        }
    }

    /**
     * 注册
     */
    public void register() {
        registerDataBusReceiverIfNot();
    }

    /**
     * 注销
     */
    public void unRegister() {
        unregisterDataBusReceiverIfRegistered();
    }
}
