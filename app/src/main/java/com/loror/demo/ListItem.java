package com.loror.demo;

import com.loror.lororboot.annotation.Bind;
import com.loror.lororboot.bind.BindAbleItem;

public class ListItem extends BindAbleItem {

    @Bind(id = R.id.text)
    public String text;

    @Override
    public int getLayout() {
        return R.layout.item_list_view;
    }
}
