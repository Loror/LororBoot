package com.loror.lororboot.bind;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class RecyclerBindAbleAdapter extends RecyclerView.Adapter<RecyclerBindAbleAdapter.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private List list;
    private BindAble bindAble;

    public RecyclerBindAbleAdapter(Context context, List list, BindAble bindAble) {
        this.context = context;
        this.list = list;
        this.bindAble = bindAble;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        int type = super.getItemViewType(position);
        if (list.size() > 0) {
            Object item = list.get(position);
            if (item instanceof BindAbleItem && ((BindAbleItem) item).viewTypeCount() > 1) {
                type = ((BindAbleItem) item).viewType();
            }
        }
        return type;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public RecyclerBindAbleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Object item = list.get(0);
        if (!(item instanceof BindAbleItem)) {
            throw new IllegalStateException("RecyclerView只支持绑定List<? extends BindAbleItem>类型");
        }
        BindAbleItem bindAbleItem = (BindAbleItem) item;
        View convertView = inflater.inflate(bindAbleItem.getLayout(), parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerBindAbleAdapter.ViewHolder holder, int position) {
        BindAbleItem bindAbleItem = (BindAbleItem) list.get(position);
        holder.bindAbleItem = bindAbleItem;
        BinderAdapter.Mark mark = new BinderAdapter.Mark();
        mark.bindAble = this.bindAble;
        mark.size = list.size();
        mark.position = position;
        mark.convertView = holder.itemView;
        bindAbleItem.updateBind(mark);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public BindAbleItem bindAbleItem;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
