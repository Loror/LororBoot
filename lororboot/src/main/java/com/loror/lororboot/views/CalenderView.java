package com.loror.lororboot.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Create By Loror
 * 自定义可标记日历控件
 */
public class CalenderView extends LinearLayout {

    public interface OnDateClickListener {
        void onDateClick(String date);
    }

    private boolean markerTintDate;
    private int markerTextColor;//标记点文字颜色，markerTintDate开启时有效
    private boolean expand;//是否展开
    private int line;//总行数
    private String date, toDate;
    private String currentDate;//当前日期
    private String choseDate;//点击日期
    private OnDateClickListener onDateClickListener;

    //非展开状态只显示首行周，其他行缓存到这等展开时加入
    private List<Data> holders;

    //标记
    private HashMap<Integer, List<String>> marker = new HashMap<>();
    //本月首周，当前周
    private Data firstWeek, toWeek;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM", Locale.CHINA);
    private SimpleDateFormat simpleDateFormatDay = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

    public CalenderView(Context context) {
        this(context, null);
    }

    public CalenderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalenderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        Date date = new Date();
        this.toDate = this.date = simpleDateFormat.format(date);
        currentDate = simpleDateFormatDay.format(date);
        load();
    }

    /**
     * 设置日期点击监听
     */
    public void setOnDateClickListener(OnDateClickListener onDateClickListener) {
        this.onDateClickListener = onDateClickListener;
    }

    /**
     * 设置日期，格式yyyy-MM
     */
    public void setDate(String date) {
        try {
            Date d = simpleDateFormat.parse(date);
            date = simpleDateFormat.format(d);
            if (!date.equals(this.date)) {
                this.date = date;
                load();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置选择，格式yyyy-MM-dd
     */
    public void setChoseDate(String choseDate) {
        try {
            Date d = simpleDateFormatDay.parse(choseDate);
            choseDate = simpleDateFormat.format(d);
            if (!choseDate.equals(this.choseDate)) {
                this.choseDate = choseDate;
                load();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置是否为marker的日期加上背景
     */
    public void setMarkerTintDate(boolean markerTintDate) {
        this.markerTintDate = markerTintDate;
        refresh();
    }

    /**
     * 设置标记点文字颜色，markerTintDate开启时有效
     */
    public void setMarkerTextColor(@ColorInt int markerTextColor) {
        this.markerTextColor = markerTextColor;
    }

    /**
     * 清除标记
     */
    public void clearMarker(@ColorInt int color) {
        marker.remove(color);
        refresh();
    }

    /**
     * 清除标记
     */
    public void clearMarkers() {
        marker.clear();
        refresh();
    }

    /**
     * 添加标记日期，格式yyyy-MM-dd
     */
    public void addMarker(@ColorInt int color, List<String> makerDates) {
        List<String> dates = marker.get(color);
        if (dates != null) {
            dates.clear();
        } else {
            dates = new ArrayList<>();
            marker.put(color, dates);
        }
        dates.addAll(makerDates);
        refresh();
    }

    /**
     * 设置是否展开状态
     */
    public void setExpand(final boolean expand, boolean animate) {
        if (this.expand == expand || getAnimation() != null) {
            return;
        }
        this.expand = expand;
        final int height = getHeight();
        final float end;
        if (expand) {
            end = (line + 1) / 2.0f;
            //有缓存周时先恢复缓存周到控件
            if (holders != null) {
                //锁定高度，避免添加周时高度改变
                ViewGroup.LayoutParams params = getLayoutParams();
                params.height = height;
                setLayoutParams(params);
                for (int i = 0; i < holders.size(); i++) {
                    Data data = holders.get(i);
                    ViewHolder holder = new ViewHolder();
                    holder.create(getContext());
                    holder.setShows(data.shows, data.dates);
                    addView(holder.view, holder.getLayoutParams());
                }
                holders = null;
            }
        } else {
            end = 2.0f / (line + 1);
        }
        if (animate) {
            //展开时将第一周恢复
            if (toWeek != null && toDate.equals(date) && expand) {
                ViewHolder holder = (ViewHolder) getChildAt(1).getTag();
                holder.data = firstWeek;
                holder.refresh();
            }
            Animation animation = new Animation() {

                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    super.applyTransformation(interpolatedTime, t);
                    ViewGroup.LayoutParams params = getLayoutParams();
                    int endHeight = (int) (height * end);
                    params.height = (int) (height + interpolatedTime * (endHeight - height));
                    setLayoutParams(params);
                    if (interpolatedTime == 1) {
                        setAnimation(null);
                        if (expand) {
                            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            setLayoutParams(params);
                        } else if (toWeek != null && toDate.equals(date)) {//收起后将当前周置顶
                            ViewHolder holder = (ViewHolder) getChildAt(1).getTag();
                            holder.data = toWeek;
                            holder.refresh();
                        }
                    }
                }
            };
            animation.setDuration(300);
            startAnimation(animation);
        } else {
            //收起后将当前周置顶，展开时将第一周恢复
            if (toWeek != null && toDate.equals(date)) {
                ViewHolder holder = (ViewHolder) getChildAt(1).getTag();
                if (expand) {
                    holder.data = firstWeek;
                } else {
                    holder.data = toWeek;
                }
                holder.refresh();
            }
            ViewGroup.LayoutParams params = getLayoutParams();
            if (!expand) {
                params.height = (int) (height * end);
            } else {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            setLayoutParams(params);
        }
    }

    /**
     * 是否展开状态
     */
    public boolean isExpand() {
        return expand;
    }

    /**
     * 刷新标记
     */
    private void refresh() {
        for (int i = 1; i < getChildCount(); i++) {
            View view = getChildAt(i);
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.refresh();
        }
    }

    private void load() {
        removeAllViews();
        if (!expand) {
            holders = new ArrayList<>();
        }
        ViewHolder header = new ViewHolder();
        header.create(getContext());
        List<String> headers = Arrays.asList("日", "一", "二", "三", "四", "五", "六");
        header.setShows(headers, null);
        addView(header.view);
        try {
            Date date = simpleDateFormat.parse(this.date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            int dayCount = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            int begin = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            line = (dayCount + begin) % 7 == 0 ? ((dayCount + begin) / 7) : ((dayCount + begin) / 7 + 1);
            int size = 7 - begin;
            boolean findToWeek = false;
            for (int i = 0; i < line; i++) {
                List<String> shows = new ArrayList<>();
                List<String> dates = new ArrayList<>();
                for (int j = 0; j < 7; j++) {
                    if (i == 0) {
                        if (j < begin) {
                            shows.add("");
                            dates.add("");
                        } else {
                            int day = j - begin + 1;
                            shows.add(String.valueOf(day));
                            dates.add(this.date + "-" + (day < 10 ? ("0" + day) : String.valueOf(day)));
                        }
                    } else {
                        int day = (i - 1) * 7 + j + size + 1;
                        if (day <= dayCount) {
                            shows.add(String.valueOf(day));
                            dates.add(this.date + "-" + (day < 10 ? ("0" + day) : String.valueOf(day)));
                        }
                    }
                }
                ViewHolder holder = new ViewHolder();
                if (i == 0) {
                    firstWeek = new Data();
                    firstWeek.shows = shows;
                    firstWeek.dates = dates;
                } else if (!findToWeek && dates.contains(currentDate)) {
                    findToWeek = true;
                    toWeek = new Data();
                    toWeek.shows = shows;
                    toWeek.dates = dates;
                }
                //非展开状态只显示首行周，其他行缓存到展开时加入
                if (!expand) {
                    if (i == 0) {
                        holder.create(getContext());
                        holder.setShows(shows, dates);
                        addView(holder.view, holder.getLayoutParams());
                    } else {
                        Data data = new Data();
                        data.shows = shows;
                        data.dates = dates;
                        holders.add(data);
                    }
                } else {
                    holder.create(getContext());
                    holder.setShows(shows, dates);
                    addView(holder.view, holder.getLayoutParams());
                }
            }
            //非展开时当前周置顶
            if (!expand) {
                if (toWeek != null && toDate.equals(this.date)) {
                    ViewHolder holder = (ViewHolder) getChildAt(1).getTag();
                    holder.data = toWeek;
                    holder.refresh();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private class Data {
        List<String> shows;
        List<String> dates;
    }

    /**
     * 控件生成类
     */
    private class ViewHolder {
        View view;

        private final int COUNT = 7;
        private final int TEXT_ON = 0xFF333333;
        private final int TEXT_OFF = 0xFFAFAEAE;

        TextView[] text = new TextView[COUNT];
        View[] doi = new View[COUNT];

        Data data = new Data();

        /**
         * 创建控件
         */
        void create(Context context) {
            view = new LinearLayout(context);
            ((LinearLayout) view).setOrientation(HORIZONTAL);
            view.setTag(this);
            for (int i = 0; i < COUNT; i++) {
                LinearLayout item = new LinearLayout(context);
                item.setOrientation(VERTICAL);
                item.setGravity(Gravity.CENTER_HORIZONTAL);
                LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
                params.weight = 1;
                ((LinearLayout) view).addView(item, params);

                text[i] = new TextView(context);
                text[i].setTextColor(0xFF333333);
                text[i].setTextSize(16);
                text[i].setMinWidth(Dp2Px(context, 20));
                text[i].setGravity(Gravity.CENTER_HORIZONTAL);
                TextPaint tp = text[i].getPaint();
                tp.setFakeBoldText(true);
                LayoutParams textParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                textParams.topMargin = Dp2Px(context, 15);
                item.addView(text[i], textParams);

                doi[i] = new View(context);
                LayoutParams doiParams = new LayoutParams(Dp2Px(context, 6), Dp2Px(context, 6));
                textParams.topMargin = Dp2Px(context, 6);
                textParams.bottomMargin = Dp2Px(context, 4);
                item.addView(doi[i], doiParams);
            }
        }

        /**
         * 设置显示
         */
        void setShows(List<String> shows, List<String> dates) {
            data.shows = shows;
            data.dates = dates;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            for (int i = 0; i < COUNT; i++) {
                text[i].setText("");
                doi[i].setVisibility(INVISIBLE);
            }
            for (int i = 0; i < shows.size() && i < COUNT; i++) {
                text[i].setText(shows.get(i));
                if (dates == null) {
                    text[i].setTextColor(TEXT_ON);
                } else {
                    boolean up = true;
                    try {
                        String date = dates.get(i);
                        if (!TextUtils.isEmpty(date)) {
                            up = format.parse(date).getTime() > format.parse(currentDate).getTime();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (i == 0 || i == 6 || up) {
                        text[i].setTextColor(TEXT_OFF);
                    } else {
                        text[i].setTextColor(TEXT_ON);
                    }
                }
            }
            if (dates != null) {
                for (int i = 0; i < dates.size() && i < COUNT; i++) {
                    final String date = dates.get(i);
                    doi[i].setVisibility(INVISIBLE);
                    text[i].setBackgroundColor(0);
                    for (Integer color : marker.keySet()) {
                        List<String> markDates = marker.get(color);
                        if (markDates.contains(date)) {
                            doi[i].setVisibility(VISIBLE);
                            doi[i].setBackgroundDrawable(getDrawable(color, 100));
                            if (markerTintDate) {
                                text[i].setBackgroundDrawable(getDrawable(color, 100));
                                if (markerTextColor != 0) {
                                    text[i].setTextColor(markerTextColor);
                                }
                            }
                            break;
                        }
                    }
                    if (date.length() > 0) {
                        ((View) text[i].getParent()).setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                choseDate = date;
                                CalenderView.this.refresh();
                                if (onDateClickListener != null) {
                                    onDateClickListener.onDateClick(choseDate);
                                }
                            }
                        });
                    }
                    if (date.equals(currentDate)) {
                        text[i].setBackgroundDrawable(getDrawable(0x332DA1FF, 100));
                    }
                    if (date.equals(choseDate)) {
                        text[i].setBackgroundDrawable(getDrawable(0xAA2DA1FF, 100));
                        text[i].setTextColor(0xFFFFFFFF);
                    }
                }
            }
        }

        /**
         * 刷新显示
         */
        void refresh() {
            if (data.shows != null) {
                setShows(data.shows, data.dates);
            }
        }

        ViewGroup.LayoutParams getLayoutParams() {
            return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int Dp2Px(Context context, float dp) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dp * scale + 0.5f);
        }

        Drawable getDrawable(int color, int cornerRadius) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(cornerRadius);//设置4个角的弧度
            drawable.setColor(color);// 设置颜色
            return drawable;
        }
    }

}
