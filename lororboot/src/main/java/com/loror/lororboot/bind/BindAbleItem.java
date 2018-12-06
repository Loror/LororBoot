package com.loror.lororboot.bind;

import android.support.annotation.LayoutRes;

import com.loror.lororUtil.view.ViewUtil;
import com.loror.lororboot.annotation.FrameCall;
import com.loror.lororboot.dataChange.DataChangeUtils;

import java.util.LinkedList;
import java.util.List;

public abstract class BindAbleItem implements DataChangeAble {
    private transient BindAble outBindAble;
    private transient List<BindHolder> bindHolders;
    private transient int position, size;

    public BindAble obtainOutBindAble() {
        return outBindAble;
    }

    public int obtainPosition() {
        return position;
    }

    public int obtainSize() {
        return size;
    }

    public int viewTypeCount() {
        return 1;
    }

    public int viewType() {
        return 0;
    }

    @LayoutRes
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
        DataChangeUtils.setData(id, value, position, bindHolders, this);
    }

    @Override
    public void notifyListDataChangeById(int id) {
        DataChangeUtils.notifyListDataChangeById(id, position, bindHolders, this);
    }

    @FrameCall
    protected final void refreshMark(BinderAdapter.Mark mark){
        this.position = mark.position;
        this.size = mark.size;
        this.outBindAble = mark.bindAble;
    }

    @Override
    public final void updateBind(Object tag) {
        BinderAdapter.Mark mark = (BinderAdapter.Mark) tag;
        List<BindHolder> bindHolders;
        if (mark.convertView.getTag() == null) {
            bindHolders = new LinkedList<>();
            BindUtils.findBindHolders(bindHolders, this, mark.convertView);
            mark.convertView.setTag(bindHolders);
        } else {
            bindHolders = (List<BindHolder>) mark.convertView.getTag();
        }
        ViewUtil.click(this, mark.convertView);
        this.bindHolders = bindHolders;
        //刷新显示并触发事件，解决控件复用问题
        BindUtils.initHolders(bindHolders, this, position);
    }

    @Override
    public void changeState(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
        BindUtils.showBindHolders(bindHolders, this);
    }
}
