package com.loror.lororboot.bind;

import android.view.View;

import com.loror.lororUtil.view.ViewUtil;
import com.loror.lororboot.annotation.Bind;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public abstract class BindAbleItem implements BindAble {
    private BindAble outBindAble;
    private List<BindHolder> bindHolders;

    public BindAble obtainOutBindAble() {
        return outBindAble;
    }

    public abstract int getLayout();

    @Override
    public void onBindFind(BindHolder holder) {

    }

    @Override
    public void event(BindHolder holder, String oldValue, String newValue) {

    }

    @Override
    public void setData(int id, Object value) {
        BindHolder holder = BindUtils.findHolderById(bindHolders, id);
        if (holder != null) {
            holder.getField().setAccessible(true);
            try {
                holder.getField().set(this, value);
                BindUtils.showBindHolder(holder, this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setData(String fieldName, Object value) {
        BindHolder holder = BindUtils.findHolderByName(bindHolders, fieldName);
        if (holder != null) {
            holder.getField().setAccessible(true);
            try {
                holder.getField().set(this, value);
                BindUtils.showBindHolder(holder, this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public final void beginBind(Object tag) {
        BinderAdapter.Mark mark = (BinderAdapter.Mark) tag;
        List<BindHolder> bindHolders;
        if (mark.convertView.getTag() == null) {
            bindHolders = new LinkedList<>();
            BindUtils.findBindHoldersOfItem(bindHolders, this, mark.convertView);
            mark.convertView.setTag(bindHolders);
        } else {
            bindHolders = (List<BindHolder>) mark.convertView.getTag();
        }
        outBindAble = mark.bindAble;
        ViewUtil.click(this, mark.convertView);
        mark.parent = null;
        resetHolder(bindHolders);
        BindUtils.showBindHolders(bindHolders, this);
        this.bindHolders = bindHolders;
    }

    /**
     * 刷新显示并触发事件，解决控件复用问题
     */
    private void resetHolder(List<BindHolder> bindHolders) {
        for (BindHolder bindHolder : bindHolders) {
            if (bindHolder.visibility != BindHolder.NOTCHANGE) {
                switch (bindHolder.visibility) {
                    case View.VISIBLE:
                        bindHolder.view.setVisibility(View.VISIBLE);
                        break;
                    case View.INVISIBLE:
                        bindHolder.view.setVisibility(View.INVISIBLE);
                        break;
                    case View.GONE:
                        bindHolder.view.setVisibility(View.GONE);
                        break;
                }
            }
            BindUtils.firstBinder(bindHolder, this);
        }
    }

}
