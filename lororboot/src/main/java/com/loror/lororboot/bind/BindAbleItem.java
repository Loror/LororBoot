package com.loror.lororboot.bind;

import com.loror.lororUtil.view.ViewUtil;

import java.util.LinkedList;
import java.util.List;

public abstract class BindAbleItem implements BindAble, DataChangeAble {
    private transient BindAble outBindAble;
    private transient List<BindHolder> bindHolders;
    private transient int position;

    public BindAble obtainOutBindAble() {
        return outBindAble;
    }

    public int obtainPosition() {
        return position;
    }

    public abstract int getLayout();

    @Override
    public boolean onBindFind(BindHolder holder) {
        return false;
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
                if (holder.getTag() instanceof Integer && (Integer) holder.getTag() == position) {
                    BindUtils.showBindHolder(holder, this);
                }
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
                if (holder.getTag() instanceof Integer && (Integer) holder.getTag() == position) {
                    BindUtils.showBindHolder(holder, this);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void notifyListDataChangeById(int id) {
        BindHolder bindHolder = BindUtils.findHolderById(bindHolders, id);
        if (bindHolder != null) {
            bindHolder.resetListCompareTag();
            if (bindHolder.getTag() instanceof Integer && (Integer) bindHolder.getTag() == position) {
                BindUtils.showBindHolder(bindHolder, this);
            }
        }
    }

    @Override
    public final void beginBind(Object tag) {
        BinderAdapter.Mark mark = (BinderAdapter.Mark) tag;
        List<BindHolder> bindHolders;
        if (mark.convertView.getTag() == null) {
            bindHolders = new LinkedList<>();
            BindUtils.findBindHolders(bindHolders, this, mark.convertView);
            mark.convertView.setTag(bindHolders);
        } else {
            bindHolders = (List<BindHolder>) mark.convertView.getTag();
        }
        outBindAble = mark.bindAble;
        ViewUtil.click(this, mark.convertView);
        mark.parent = null;
        this.bindHolders = bindHolders;
        this.position = mark.position;
        //刷新显示并触发事件，解决控件复用问题
        BindUtils.initHolders(bindHolders, this, mark.position);
    }

}
