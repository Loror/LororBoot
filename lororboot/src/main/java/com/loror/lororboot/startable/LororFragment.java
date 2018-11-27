package com.loror.lororboot.startable;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.loror.lororUtil.view.ViewUtil;
import com.loror.lororboot.autoRun.AutoRunAble;
import com.loror.lororboot.bind.BindHolder;
import com.loror.lororboot.bind.BindUtils;
import com.loror.lororboot.bind.DataChangeAble;
import com.loror.lororboot.dataBus.DataBus;
import com.loror.lororboot.dataChange.DataChangeUtils;
import com.loror.lororboot.views.BindAbleBannerView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

public class LororFragment extends Fragment implements StartDilogAble, DataChangeAble, AutoRunAble {

    private List<BindHolder> bindHolders = new LinkedList<>();
    private WeakReference<LororActivity> weakReference;
    private Decorater decorater;

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
        decorater = new Decorater(getActivity(), this);
        decorater.onCreate();
    }

    @Override
    public void onResume() {
        decorater.onResume();
        super.onResume();
        //banner恢复滚动
        LororActivity activity = weakReference == null ? null : weakReference.get();
        if (activity != null) {
            for (BindHolder bindHolder : bindHolders) {
                if (bindHolder.getView() instanceof BindAbleBannerView) {
                    ((BindAbleBannerView) bindHolder.getView()).startScrol();
                }
            }
        }
    }

    @Override
    public void onStart() {
        LororActivity activity = weakReference == null ? null : weakReference.get();
        if (activity != null) {
            if (bindHolders.size() > 0) {
                activity.registerBinder(this);
            }
        }
        super.onStart();
    }

    @Override
    public void onPause() {
        //banner停止滚动
        LororActivity activity = weakReference == null ? null : weakReference.get();
        if (activity != null) {
            for (BindHolder bindHolder : bindHolders) {
                if (bindHolder.getView() instanceof BindAbleBannerView) {
                    ((BindAbleBannerView) bindHolder.getView()).stopScrol();
                }
            }
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        LororActivity activity = weakReference == null ? null : weakReference.get();
        if (activity != null) {
            activity.unRegisterBinder(this);
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        bindHolders.clear();
        decorater.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void finalize() throws Throwable {
        if (decorater != null) {
            decorater.release();
        }
        super.finalize();
    }

    public void sendDataToBus(String name, Intent data) {
        DataBus.notifyReceivers(name, data, getActivity());
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
        DataChangeUtils.setData(id, value, null, bindHolders, this);
    }

    @Override
    public void setData(String fieldName, Object value) {
        DataChangeUtils.setData(fieldName, value, null, bindHolders, this);
    }

    public void notifyListDataChangeById(@IdRes int id) {
        DataChangeUtils.notifyListDataChangeById(id, null, bindHolders, this);
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

    public void runAutoRunByPenetration(String methodName) {
        decorater.runAutoRunByPenetration(methodName);
    }

    @Override
    public void run(int thread, Runnable runnable) {
        decorater.run(thread, runnable, new Handler());
    }
}
