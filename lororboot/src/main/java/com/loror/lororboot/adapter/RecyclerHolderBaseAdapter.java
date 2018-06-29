package com.loror.lororboot.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loror.lororUtil.view.ViewUtil;

/**
 * Created by Loror on 2017/12/7.
 */

public abstract class RecyclerHolderBaseAdapter<VH extends ViewHolder> extends RecyclerView.Adapter<VH> {

    private LayoutInflater inflater;
    private Context context;
    private OnItemClicklistener onItemClicklistener;

    public void setOnItemClicklistener(OnItemClicklistener onItemClicklistener) {
        this.onItemClicklistener = onItemClicklistener;
    }

    public RecyclerHolderBaseAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public Context getContext() {
        return context;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    @LayoutRes
    public abstract int getLayout(int viewType);

    public abstract VH newHolder(View item);

    @Override
    public final VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = inflater.inflate(getLayout(viewType), parent, false);
        VH holder = newHolder(convertView);
        ViewUtil.find(holder, convertView);
        ViewUtil.click(holder, convertView);
        return holder;
    }

    public abstract void bindView(VH baseViewHolder, int position);

    @Override
    public void onBindViewHolder(VH holder, final int position) {
        if (onItemClicklistener != null)
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClicklistener.onItemClick(view, position);
                }
            });
        bindView(holder, position);
    }

}
