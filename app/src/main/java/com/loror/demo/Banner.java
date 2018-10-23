package com.loror.demo;

import com.loror.lororboot.annotation.Bind;
import com.loror.lororboot.bind.BindAbleItem;
import com.loror.lororboot.bind.BindHolder;

public class Banner extends BindAbleItem {
    @Bind(id = R.id.image)
    public String image;
    @Bind(id = R.id.text)
    public String pos;

    public Banner(String image) {
        this.image = image;
    }

    @Override
    public boolean onBindFind(BindHolder holder) {
        switch (holder.getView().getId()) {
            case R.id.text:
                pos = obtainPosition() + "";
                break;
        }
        return super.onBindFind(holder);
    }

    @Override
    public int getLayout() {
        return R.layout.item_banner;
    }
}
