package com.loror.lororboot.views;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.loror.lororUtil.view.ItemClickAble;
import com.loror.lororUtil.view.OnItemClickListener;
import com.loror.lororboot.R;
import com.loror.lororboot.bind.BindAbleBannerAdapter;

public class BindAbleBannerView extends ViewPager implements ItemClickAble{

    private PagerAdapter adapter;
    private OnItemClickListener onItemClicklistener;
    private BindAblePointView pointView;

    public BindAbleBannerView(Context context) {
        this(context, null);
    }

    public BindAbleBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BindAbleBannerView);
        peroid = array != null ? array.getInt(R.styleable.BindAbleBannerView_changePeroid, 5000) : 5000;
        if (array != null) {
            array.recycle();
        }
    }

    public void setPointView(final BindAblePointView pointView) {
        this.pointView = pointView;
        bindPointView();
    }

    public BindAblePointView getPointView() {
        return pointView;
    }

    private void bindPointView() {
        if (this.pointView != null && adapter != null) {
            if (adapter instanceof BindAbleBannerAdapter) {
                clearOnPageChangeListeners();
                addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        pointView.setIndex(position % ((BindAbleBannerAdapter) adapter).getItemCount());
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
                this.pointView.setCount(((BindAbleBannerAdapter) adapter).getItemCount());
            }
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (adapter != null) {
                if (adapter instanceof BindAbleBannerAdapter) {
                    if (((BindAbleBannerAdapter) adapter).getItemCount() > 1) {
                        setCurrentItem(getCurrentItem() + 1);
                    }
                } else {
                    if (adapter.getCount() > 1) {
                        setCurrentItem((getCurrentItem() + 1) % adapter.getCount());
                    }
                }
            }
            return false;
        }
    });

    private Thread thread;
    private OnScrollChanged onScrollChanged;
    private boolean pause, calledStart;
    private int peroid = 5000;


    public interface OnScrollChanged {
        void onScrollChanged(int x, int width);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stopScrol();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (calledStart) {
                    resumeScrol();
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClicklistener = onItemClicklistener;
        if (adapter != null && adapter instanceof BindAbleBannerAdapter) {
            ((BindAbleBannerAdapter) adapter).setOnItemClicklistener(onItemClicklistener);
        }
    }

    @Override
    public void setAdapter(final PagerAdapter adapter) {
        this.adapter = adapter;
        if (adapter != null && adapter instanceof BindAbleBannerAdapter) {
            ((BindAbleBannerAdapter) adapter).setOnItemClicklistener(onItemClicklistener);
        }
        super.setAdapter(adapter);
        if (adapter instanceof BindAbleBannerAdapter && ((BindAbleBannerAdapter) adapter).getItemCount() > 0) {
            ((BindAbleBannerAdapter) adapter).releseViews();
            setCurrentItem(200 - (200 % ((BindAbleBannerAdapter) adapter).getItemCount()), false);
        }
        bindPointView();
    }

    public void setChangePeroid(int peroid) {
        this.peroid = peroid;
    }

    public void startScrol() {
        calledStart = true;
        resumeScrol();
    }

    protected void resumeScrol() {
        pause = false;
        if (thread == null) {
            thread = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            sleep(peroid);
                        } catch (InterruptedException e) {
                            return;
                        }
                        if (!pause) {
                            handler.sendEmptyMessage(0);
                        }
                    }
                }
            };
            thread.start();
        }
    }

    public void pauseScrol() {
        pause = true;
    }

    public void stopScrol() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public void setOnScrollChanged(OnScrollChanged onScrollChanged) {
        this.onScrollChanged = onScrollChanged;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (onScrollChanged != null) {
            onScrollChanged.onScrollChanged(l, getWidth());
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (calledStart) {
                    resumeScrol();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setAnimationSpeed(int speed) {
        changeAnimationSpeed(speed);
    }

    protected void changeAnimationSpeed(int speed) {
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(getContext(), new AccelerateInterpolator());
            field.set(this, scroller);
            scroller.setmDuration(speed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class FixedSpeedScroller extends Scroller {
        private int mDuration = 1500;

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, mDuration < 0 ? duration : mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            if (mDuration < 0) {
                super.startScroll(startX, startY, dx, dy);
            } else {
                super.startScroll(startX, startY, dx, dy, mDuration);
            }
        }

        public void setmDuration(int time) {
            mDuration = time;
        }

    }
}
