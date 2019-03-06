package com.loror.lororboot.dataBus;

import android.content.Intent;

public class StickEvent {
    public String name;
    public Intent data;

    public StickEvent(String name, Intent data) {
        this.name = name;
        this.data = data;
    }
}
