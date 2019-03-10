package com.loror.lororboot.startable;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import com.loror.lororboot.annotation.LaunchMode;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LaunchModeDialog {

    /**
     * 缓存的Dialog
     */
    private static HashMap<Activity, List<Dialog>> savedDialog = new HashMap<>();

    /**
     * 创建Dialog
     */
    public static Dialog createDialog(Class classType, Context activity) throws Exception {
        if (activity instanceof Activity) {
            LaunchMode mode = activity.getClass().getAnnotation(LaunchMode.class);
            int type = mode == null ? LaunchMode.STANDARD : mode.value();
            if (type == LaunchMode.SINGLEINACTIVITY) {
                List<Dialog> dialogs = savedDialog.get(activity);
                if (dialogs != null) {
                    for (Dialog dialog : dialogs) {
                        if (dialog.getClass() == classType) {
                            return dialog;
                        }
                    }
                } else {
                    dialogs = new ArrayList<>();
                    savedDialog.put((Activity) activity, dialogs);
                }
                Constructor<Dialog> con = classType.getConstructor(Context.class);
                Dialog obj = con.newInstance(activity);
                dialogs.add(obj);
                return obj;
            }
        }
        Constructor<Dialog> con = classType.getConstructor(Context.class);
        return con.newInstance(activity);
    }

    /**
     * 移除Dialog
     */
    public static void destroyDialogs(Activity activity) {
        savedDialog.remove(activity);
    }
}
