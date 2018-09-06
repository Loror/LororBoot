package com.loror.lororboot.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.loror.lororUtil.view.ViewUtil;
import com.loror.lororboot.R;
import com.loror.lororboot.views.EmptyLayout;

/**
 * Created by Loror on 2017/7/6.
 */

public abstract class HolderbaseAdapter<T extends HolderbaseAdapter.BaseViewHolder> extends BaseAdapter {
    private LayoutInflater inflater;
    private Context context;
    private boolean notifyed;

    public HolderbaseAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public Context getContext() {
        return context;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @LayoutRes
    public abstract int getLayout(int position);

    public abstract T newHolder();

    public abstract void bindView(T baseViewHolder, int position);

    public abstract int count();

    public boolean showEmptyView() {
        return false;
    }

    public String emptyString() {
        return "暂无数据";
    }

    public View emptyView(LayoutInflater inflater, ViewGroup parent) {
        return null;
    }

    @Override
    public final int getCount() {
        return showEmptyView() ? (count() == 0 ? 1 : count()) : count();
    }

    @Deprecated
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (count() == 0 && showEmptyView()) {
            convertView = inflater.inflate(R.layout.holder_base_empty, parent, false);
            View empty = emptyView(inflater, parent);
            if (empty == null) {
                TextView textView = (TextView) convertView.findViewById(R.id.tv_empty);
                textView.setText(notifyed ? emptyString() : "");
            } else {
                ((EmptyLayout) convertView).removeAllViews();
                if (notifyed) {
                    ((EmptyLayout) convertView).addView(empty, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                }
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onEmptyClick(v);
                }
            });
        } else {
            T holder;
            if (convertView == null || convertView instanceof EmptyLayout) {
                convertView = inflater.inflate(getLayout(position), parent, false);
                holder = newHolder();
                ViewUtil.find(holder, convertView, getIdClass());
                convertView.setTag(holder);
            } else {
                holder = (T) convertView.getTag();
            }
            holder.itemView = convertView;
            ViewUtil.click(holder, convertView);
            bindView(holder, position);
        }
        return convertView;
    }

    protected void onEmptyClick(View v) {
    }

    public Class<?> getIdClass() {
        return null;
    }

    @Override
    public void notifyDataSetChanged() {
        notifyDataSetChanged(true);
    }

    public void notifyDataSetChanged(boolean showEmpty) {
        notifyed = showEmpty;
        super.notifyDataSetChanged();
    }

    public static class BaseViewHolder {
        public View itemView;
    }
}
