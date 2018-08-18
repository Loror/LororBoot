package com.loror.lororboot.startable;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.loror.lororUtil.view.ViewUtil;
import com.loror.lororboot.annotation.RunThread;
import com.loror.lororboot.annotation.RunTime;
import com.loror.lororboot.autoRun.AutoRunAble;
import com.loror.lororboot.autoRun.AutoRunHolder;
import com.loror.lororboot.autoRun.AutoRunUtil;
import com.loror.lororboot.bind.BindAble;
import com.loror.lororboot.bind.BindHolder;
import com.loror.lororboot.bind.BindUtils;
import com.loror.lororboot.bind.DataChangeAble;
import com.loror.lororboot.views.BindAbleBannerView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LororActivity extends AppCompatActivity implements StartDilogAble, BindAble, DataChangeAble, AutoRunAble {
    protected Context context = this;
    private List<BindHolder> bindHolders = new LinkedList<>();
    private List<BindAble> registedBinders = new ArrayList<>();
    private WeakReference<List<BindAble>> weakReferenceBindAbleList = new WeakReference<>(registedBinders);
    private Runnable bindRunnable;
    private Handler handler;
    private List<AutoRunHolder> autoRunHolders = new ArrayList<>();
    private int createState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        autoRunHolders = AutoRunUtil.findAutoRunHolders(this);
        createState = 1;
    }

    @Override
    protected void onResume() {
        if (createState == 1) {
            createState = 2;
            int size = autoRunHolders.size();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    AutoRunHolder holder = autoRunHolders.get(i);
                    if (holder.getWhen() == RunTime.AFTERONCREATE) {
                        AutoRunUtil.runAutoRunHolders(autoRunHolders, this);
                    }
                }
            }
        }
        super.onResume();
        if (!isFinishing()) {
            for (BindHolder bindHolder : bindHolders) {
                if (bindHolder.getView() instanceof BindAbleBannerView) {
                    ((BindAbleBannerView) bindHolder.getView()).startScrol();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (BindHolder bindHolder : bindHolders) {
            if (bindHolder.getView() instanceof BindAbleBannerView) {
                ((BindAbleBannerView) bindHolder.getView()).stopScrol();
            }
        }
    }

    @Override
    protected void onDestroy() {
        autoRunHolders.clear();
        super.onDestroy();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        BindUtils.findBindHoldersAndInit(bindHolders, this);
        ViewUtil.click(this);
        beginBind(this);
    }

    public void registerBinder(BindAble bindAble) {
        if (!registedBinders.contains(bindAble)) {
            registedBinders.add(bindAble);
            beginBind(this);
        }
    }

    public void unRegisterBinder(BindAble bindAble) {
        registedBinders.remove(bindAble);
    }

    public BindHolder findHolderById(@IdRes int id) {
        return BindUtils.findHolderById(bindHolders, id);
    }

    @Override
    public boolean onBindFind(BindHolder holder) {
        return false;
    }

    @Override
    public final void beginBind(Object tag) {
        if (handler == null) {
            handler = new Handler();
            final WeakReference<LororActivity> weakReference = new WeakReference<>(this);
            bindRunnable = new Runnable() {
                @Override
                public void run() {
                    LororActivity activity = weakReference.get();
                    List<BindAble> registedBinders = weakReferenceBindAbleList.get();
                    if (activity != null && !activity.isFinishing()) {
                        if (bindHolders.size() > 0 || registedBinders.size() > 0) {
                            BindUtils.showBindHolders(bindHolders, activity);
                            if (registedBinders != null) {
                                int size = registedBinders.size();
                                for (int i = 0; i < size; i++) {
                                    registedBinders.get(i).beginBind(this);
                                }
                            }
                            handler.removeCallbacks(bindRunnable);
                            handler.postDelayed(bindRunnable, 50);
                        } else {
                            handler.removeCallbacks(bindRunnable);
                            handler = null;
                        }
                    } else {
                        bindHolders.clear();
                        if (registedBinders != null) {
                            registedBinders.clear();
                        }
                    }
                }
            };
            bindRunnable.run();
        }
    }

    @Override
    public void event(BindHolder holder, String oldValue, String newValue) {

    }

    @Override
    public void setData(int id, Object value) {
        BindHolder holder = BindUtils.findHolderById(bindHolders, id);
        if (holder != null) {
            holder.getField().setAccessible(true);
            try {
                holder.getField().set(this, value);
                BindUtils.showBindHolder(holder, this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setData(String fieldName, Object value) {
        BindHolder holder = BindUtils.findHolderByName(bindHolders, fieldName);
        if (holder != null) {
            holder.getField().setAccessible(true);
            try {
                holder.getField().set(this, value);
                BindUtils.showBindHolder(holder, this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyListDataChangeById(@IdRes int id) {
        BindHolder bindHolder = BindUtils.findHolderById(bindHolders, id);
        if (bindHolder != null) {
            bindHolder.resetListCompareTag();
            BindUtils.showBindHolder(bindHolder, this);
        }
    }

    @Override
    public void startDialog(Intent intent) {
        try {
            Class classType = Class.forName(intent.getComponent().getClassName());
            Constructor<Dialog> con = classType.getConstructor(Context.class);
            Dialog obj = con.newInstance(this);
            if (obj instanceof LororDialog) {
                ((LororDialog) obj).putIntent(intent);
            } else if (intent.getFlags() != Intent.FLAG_ACTIVITY_NO_USER_ACTION) {
                Toast.makeText(this, "你开启的弹窗不是LororDialog，无法传递intent，如不需传递intent可以设置flags为FLAG_ACTIVITY_NO_USER_ACTION以忽略此信息。", Toast.LENGTH_LONG).show();
            }
            obj.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "开启弹窗失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void startDialogForResult(Intent intent, final int requestCode) {
        try {
            Class classType = Class.forName(intent.getComponent().getClassName());
            Constructor<Dialog> con = classType.getConstructor(Context.class);
            Dialog obj = con.newInstance(this);
            if (obj instanceof LororDialog) {
                ((LororDialog) obj).putIntent(intent);
                ((LororDialog) obj).forResult(requestCode, new LororDialog.ForResult() {
                    @Override
                    public void result(int requestCode, int resultCode, Intent data) {
                        onDialogResult(requestCode, resultCode, data);
                    }
                });
                obj.show();
            } else {
                Toast.makeText(this, "你开启的弹窗不是LororDialog，无法以forResult方式开启。", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "开启弹窗失败", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onDialogResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void runUserAutoRun(String methodName) {
        int size = autoRunHolders.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                AutoRunHolder holder = autoRunHolders.get(i);
                if (holder.getWhen() == RunTime.USERCALL && holder.getMethodName().equals(methodName)) {
                    AutoRunUtil.runAutoRunHolders(autoRunHolders, this);
                    break;
                }
            }
        }
    }

    @Override
    public void run(@RunThread int thread, Runnable runnable) {
        if (thread == RunThread.MAINTHREAD) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                runnable.run();
            } else {
                if (handler == null) {
                    handler = new Handler();
                }
                handler.post(runnable);
            }
        } else if (thread == RunThread.NEWTHREAD) {
            new Thread(runnable).start();
        } else {
            runnable.run();
        }
    }
}
