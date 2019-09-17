package com.loror.lororboot.bind;

import android.content.Context;
import android.support.annotation.NonNull;
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
    private BindHolder bindHolder;

    public RecyclerBindAbleAdapter(Context context, List list, BindAble bindAble, BindHolder bindHolder) {
        this.context = context;
        this.list = list;
        this.bindAble = bindAble;
        this.bindHolder = bindHolder;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        int type = super.getItemViewType(position);
        if (list.size() > 0) {
            Object item = list.get(position);
            if (item instanceof BindAbleItem) {
                BindAbleItem bindAbleItem = (BindAbleItem) item;
                BinderAdapter.Mark mark = new BinderAdapter.Mark();
                mark.bindAble = this.bindAble;
                mark.position = position;
                mark.size = list.size();
                bindAbleItem.refreshMark(mark);
                type = bindAbleItem.viewType();
            }
        }
        return type;
    }

    public Context getContext() {
        return context;
    }

    @Override
    @NonNull
    public RecyclerBindAbleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Object item = list.get(0);
        if (!(item instanceof BindAbleItem)) {
            throw new IllegalStateException("RecyclerView只支持绑定List<? extends BindAbleItem>类型");
        }
        BindAbleItem bindAbleItem = (BindAbleItem) item;
        int layout = bindAbleItem.getLayout(viewType);
        if (layout == 0) {
            throw new IllegalArgumentException(bindAbleItem.getClass().getName() + ":未指定layout");
        }
        View convertView = inflater.inflate(layout, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerBindAbleAdapter.ViewHolder holder, int position) {
        BindAbleItem bindAbleItem = (BindAbleItem) list.get(position);
        BinderAdapter.Mark mark = new BinderAdapter.Mark();
        mark.bindAble = this.bindAble;
        mark.size = list.size();
        mark.position = holder.getAdapterPosition();
        bindAbleItem.refreshMark(mark);
        if (bindHolder.connections != null) {
            BindAbleItemConnectionUtils.connect(bindAbleItem, bindAble, bindHolder.connections.get());
        }
        bindAbleItem.updateBind(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
