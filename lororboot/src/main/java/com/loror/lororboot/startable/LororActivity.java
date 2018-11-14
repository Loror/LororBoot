package com.loror.lororboot.startable;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.widget.Toast;

import com.loror.lororUtil.view.ViewUtil;
import com.loror.lororboot.annotation.PermissionResult;
import com.loror.lororboot.annotation.RequestPermission;
import com.loror.lororboot.annotation.RequestTime;
import com.loror.lororboot.annotation.RunThread;
import com.loror.lororboot.annotation.RunTime;
import com.loror.lororboot.autoRun.AutoRunAble;
import com.loror.lororboot.autoRun.AutoRunHolder;
import com.loror.lororboot.autoRun.AutoRunUtil;
import com.loror.lororboot.bind.BindAble;
import com.loror.lororboot.bind.BindHolder;
import com.loror.lororboot.bind.BindUtils;
import com.loror.lororboot.bind.DataChangeAble;
import com.loror.lororboot.dataChange.DataChangeUtils;
import com.loror.lororboot.views.BindAbleBannerView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LororActivity extends AppCompatActivity implements StartDilogAble, DataChangeAble, AutoRunAble {
    protected Context context = this;
    private List<BindHolder> bindHolders = new LinkedList<>();
    private List<BindAble> registedBinders = new ArrayList<>();
    private WeakReference<List<BindAble>> weakReferenceBindAbleList = new WeakReference<>(registedBinders);
    private Runnable bindRunnable;
    private Handler handler;
    private List<AutoRunHolder> autoRunHolders = new ArrayList<>();
    private int createState;
    private int requestCode;
    private SparseArray<String> permissionRequestMap;
    private Method permissionResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RequestPermission permission = getClass().getAnnotation(RequestPermission.class);
        if (permission != null && permission.when() == RequestTime.ONCREATE) {
            requestPermissions(permission);
        }
        autoRunHolders = AutoRunUtil.findAutoRunHolders(this);
        createState = 1;
    }

    private void requestPermissions(RequestPermission permission) {
        Method[] methods = getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getAnnotation(PermissionResult.class) != null) {
                permissionResult = methods[i];
                break;
            }
        }
        String[] requests = permission.value();
        for (int i = 0; i < requests.length; i++) {
            requestPermission(requests[i]);
        }
    }

    @Override
    protected void onResume() {
        RequestPermission permission = getClass().getAnnotation(RequestPermission.class);
        if (permission != null && permission.when() == RequestTime.ONRESUME) {
            requestPermissions(permission);
        }
        if (createState == 1) {
            createState = 2;
            AutoRunUtil.runAutoRunHolderByPenetration(RunTime.AFTERONCREATE, autoRunHolders, this);
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
        AutoRunUtil.runAutoRunHolderByPenetration(RunTime.BEFOREONDESTROY, autoRunHolders, this);
        bindHolders.clear();
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
        DataChangeUtils.setData(id, value, null, bindHolders, this);
    }

    @Override
    public void setData(String fieldName, Object value) {
        DataChangeUtils.setData(fieldName, value, null, bindHolders, this);
    }

    public void notifyListDataChangeById(@IdRes int id) {
        DataChangeUtils.notifyListDataChangeById(id, null, bindHolders, this);
    }

    /**
     * 动态申请权限
     */
    public void requestPermission(String permission) {
        if (permissionRequestMap == null) {
            permissionRequestMap = new SparseArray<>();
        }
        int hasIt = 0;
        // 判断是否已经获得了该权限
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            // 权限申请曾经被用户拒绝
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                hasIt = 2;
            } else {
                // 进行权限请求
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
                permissionRequestMap.put(requestCode, permission);
                requestCode++;
            }
        } else {
            hasIt = 1;
        }
        if (hasIt > 0 && permissionResult != null) {
            try {
                permissionResult.invoke(this, permission, hasIt == 1);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否有权限
     */
    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionRequestMap != null) {
            String permission = permissionRequestMap.get(requestCode);
            boolean success = false;
            // 如果请求被拒绝，那么通常grantResults数组为空
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                success = true;
            }
            if (permissionResult != null) {
                try {
                    permissionResult.invoke(this, permission, success);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
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
        AutoRunUtil.runAutoRunHolderByPenetration(methodName, autoRunHolders, this);
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
