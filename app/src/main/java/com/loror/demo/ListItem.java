package com.loror.demo;

import com.loror.lororboot.annotation.Bind;
import com.loror.lororboot.bind.BindAbleItem;

public class ListItem extends BindAbleItem {

    @Bind(id = R.id.text)
    public String text;

    @Override
    public int viewType() {
        return obtainPosition() % 2 == 0 ? 0 : 1;
    }

    @Override
    public int viewTypeCount() {
        return 2;
    }

    @Override
    public int getLayout() {
        return obtainPosition() % 2 == 0 ? R.layout.item_list_view : R.layout.item_list_view_2;
    }
}
