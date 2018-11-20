package com.loror.demo;

import android.os.Bundle;
import android.view.View;

import com.loror.lororUtil.view.Click;
import com.loror.lororboot.startable.LororActivity;

public class SecondActivity extends LororActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }

    @Click(id = R.id.send)
    public void send(View v) {
        sendDataToBus("toast", "打印消息");
    }
}
