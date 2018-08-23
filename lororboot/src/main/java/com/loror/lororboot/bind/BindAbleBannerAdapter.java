package com.loror.lororboot.bind;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.loror.lororUtil.image.ImageUtil;
import com.loror.lororUtil.view.OnItemClickListener;
import com.loror.lororboot.views.EmptyLayout;

import java.util.ArrayList;
import java.util.List;

public class BindAbleBannerAdapter extends PagerAdapter {
    private Context context;
    private BindAble bindAble;
    private List list;
    private int imagePlace;
    private int widthLimit;
    private List<View> views = new ArrayList<View>();
    private int tagKey = 4 << 24;
    private OnItemClickListener onItemClicklistener;

    public BindAbleBannerAdapter(Context context, List list, int imagePlace, int widthLimit, BindAble bindAble) {
        this.context = context;
        this.bindAble = bindAble;
        this.list = list;
        this.imagePlace = imagePlace;
        this.widthLimit = widthLimit == 0 ? getScreenWidth() : widthLimit;
    }

    protected int getScreenWidth() {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        if (getItemCount() == 0) {
            return new View(context);
        }
        final int index = position % getItemCount();
        View v = onBindView(container, getItemCount() < 4 ? position % 3 : index, getItemCount() == 1 ? 0 : index);
        onViewSwitched(container, v, index);
        if (v.getParent() != null) {
            container.removeView(v);
        }
        container.addView(v);
        v.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onItemClick(v, index);
            }
        });
        return v;
    }

    /**
     * 布局切换
     */
    protected void onViewSwitched(ViewGroup contanier, View view, int position) {
        if (view instanceof ImageView) {
            ImageUtil imageUtil = ImageUtil.with(context).from(String.valueOf(list.get(position))).to((ImageView) view);
            int width = widthLimit;
            if (width == 0) {
                width = 720;
            }
            imageUtil.setWidthLimit(width);
            if (imagePlace != 0) {
                imageUtil.setDefaultImage(imagePlace);
            }

            imageUtil.loadImage();
        } else {
            Object item = list.get(position);
            if (!(item instanceof BindAbleItem)) {
                throw new IllegalStateException("Banner只支持绑定List<? extends BindAbleItem>类型");
            }
            BindAbleItem bindAbleItem = (BindAbleItem) item;
            BinderAdapter.Mark mark = new BinderAdapter.Mark();
            mark.bindAble = this.bindAble;
            mark.position = position;
            mark.convertView = view;
            mark.parent = contanier;
            bindAbleItem.beginBind(mark);
        }
    }

    /**
     * 抽取布局
     */
    protected View onBindView(ViewGroup container, int tag, int position) {
        View item = null;
        for (View view : views) {
            if (tag == (Integer) view.getTag(tagKey)) {
                item = view;
                break;
            }
        }
        if (item == null) {
            item = getItemView(container, position);
            item.setTag(tagKey, tag);
            views.add(item);
        }
        return item;
    }

    /**
     * 生成布局
     */
    protected View getItemView(ViewGroup container, int position) {
        Object item = list.get(position);
        if (item instanceof BindAbleItem) {
            return LayoutInflater.from(context).inflate(((BindAbleItem) item).getLayout(), container, false);
        } else {
            ImageView imageView = new ImageView(context);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            return imageView;
        }
    }

    public void releseViews() {
        views.clear();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void setOnItemClicklistener(OnItemClickListener onItemClicklistener) {
        this.onItemClicklistener = onItemClicklistener;
    }

    public void onItemClick(View view, int position) {
        if (onItemClicklistener != null) {
            onItemClicklistener.onItemClick(view, position);
        }
    }

    public int getItemCount() {
        return list.size();
    }

}
