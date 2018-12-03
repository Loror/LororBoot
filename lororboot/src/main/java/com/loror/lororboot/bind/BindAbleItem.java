package com.loror.lororboot.bind;

import com.loror.lororUtil.view.ViewUtil;
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
        this.size = mark.size;
        //刷新显示并触发事件，解决控件复用问题
        BindUtils.initHolders(bindHolders, this, position);
    }

    @Override
    public void setState(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
        BindUtils.showBindHolders(bindHolders, this);
    }
}
