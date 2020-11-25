package com.loror.lororboot.startable;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.loror.lororUtil.dataBus.DataBus;
import com.loror.lororUtil.view.ViewUtil;
import com.loror.lororboot.annotation.RequestPermission;
import com.loror.lororboot.annotation.RequestTime;
import com.loror.lororboot.bind.BindAble;
import com.loror.lororboot.bind.BindHolder;
import com.loror.lororboot.bind.BindUtils;
import com.loror.lororboot.bind.DataChangeAble;
import com.loror.lororboot.dataBus.DataBusUtil;
import com.loror.lororboot.dataChange.DataChangeUtils;
import com.loror.lororboot.views.BindAbleBannerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LororActivity extends AppCompatActivity implements StartDialogAble, DataChangeAble {

    protected Context context = this;

    protected static Handler handler = new Handler();
    private WeakReference<LororActivity> weakReference;
    private Runnable bindRunnable;
    private boolean bindAbleAutoRefresh = true;
    private final List<BindHolder> bindHolders = new LinkedList<>();
    private final List<BindAble> registedBinders = new ArrayList<>();
    private boolean paused, released;

    private int requestCode;
    private DataBusUtil dataBusUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RequestPermission permission = getClass().getAnnotation(RequestPermission.class);
        if (permission != null && permission.when() == RequestTime.ONCREATE) {
            requestPermissions(permission);
        }
        dataBusUtil = new DataBusUtil(this, this);
        dataBusUtil.register();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        updateBind(this);
        startBinder();
    }

    @Override
    protected void onResume() {
        paused = false;
        RequestPermission permission = getClass().getAnnotation(RequestPermission.class);
        if (permission != null && permission.when() == RequestTime.ONRESUME) {
            requestPermissions(permission);
        }
        super.onResume();
        //banner恢复滚动
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
        paused = true;
        //banner停止滚动
        for (BindHolder bindHolder : bindHolders) {
            if (bindHolder.getView() instanceof BindAbleBannerView) {
                ((BindAbleBannerView) bindHolder.getView()).stopScrol();
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        bindHolders.clear();
        release();
        super.onDestroy();
    }

    //用于适配部分oppo手机finish后不调用onDestroy生命周期的问题
    @Override
    public void finish() {
        release();
        super.finish();
    }

    protected void release() {
        released = true;
        LaunchModeDialog.destroyDialogs(this);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof LororFragment) {
                    ((LororFragment) fragment).release();
                }
            }
        }
        if (dataBusUtil != null) {
            dataBusUtil.unRegister();
        }
    }

    public void sendDataToBus(String name, Intent data) {
        DataBus.notifyReceivers(name, data, this);
    }

    public void registerBinder(BindAble bindAble) {
        if (!registedBinders.contains(bindAble)) {
            registedBinders.add(bindAble);
            startBinder();
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

    public void setBindAbleAutoRefresh(boolean bindAbleAutoRefresh) {
        this.bindAbleAutoRefresh = bindAbleAutoRefresh;
        if (bindAbleAutoRefresh) {
            startBinder();
        }
    }

    public boolean isBindAbleAutoRefresh() {
        return bindAbleAutoRefresh;
    }

    protected void startBinder() {
        if (bindAbleAutoRefresh && bindRunnable == null) {
            bindRunnable = new Runnable() {
                @Override
                public void run() {
                    LororActivity activity = weakReference == null ? null : weakReference.get();
                    if (activity != null) {
                        if (activity.released) {
                            activity.bindRunnable = null;
                            return;
                        }
                        if (activity.bindHolders.size() == 0 && activity.registedBinders.size() == 0) {
                            activity.bindRunnable = null;
                            return;
                        } // 无需开启刷新
                        if (!activity.isFinishing()) {
                            activity.changeState(null);
                            activity.changChildBinderState();
                            if (activity.bindAbleAutoRefresh) {
                                handler.postDelayed(activity.bindRunnable, activity.paused ? 500 : 30);
                            } else {
                                activity.bindRunnable = null;
                            }
                        } else {
                            handler.removeCallbacks(activity.bindRunnable);
                            activity.bindRunnable = null;
                            activity.bindHolders.clear();
                            activity.registedBinders.clear();
                        }
                    }
                }
            };
            handler.post(bindRunnable);
        }
    }

    protected void changChildBinderState() {
        int size = registedBinders.size();
        for (int i = 0; i < size; i++) {
            registedBinders.get(i).changeState(null);
        }
    }

    @Override
    public final void updateBind(Object tag) {
        weakReference = new WeakReference<>(this);
        BindUtils.findBindHoldersAndInit(bindHolders, this);
        ViewUtil.click(this);
    }

    @Override
    public void changeState(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
        BindUtils.showBindHolders(bindHolders, this);
    }

    @Override
    public void event(BindHolder holder, String oldValue, String newValue) {

    }

    @Override
    public void setData(int id, Object value) {
        DataChangeUtils.setData(id, value, null, bindHolders, this);
    }

    public void notifyListDataChangeById(@IdRes int id) {
        DataChangeUtils.notifyListDataChangeById(id, null, bindHolders, this);
    }

    private void requestPermissions(RequestPermission permission) {
        String[] requests = permission.value();
        boolean anyway = permission.requestAnyway();
        requestPermissions(requests, anyway);
    }

    /**
     * 动态申请权限
     */
    public void requestPermission(String permission) {
        requestPermission(permission, false);
    }

    /**
     * 动态申请权限
     */
    public void requestPermissions(String[] permission) {
        requestPermissions(permission, false);
    }

    /**
     * 动态申请权限
     */
    public void requestPermission(String permission, boolean requestAnyway) {
        requestPermissions(new String[]{permission}, requestAnyway);
    }

    /**
     * 动态申请权限
     */
    public void requestPermissions(String[] permission, boolean requestAnyway) {
        List<String> requests = new ArrayList<>();
        for (int i = 0; i < permission.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permission[i]) != PackageManager.PERMISSION_GRANTED) {
                // 权限申请曾经被用户拒绝
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission[i])) {
                    if (requestAnyway) {
                        requests.add(permission[i]);
                    } else {
                        onPermissionsResult(permission[i], false);
                    }
                } else {
                    requests.add(permission[i]);
                }
            } else {
                onPermissionsResult(permission[i], true);
            }
        }

        if (requests.size() > 0) {
            // 进行权限请求
            ActivityCompat.requestPermissions(this, requests.toArray(new String[0]), requestCode);
            requestCode++;
        }
    }

    /**
     * 是否有权限
     */
    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 请求权限回调
     */
    public void onPermissionsResult(String permission, boolean success) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            boolean success = false;
            // 如果请求被拒绝，那么通常grantResults数组为空
            if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                success = true;
            }
            onPermissionsResult(permission, success);
        }
    }

    @Override
    public void startDialog(Intent intent) {
        try {
            Class classType = Class.forName(intent.getComponent().getClassName());
            Dialog obj = LaunchModeDialog.createDialog(classType, this);
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
            Dialog obj = LaunchModeDialog.createDialog(classType, this);
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

}
