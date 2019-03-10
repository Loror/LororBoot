package com.loror.lororboot.bind;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.loror.lororboot.R;
import com.loror.lororboot.views.EmptyLayout;

import java.util.List;

public class BinderAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List list;
    private boolean showEmpty;
    private String emptyString = "暂无数据";
    private BindAble bindAble;
    private boolean itemEnable = true;

    public BinderAdapter(Context context, List list, BindAble bindAble) {
        this.context = context;
        this.list = list;
        this.bindAble = bindAble;
        inflater = LayoutInflater.from(context);
    }

    public Context getContext() {
        return context;
    }

    @Override
    public int getViewTypeCount() {
        int count = super.getViewTypeCount();
        if (list.size() > 0) {
            Object item = list.get(0);
            if (item instanceof BindAbleItem && ((BindAbleItem) item).viewTypeCount() > 1) {
                count = ((BindAbleItem) item).viewTypeCount();
            }
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        int type = super.getItemViewType(position);
        if (list.size() > 0) {
            Object item = list.get(position);
            if (item instanceof BindAbleItem && ((BindAbleItem) item).viewTypeCount() > 1) {
                BindAbleItem bindAbleItem = (BindAbleItem) item;
                Mark mark = new Mark();
                mark.bindAble = this.bindAble;
                mark.position = position;
                mark.size = list.size();
                bindAbleItem.refreshMark(mark);
                type = bindAbleItem.viewType();
            }
        }
        return type;
    }

    public void setItemEnable(boolean itemEnable) {
        this.itemEnable = itemEnable;
    }

    @Override
    public boolean isEnabled(int position) {
        return itemEnable;
    }

    public int count() {
        return list.size();
    }

    @Override
    public int getCount() {
        return showEmptyView() ? (count() == 0 ? 1 : count()) : count();
    }

    @Override
    public Object getItem(int position) {
        return count() == 0 ? null : list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (count() == 0 && showEmptyView()) {
            convertView = inflater.inflate(R.layout.holder_base_empty, parent, false);
            TextView textView = (TextView) convertView.findViewById(R.id.tv_empty);
            textView.setText(emptyString());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        } else {
            Object item = list.get(position);
            if (!(item instanceof BindAbleItem)) {
                throw new IllegalStateException("AbsListView只支持绑定List<? extends BindAbleItem>类型");
            }
            BindAbleItem bindAbleItem = (BindAbleItem) item;
            Mark mark = new Mark();
            mark.bindAble = this.bindAble;
            mark.position = position;
            mark.size = list.size();
            bindAbleItem.refreshMark(mark);
            if (convertView == null || convertView instanceof EmptyLayout) {
                convertView = inflater.inflate(bindAbleItem.getLayout(), parent, false);
            }
            bindAbleItem.updateBind(convertView);
        }
        return convertView;
    }

    public void setShowEmpty(boolean showEmpty) {
        this.showEmpty = showEmpty;
    }

    private boolean showEmptyView() {
        return showEmpty;
    }

    public void setEmptyString(String emptyString) {
        this.emptyString = emptyString;
    }

    public String emptyString() {
        return emptyString;
    }

    static class Mark {
        int position;
        int size;
        BindAble bindAble;
    }
}
