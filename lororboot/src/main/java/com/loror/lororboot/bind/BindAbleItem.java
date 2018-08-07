package com.loror.lororboot.bind;

import android.view.View;
import android.widget.ListView;

import com.loror.lororUtil.view.ViewUtil;
import com.loror.lororboot.annotation.Bind;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public abstract class BindAbleItem implements BindAble, DataChangeAble {
    private transient BindAble outBindAble;
    private transient List<BindHolder> bindHolders;
    private transient int position;

    public BindAble obtainOutBindAble() {
        return outBindAble;
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
            if (bindHolder.getView() instanceof ListView) {
                BinderAdapter adapter = (BinderAdapter) bindHolder.getView().getTag(bindHolder.getView().getId());
                adapter.setShowEmpty(true);
            }
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
            BindUtils.findBindHoldersOfItem(bindHolders, this, mark.convertView);
            mark.convertView.setTag(bindHolders);
        } else {
            bindHolders = (List<BindHolder>) mark.convertView.getTag();
        }
        outBindAble = mark.bindAble;
        ViewUtil.click(this, mark.convertView);
        mark.parent = null;
        resetHolder(bindHolders, mark.position);
        this.bindHolders = bindHolders;
        this.position = mark.position;
    }

    /**
     * 刷新显示并触发事件，解决控件复用问题
     */
    private void resetHolder(List<BindHolder> bindHolders, int position) {
        for (BindHolder bindHolder : bindHolders) {
            bindHolder.setTag(position);
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
            BindUtils.specialBinder(bindHolder, bindHolder.view, this);
            if (onBindFind(bindHolder)) {
                Object volume = BindUtils.getVolume(bindHolder, this);
                if (volume instanceof List) {
                    bindHolder.compareTag = ((List) volume).size();
                } else {
                    bindHolder.compareTag = volume;
                }
            } else {
                BindUtils.firstBinder(bindHolder, this);
            }
        }
    }

}
