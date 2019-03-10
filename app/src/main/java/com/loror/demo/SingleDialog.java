package com.loror.demo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.EditText;

import com.loror.lororboot.annotation.LaunchMode;
import com.loror.lororboot.startable.LororDialog;

@LaunchMode(LaunchMode.SINGLEINACTIVITY)
public class SingleDialog extends LororDialog {
    public SingleDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_single);
    }

    @Override
    public void show() {
        setView(new EditText(getContext()));
        super.show();
    }
}
