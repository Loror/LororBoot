package com.loror.lororboot.startable;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.loror.lororUtil.view.ViewUtil;
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

public class LororFragment extends Fragment implements StartDilogAble, BindAble, DataChangeAble, AutoRunAble {

    private List<BindHolder> bindHolders = new LinkedList<>();
    private WeakReference<List<BindHolder>> weakReferenceList = new WeakReference<>(bindHolders);
    private WeakReference<LororActivity> weakReference;
    private Handler handler;
    private List<AutoRunHolder> autoRunHolders = new ArrayList<>();
    private int createState;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = getActivity();
        if (activity instanceof LororActivity) {
            weakReference = new WeakReference<>((LororActivity) activity);
            BindUtils.findBindHoldersAndInit(bindHolders, this);
            ViewUtil.click(this);
            if (bindHolders.size() > 0) {
                ((LororActivity) activity).registerBinder(this);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        autoRunHolders = AutoRunUtil.findAutoRunHolders(this);
        createState = 1;
    }

    @Override
    public void onResume() {
        if (createState == 1) {
            createState = 2;
            int size = autoRunHolders.size();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    AutoRunHolder holder = autoRunHolders.get(i);
                    if (holder.getWhen() == AutoRunHolder.AFTERONCREATE) {
                        AutoRunUtil.runAutoRunHolders(autoRunHolders, this);
                    }
                }
            }
        }
        super.onResume();
        List<BindHolder> bindHolders = weakReferenceList.get();
        if (bindHolders != null) {
            for (BindHolder bindHolder : bindHolders) {
                if (bindHolder.getView() instanceof BindAbleBannerView) {
                    ((BindAbleBannerView) bindHolder.getView()).startScrol();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        List<BindHolder> bindHolders = weakReferenceList.get();
        if (bindHolders != null) {
            for (BindHolder bindHolder : bindHolders) {
                if (bindHolder.getView() instanceof BindAbleBannerView) {
                    ((BindAbleBannerView) bindHolder.getView()).stopScrol();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        LororActivity activity = weakReference == null ? null : weakReference.get();
        if (activity != null) {
            activity.unRegisterBinder(this);
        }
        autoRunHolders.clear();
        super.onDestroy();
    }

    @Override
    public final void beginBind(Object tag) {
        LororActivity activity = weakReference == null ? null : weakReference.get();
        if (activity != null) {
            BindUtils.showBindHolders(bindHolders, this);
        }
    }

    public BindHolder findHolderById(@IdRes int id) {
        return BindUtils.findHolderById(bindHolders, id);
    }

    @Override
    public boolean onBindFind(BindHolder holder) {
        return false;
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
            Dialog obj = con.newInstance(getContext());
            if (obj instanceof LororDialog) {
                ((LororDialog) obj).putIntent(intent);
            } else if (intent.getFlags() != Intent.FLAG_ACTIVITY_NO_USER_ACTION) {
                Toast.makeText(getContext(), "你开启的弹窗不是LororDialog，无法传递intent，如不需传递intent可以设置flags为FLAG_ACTIVITY_NO_USER_ACTION以忽略此信息。", Toast.LENGTH_SHORT).show();
            }
            obj.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "开启弹窗失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void startDialogForResult(Intent intent, final int requestCode) {
        try {
            Class classType = Class.forName(intent.getComponent().getClassName());
            Constructor<Dialog> con = classType.getConstructor(Context.class);
            Dialog obj = con.newInstance(getContext());
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
                Toast.makeText(getContext(), "你开启的弹窗不是LororDialog，无法以forResult方式开启。", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "开启弹窗失败", Toast.LENGTH_SHORT).show();
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
                if (holder.getWhen() == AutoRunHolder.USERCALL && holder.getMethodName().equals(methodName)) {
                    AutoRunUtil.runAutoRunHolders(autoRunHolders, this);
                    break;
                }
            }
        }
    }

    @Override
    public void run(int thread, Runnable runnable) {
        if (thread == AutoRunHolder.MAINTHREAD) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                runnable.run();
            } else {
                if (handler == null) {
                    handler = new Handler();
                }
                handler.post(runnable);
            }
        } else {
            new Thread(runnable).start();
        }
    }
}
