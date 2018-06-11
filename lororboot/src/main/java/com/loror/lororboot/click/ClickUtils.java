package com.loror.lororboot.click;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.loror.lororboot.adapter.OnItemClicklistener;
import com.loror.lororboot.annotation.Click;
import com.loror.lororboot.annotation.ItemClick;
import com.loror.lororboot.bind.BindAbleItem;
import com.loror.lororboot.bind.BinderAdapter;
import com.loror.lororboot.views.BindAbleBannerView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClickUtils {

    /**
     * id查找控件
     */
    private static final View findViewById(Object object, int id) {
        View view = null;
        if (object instanceof Activity) {
            view = ((Activity) object).findViewById(id);
        } else if (object instanceof Fragment) {
            view = ((Fragment) object).getView().findViewById(id);
        } else if (object instanceof Dialog) {
            view = ((Dialog) object).findViewById(id);
        } else if (object instanceof View) {
            view = ((View) object).findViewById(id);
        }
        return view;
    }

    /**
     * 找到所有Click并注册监听
     */
    public static void findAndBindClick(final Object object) {
        Method[] methods = object.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            final Method method = methods[i];
            Click click = (Click) method.getAnnotation(Click.class);
            ItemClick itemClick = (ItemClick) method.getAnnotation(ItemClick.class);
            if (click != null) {
                int id = click.id();
                View view = findViewById(object, id);
                if (view != null) {
                    method.setAccessible(true);
                    final long clickSpace = click.clickSpace();
                    view.setOnClickListener(new View.OnClickListener() {
                        long clickTime;

                        @Override
                        public void onClick(View v) {
                            if (System.currentTimeMillis() - clickTime > clickSpace) {
                                clickTime = System.currentTimeMillis();
                                try {
                                    method.invoke(object, v);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });
                }
            }
            if (itemClick != null) {
                int id = itemClick.id();
                if (id != 0) {
                    View view = findViewById(object, id);
                    method.setAccessible(true);
                    final long clickSpace = itemClick.clickSpace();
                    if (view != null) {
                        if (view instanceof AbsListView) {
                            ((AbsListView) view).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                long clickTime;

                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    if (System.currentTimeMillis() - clickTime > clickSpace) {
                                        clickTime = System.currentTimeMillis();
                                        try {
                                            method.invoke(object, view, position);
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        } catch (InvocationTargetException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        } else if (view instanceof BindAbleBannerView) {
                            ((BindAbleBannerView) view).setOnItemClicklistener(new OnItemClicklistener() {
                                long clickTime;

                                @Override
                                public void onItemClick(View view, int position) {
                                    if (System.currentTimeMillis() - clickTime > clickSpace) {
                                        clickTime = System.currentTimeMillis();
                                        try {
                                            method.invoke(object, view, position);
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        } catch (InvocationTargetException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    /**
     * 找到BindAbleAdapter中所有Click并注册监听
     */
    public static void findAndBindClickOfAdapter(final Object object, View parent, ViewGroup group) {
        Method[] methods = object.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            final Method method = methods[i];
            Click click = (Click) method.getAnnotation(Click.class);
            ItemClick itemClick = (ItemClick) method.getAnnotation(ItemClick.class);
            if (click != null) {
                int id = click.id();
                View view = parent.findViewById(id);
                if (view != null) {
                    method.setAccessible(true);
                    final long clickSpace = click.clickSpace();
                    view.setOnClickListener(new View.OnClickListener() {
                        long clickTime;

                        @Override
                        public void onClick(View v) {
                            if (System.currentTimeMillis() - clickTime > clickSpace) {
                                clickTime = System.currentTimeMillis();
                                try {
                                    method.invoke(object, v);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }
            if (itemClick != null) {
                int id = itemClick.id();
                if (id != 0) {
                    View view = parent.findViewById(id);
                    method.setAccessible(true);
                    final long clickSpace = itemClick.clickSpace();
                    if (view != null && view instanceof AbsListView) {
                        ((AbsListView) view).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            long clickTime;

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if (System.currentTimeMillis() - clickTime > clickSpace) {
                                    clickTime = System.currentTimeMillis();
                                    try {
                                        method.invoke(object, view, position);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                } else {
                    method.setAccessible(true);
                    final long clickSpace = itemClick.clickSpace();
                    if (group instanceof AbsListView) {
                        final AbsListView absListView = (AbsListView) group;
                        if (absListView.getOnItemClickListener() == null) {
                            absListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                long clickTime;

                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    if (System.currentTimeMillis() - clickTime > clickSpace) {
                                        clickTime = System.currentTimeMillis();
                                        BinderAdapter adapter = (BinderAdapter) absListView.getTag(absListView.getId());
                                        BindAbleItem item = (BindAbleItem) adapter.getItem(position);
                                        boolean clicked = false;
                                        try {
                                            method.invoke(item, view);
                                            clicked = true;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        if (!clicked) {
                                            try {
                                                method.invoke(item, view, position);
                                            } catch (IllegalAccessException e) {
                                                e.printStackTrace();
                                            } catch (InvocationTargetException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        parent.setOnClickListener(new View.OnClickListener() {
                            long clickTime;

                            @Override
                            public void onClick(View v) {
                                if (System.currentTimeMillis() - clickTime > clickSpace) {
                                    clickTime = System.currentTimeMillis();
                                    try {
                                        method.invoke(object, v);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }
}
