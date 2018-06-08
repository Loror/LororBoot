package com.loror.lororboot.bind;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.loror.lororUtil.image.ImageUtil;
import com.loror.lororboot.adapter.OnItemClicklistener;

import java.util.ArrayList;
import java.util.List;

public class BindAbleBannerAdapter extends PagerAdapter {
    private Context context;
    private List list;
    private int imagePlace;
    private int widthLimit;
    private List<ImageView> views;
    private int tagKey = 4 << 24;
    private OnItemClicklistener onItemClicklistener;

    public BindAbleBannerAdapter(Context context, List list, int imagePlace, int widthLimit) {
        this.context = context;
        this.list = list;
        this.imagePlace = imagePlace;
        this.widthLimit = widthLimit == 0 ? getScreenWidth() : widthLimit;
        views = new ArrayList<ImageView>();
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
        ImageView v = onBindView(container, getItemCount() < 4 ? position % 3 : index, getItemCount() == 1 ? 0 : index);
        onViewSwitched(v, index);
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

    private void onViewSwitched(ImageView imageView, int position) {
        ImageUtil imageUtil = ImageUtil.with(context).from(String.valueOf(list.get(position))).to(imageView);
        int width = widthLimit;
        if (width == 0) {
            width = 720;
        }
        imageUtil.setWidthLimit(width);
        if (imagePlace != 0) {
            imageUtil.setDefaultImage(imagePlace);
        }
        imageUtil.loadImage();
    }

    /**
     * 抽取布局
     */
    private ImageView onBindView(ViewGroup container, int tag, int position) {
        ImageView item = null;
        for (ImageView view : views) {
            if (tag == (Integer) view.getTag(tagKey)) {
                item = view;
                break;
            }
        }
        if (item == null) {
            item = getItemView(container);
            item.setTag(tagKey, tag);
            views.add(item);
        }
        return item;
    }

    public ImageView getItemView(ViewGroup container) {
        ImageView imageView = new ImageView(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imageView;
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

    public void setOnItemClicklistener(OnItemClicklistener onItemClicklistener) {
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
