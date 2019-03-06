package com.loror.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.loror.lororUtil.view.Click;
import com.loror.lororUtil.view.Find;
import com.loror.lororUtil.view.ViewUtil;
import com.loror.lororboot.annotation.DataRun;
import com.loror.lororboot.annotation.RunThread;
import com.loror.lororboot.dataBus.RemoteDataBusReceiver;
import com.loror.lororboot.startable.LororActivity;

public class SecondActivity extends LororActivity implements RemoteDataBusReceiver {

    @Find
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        ViewUtil.find(this);
    }

    @Click(id = R.id.send)
    public void send(View v) {
        Intent data = new Intent();
        data.putExtra("msg", "打印消息");
        sendDataToBus("toast", data);
    }

    @Override
    @DataRun(thread = RunThread.MAINTHREAD, sticky = true)
    public void receiveData(String name, Intent data) {
        if ("SecondActivity.sticky".equals(name)) {
            text.setText(data.getStringExtra("data"));
        }
    }
}
