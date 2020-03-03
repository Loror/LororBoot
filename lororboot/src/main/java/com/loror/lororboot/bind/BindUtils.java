package com.loror.lororboot.bind;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loror.lororUtil.image.BitmapConverter;
import com.loror.lororUtil.image.ImageUtil;
import com.loror.lororboot.LororApplication;
import com.loror.lororboot.annotation.AppendId;
import com.loror.lororboot.annotation.Bind;
import com.loror.lororboot.annotation.BindAbleItemConnection;
import com.loror.lororboot.annotation.DisableItem;
import com.loror.lororboot.annotation.Gif;
import com.loror.lororboot.annotation.Visibility;
import com.loror.lororboot.startable.LororActivity;
import com.loror.lororboot.startable.LororDialog;
import com.loror.lororboot.startable.LororFragment;
import com.loror.lororboot.views.BindAbleBannerView;
import com.loror.lororboot.views.BindAblePointView;
import com.loror.lororboot.views.BindRefreshAble;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class BindUtils {

    /**
     * 找到BIndAble的BindHolder并初始化
     */
    public static void findBindHoldersAndInit(List<BindHolder> bindHolders, final BindAble bindAble) {
        findBindHolders(bindHolders, bindAble, null);
        initHolders(bindHolders, bindAble, null);
    }

    /**
     * 初始化BindHolder
     */
    public static void initHolders(List<BindHolder> bindHolders, final BindAble bindAble, Object tag) {
        for (BindHolder bindHolder : bindHolders) {
            bindHolder.setTag(tag);
            boolean interrupt = bindAble.onBindFind(bindHolder);
            specialBinder(bindHolder, bindHolder.getView(), bindAble);
            if (interrupt) {
                Object volume = getValue(bindHolder, bindAble);
                if (volume instanceof List) {
                    bindHolder.compareTag = ((List) volume).size();
                } else {
                    bindHolder.compareTag = volume;
                }
            } else {
                if (bindHolder.visibility != Visibility.NOTCHANGE) {
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
                Object volume = getValue(bindHolder, bindAble);
                if (volume instanceof List) {
                    bindHolder.compareTag = -1;
                } else {
                    bindHolder.compareTag = null;
                }
                bindHolder.isFirst = true;
            }
        }
    }

    /**
     * 找到BIndAble的BindHolder
     */
    public static void findBindHolders(List<BindHolder> bindHolders, final BindAble bindAble, View parent) {
        bindHolders.clear();
        Field[] fields = bindAble.getClass().getDeclaredFields();
        HashMap<Integer, List<Field>> connections = new HashMap<>();
        if (fields != null) {
            for (int i = 0; i < fields.length; ++i) {
                final Field field = fields[i];
                Bind bind = (Bind) field.getAnnotation(Bind.class);
                if (bind != null) {
                    field.setAccessible(true);
                    int id = bind.id();
                    View view = findViewById(id, bindAble, parent);
                    if (view != null) {
                        if (bind.visibility() != Visibility.NOTCHANGE) {
                            view.setVisibility(bind.visibility());
                        }
                        final BindHolder bindHolder = new BindHolder();
                        bindHolder.view = view;
                        bindHolder.field = field;
                        bindHolder.format = bind.format().length() == 0 ? null : bind.format();
                        bindHolder.event = bind.event().length() == 0 ? null : bind.event();
                        bindHolder.empty = bind.empty().length() == 0 ? null : bind.empty();
                        bindHolder.visibility = bind.visibility();
                        bindHolder.imagePlace = bind.imagePlace();
                        bindHolder.errorPlace = bind.errorPlace();
                        bindHolder.imageWidth = bind.imageWidth();
                        bindHolder.onlyEvent = bind.onlyEvent();
                        bindHolder.disableItem = field.getAnnotation(DisableItem.class) != null;
                        bindHolder.gif = field.getAnnotation(Gif.class) != null;
                        if (bind.bitmapConverter() != BitmapConverter.class && view instanceof ImageView) {
                            try {
                                bindHolder.bitmapConverter = bind.bitmapConverter().newInstance();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (view instanceof TextView) {
                            if (bindHolder.format != null) {
                                int index = 0;
                                do {
                                    int start = bindHolder.format.indexOf("${", index);
                                    if (start < 0) {
                                        break;
                                    }
                                    int end = bindHolder.format.indexOf("}", start);
                                    if (end < 0) {
                                        break;
                                    }
                                    index = end;
                                    String fieldName = bindHolder.format.substring(start + 2, end);
                                    try {
                                        Field find = bindAble.getClass().getDeclaredField(fieldName);
                                        if (bindHolder.unions == null) {
                                            bindHolder.unions = new LinkedList<>();
                                        }
                                        bindHolder.unions.add(find);
                                    } catch (NoSuchFieldException e) {
                                        e.printStackTrace();
                                    }

                                } while (true);
                            }
                        }
                        bindHolders.add(bindHolder);
                        AppendId appendId = field.getAnnotation(AppendId.class);
                        if (appendId != null) {
                            int[] ids = appendId.id();
                            boolean only = appendId.onlyEvent();
                            int length = ids.length;
                            for (int j = 0; j < length; j++) {
                                view = findViewById(ids[j], bindAble, parent);
                                if (view == null) {
                                    continue;
                                }
                                BindHolder appendBindHolder = bindHolder.cloneOne();
                                appendBindHolder.view = view;
                                appendBindHolder.onlyEvent = only;
                                bindHolders.add(appendBindHolder);
                            }
                        }
                    }
                } else {
                    BindAbleItemConnection connection = (BindAbleItemConnection) field.getAnnotation(BindAbleItemConnection.class);
                    if (connection != null) {
                        if (connections.containsKey(connection.id())) {
                            List<Field> value = connections.get(connection.id());
                            if (!value.contains(field)) {
                                value.add(field);
                            }
                        } else {
                            List<Field> value = new LinkedList<>();
                            value.add(field);
                            connections.put(connection.id(), value);
                        }
                    }
                }
            }
            for (BindHolder holder : bindHolders) {
                int id = holder.getView().getId();
                if (connections.containsKey(id)) {
                    holder.connections = connections.get(id);
                }
            }
        }
    }

    /**
     * 查找
     */
    private static View findViewById(int id, BindAble bindAble, View parent) {
        View view = null;
        if (parent != null) {
            view = parent.findViewById(id);
        } else if (bindAble instanceof LororActivity) {
            view = ((Activity) bindAble).findViewById(id);
        } else if (bindAble instanceof LororFragment) {
            view = ((LororFragment) bindAble).getView().findViewById(id);
        } else if (bindAble instanceof LororDialog) {
            view = ((LororDialog) bindAble).findViewById(id);
        } else if (bindAble instanceof FindViewAble) {
            view = ((FindViewAble) bindAble).findViewById(id);
        }
        return view;
    }

    /**
     * 处理特定类型view绑定
     */
    protected static void specialBinder(final BindHolder bindHolder, View view, final BindAble bindAble) {
        final Field field = bindHolder.field;
        final int id = view.getId();
        if (view instanceof BindRefreshAble) {
            ((BindRefreshAble) view).find(new FieldControl(bindAble, bindHolder));
        } else if (view instanceof EditText) {
            bindHolder.unions = null;
            if (!bindHolder.onlyEvent) {
                Class<?> type = field.getType();
                if (type != String.class && type != CharSequence.class) {
                    throw new IllegalStateException("EditText为双向绑定，只支持属性为String，CharSequence类型(" + bindAble.getClass().getName() + "->" + field.getName() + ")");
                }
            }
            Object tag = view.getTag(id);
            if (tag instanceof TextWatcher) {
                ((EditText) view).removeTextChangedListener((TextWatcher) tag);
            }
            TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String value = s.toString();
                    if (bindHolder.format != null) {
                        int index = bindHolder.format.indexOf("%s");
                        if (index == -1) {
                            value = value.replace(bindHolder.format, "");
                        } else if (index == 0 || index == bindHolder.format.length() - 2) {
                            value = value.replace(bindHolder.format.replace("%s", ""), "");
                        } else {
                            String left = bindHolder.format.substring(0, index);
                            String right = bindHolder.format.substring(index + 2);
                            if (value.startsWith(left)) {
                                value = value.substring(left.length());
                            }
                            if (value.endsWith(right)) {
                                value = value.substring(0, value.length() - right.length());
                            }
                        }
                    }
                    Object old = bindHolder.compareTag;
                    if (!bindHolder.onlyEvent) {
                        try {
                            Class<?> type = field.getType();
                            if (type == String.class) {
                                field.set(bindAble, bindHolder.compareTag = value);
                            } else if (type == CharSequence.class) {
                                field.set(bindAble, bindHolder.compareTag = s);
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    if (bindHolder.event != null) {
                        bindAble.event(bindHolder, old == null ? null : String.valueOf(old), value);
                    }
                }
            };
            ((EditText) view).addTextChangedListener(watcher);
            view.setTag(id, watcher);
        } else if (view instanceof CheckBox) {
            if (!bindHolder.onlyEvent) {
                Class<?> type = field.getType();
                if (type != boolean.class && type != Boolean.class) {
                    throw new IllegalStateException("CheckBox为双向绑定，只支持绑定Boolean类型(" + bindAble.getClass().getName() + "->" + field.getName() + ")");
                }
            }
            ((CheckBox) view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!bindHolder.onlyEvent) {
                        try {
                            field.set(bindAble, bindHolder.compareTag = isChecked);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    if (bindHolder.event != null) {
                        bindAble.event(bindHolder, String.valueOf(!isChecked), String.valueOf(isChecked));
                    }
                }
            });
        } else if (view instanceof ProgressBar) {
            if (!bindHolder.onlyEvent) {
                Class<?> type = field.getType();
                if (type != Integer.class && type != Long.class && type != int.class && type != long.class) {
                    throw new IllegalStateException("ProgressBar只支持绑定Integer,Long类型(" + bindAble.getClass().getName() + "->" + field.getName() + ")");
                }
            }
        } else if (view instanceof AbsListView) {
            Class<?> type = field.getType();
            if (type == List.class || type == ArrayList.class) {
                List list = null;
                try {
                    list = (List) field.get(bindAble);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (list != null) {
                    BinderAdapter adapter = new BinderAdapter(view.getContext(), list, bindAble, bindHolder);
                    if (bindHolder.disableItem) {
                        adapter.setItemEnable(false);
                    }
                    ((AbsListView) view).setAdapter(adapter);
                    view.setTag(id, adapter);
                    bindHolder.compareTag = list.size();
                } else {
                    throw new IllegalStateException("AbsListView绑定的List<? extends BindAbleItem>不能为null(" + bindAble.getClass().getName() + "->" + field.getName() + ")");
                }
            } else {
                throw new IllegalStateException("AbsListView只支持绑定List<? extends BindAbleItem>类型(" + bindAble.getClass().getName() + "->" + field.getName() + ")");
            }
        } else if (view instanceof RecyclerView) {
            Class<?> type = field.getType();
            if (type == List.class || type == ArrayList.class) {
                List list = null;
                try {
                    list = (List) field.get(bindAble);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (list != null) {
                    RecyclerBindAbleAdapter adapter = new RecyclerBindAbleAdapter(view.getContext(), list, bindAble, bindHolder);
                    ((RecyclerView) view).setAdapter(adapter);
                    view.setTag(id, adapter);
                    bindHolder.compareTag = list.size();
                } else {
                    throw new IllegalStateException("RecyclerView绑定的List<? extends BindAbleItem>不能为null(" + bindAble.getClass().getName() + "->" + field.getName() + ")");
                }
            } else {
                throw new IllegalStateException("RecyclerView只支持绑定List<? extends BindAbleItem>类型(" + bindAble.getClass().getName() + "->" + field.getName() + ")");
            }
        } else if (view instanceof BindAbleBannerView) {
            Class<?> type = field.getType();
            if (type == List.class || type == ArrayList.class) {
                List list = null;
                try {
                    list = (List) field.get(bindAble);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                ViewGroup parent = ((ViewGroup) view.getParent());
                BindAblePointView pointView = null;
                if (parent.getChildCount() > 0) {
                    for (int i = 0; i < parent.getChildCount(); i++) {
                        View child = parent.getChildAt(i);
                        if (child instanceof BindAblePointView) {
                            view.setTag(view.getId(), pointView = (BindAblePointView) child);
                            break;
                        }
                    }
                }
                if (list != null) {
                    if (pointView != null) {
                        ((BindAbleBannerView) view).setPointView(pointView);
                    }
                    if (list.size() > 0) {
                        BindAbleBannerAdapter adapter = new BindAbleBannerAdapter(view.getContext(), list, bindAble, bindHolder);
                        ((BindAbleBannerView) view).setAdapter(adapter);
                    }
                    bindHolder.compareTag = list.size();
                } else {
                    throw new IllegalStateException("BindAbleBannerView绑定的List<?>不能为null(" + bindAble.getClass().getName() + "->" + field.getName() + ")");
                }
            } else {
                throw new IllegalStateException("BindAbleBannerViewPager只支持绑定List<?>类型(" + bindAble.getClass().getName() + "->" + field.getName() + ")");
            }
        }

    }

    /**
     * 检测所有BindHolder变化并更新显示
     */
    public static void showBindHolders(List<BindHolder> bindHolders, BindAble bindAble) {
        for (BindHolder bindHolder : bindHolders) {
            showBindHolder(bindHolder, bindAble);
        }
    }

    protected static Object getValue(BindHolder bindHolder, BindAble bindAble) {
        Object value = null;
        try {
            value = bindHolder.field.get(bindAble);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 检测BindHolder变化并更新显示
     */
    public static void showBindHolder(final BindHolder bindHolder, BindAble bindAble) {
        Object volume = getValue(bindHolder, bindAble);
        boolean isList = volume instanceof List;
        boolean volumeChange = !isList && ((bindHolder.compareTag == null && volume != null) || (bindHolder.compareTag != null && !bindHolder.compareTag.equals(volume)));
        boolean listChange = isList && (bindHolder.compareTag == null || (int) bindHolder.compareTag != ((List) volume).size());
        //首次一定触发
        if (bindHolder.isFirst || (volumeChange || listChange)) {
            bindHolder.isFirst = false;
            String vol = volume == null ? bindHolder.empty : String.valueOf(volume);
            vol = vol == null ? null :
                    (bindHolder.format == null ? vol : bindHolder.format.replace("%s", vol));
            Object old = bindHolder.compareTag;
            if (isList) {
                bindHolder.compareTag = ((List) volume).size();
            } else {
                bindHolder.compareTag = volume;
            }
            if (!bindHolder.onlyEvent) {
                if (bindHolder.view instanceof BindRefreshAble) {
                    ((BindRefreshAble) bindHolder.view).refresh(new Value(bindHolder.format, bindHolder.empty, volume));
                } else if (bindHolder.view instanceof CheckBox) {
                    ((CheckBox) bindHolder.view).setChecked(!(volume == null || !(Boolean) volume));
                } else if (bindHolder.view instanceof TextView) {
                    if (bindHolder.field.getType() == CharSequence.class) {
                        ((TextView) bindHolder.view).setText((CharSequence) volume);
                    } else {
                        if (vol == null) {
                            //为空时format占位，无format显示空
                            if (bindHolder.format == null) {
                                ((TextView) bindHolder.view).setText("");
                            } else {
                                ((TextView) bindHolder.view).setText(bindHolder.union(bindAble, bindHolder.format.replace("%s", "")));
                            }
                        } else {
                            ((TextView) bindHolder.view).setText(bindHolder.union(bindAble, vol));
                        }
                    }
                } else if (bindHolder.view instanceof ImageView) {
                    ImageView imageView = (ImageView) bindHolder.view;
                    if (volume instanceof Integer && bindHolder.format == null) {
                        imageView.setImageResource((Integer) volume);
                    } else {
                        int width = bindHolder.imageWidth;
                        if (width > 1080) {
                            width = 1080;
                        }
                        ImageUtil imageUtil = ImageUtil.with(imageView.getContext())
                                .from(vol == null ? "" : vol).to(imageView).setWidthLimit(width).setNoSdCache(LororApplication.NoImageSdCardCache).setIsGif(bindHolder.gif);
                        if (bindHolder.imagePlace != 0) {
                            imageUtil.setDefaultImage(bindHolder.imagePlace);
                        }
                        if (bindHolder.errorPlace != 0) {
                            imageUtil.setErrorImage(bindHolder.errorPlace);
                        }
                        imageUtil.setBitmapConverter(bindHolder.bitmapConverter);
                        imageUtil.loadImage();
                    }
                } else if (bindHolder.view instanceof ProgressBar) {
                    if (volume == null) {
                        ((ProgressBar) bindHolder.view).setProgress(0);
                    } else {
                        ((ProgressBar) bindHolder.view).setProgress((int) Long.parseLong(String.valueOf(volume)));
                    }
                } else if (bindHolder.view instanceof WebView) {
                    if (vol != null) {
                        ((WebView) bindHolder.view).loadUrl(vol);
                    }
                } else if (bindHolder.view instanceof AbsListView) {
                    BinderAdapter adapter = (BinderAdapter) bindHolder.view.getTag(bindHolder.view.getId());
                    adapter.notifyDataSetChanged();
                } else if (bindHolder.view instanceof RecyclerView) {
                    final RecyclerView recyclerView = (RecyclerView) bindHolder.view;
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (!recyclerView.isComputingLayout()) {
                                RecyclerBindAbleAdapter adapter = (RecyclerBindAbleAdapter) bindHolder.view.getTag(bindHolder.view.getId());
                                adapter.notifyDataSetChanged();
                            } else {
                                recyclerView.postDelayed(this, 10);
                            }
                        }
                    };//正在布局中则延迟刷新
                    runnable.run();
                } else if (bindHolder.view instanceof BindAbleBannerView) {
                    if (volume == null) {
                        throw new IllegalStateException("BindAbleBannerView绑定的List<?>不能为null(" + bindAble.getClass().getName() + "->" + bindHolder.field.getName() + ")");
                    }
                    List list = (List) volume;
                    if (list.size() > 0) {
                        BindAbleBannerAdapter adapter = new BindAbleBannerAdapter(bindHolder.view.getContext(), list, bindAble, bindHolder);
                        ((BindAbleBannerView) bindHolder.view).setAdapter(adapter);
                    }
                }
            }
            if (bindHolder.event != null) {
                bindAble.event(bindHolder, old == null ? null : String.valueOf(old), volume == null ? null : String.valueOf(volume));
            }
        }
    }

    public static BindHolder findHolderById(List<BindHolder> bindHolders, @IdRes int id) {
        BindHolder bindHolder = null;
        for (BindHolder item : bindHolders) {
            if (item.view.getId() == id) {
                bindHolder = item;
                break;
            }
        }
        return bindHolder;
    }

    public static BindHolder findHolderByName(List<BindHolder> bindHolders, String feildName) {
        BindHolder bindHolder = null;
        for (BindHolder item : bindHolders) {
            if (item.field.getName().equals(feildName)) {
                bindHolder = item;
                break;
            }
        }
        return bindHolder;
    }
}
