package com.loror.demo;

import android.view.View;

import com.loror.lororUtil.view.Click;
import com.loror.lororboot.annotation.Bind;
import com.loror.lororboot.annotation.Connection;
import com.loror.lororboot.bind.BindAbleItem;

public class ListItem extends BindAbleItem {

    @Bind(id = R.id.text)
    public String text;

    @Connection
    private transient OnItemClickConnect onItemClickConnect;

    @Override
    public int viewType() {
        return obtainPosition() % 2;
    }

    @Override
    public int viewTypeCount() {
        return 2;
    }

    @Override
    public int getLayout(int viewType) {
        return viewType == 0 ? R.layout.item_list_view : R.layout.item_list_view_2;
    }

    @Click(id = R.id.text)
    public void text(View v) {
        if (onItemClickConnect != null) {
            onItemClickConnect.onItemClick(0, obtainPosition());
        }
    }
}
