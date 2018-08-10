package com.loror.lororboot.autoRun;

import com.loror.lororboot.annotation.AutoRun;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AutoRunUtil {
    public static void findAutoRunHolders(AutoRunAble autoRunAble) {
        List<AutoRunHolder> penetrations = new ArrayList<>();
        List<AutoRunHolder> relations = new ArrayList<>();
        Method[] methods = autoRunAble.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            AutoRun run = (AutoRun) method.getAnnotation(AutoRun.class);
            if (run != null) {
                AutoRunHolder holder = new AutoRunHolder();
                holder.when = run.when();
                holder.name = run.name().length() == 0 ? null : run.name();
                holder.methodName = method.getName();
                holder.relationMethod = run.relationMethod().length() == 0 ? null : run.relationMethod();
                holder.thread = run.thread();
                holder.method = method;
                if (holder.when == AutoRunHolder.USERCALL || holder.when == AutoRunHolder.AFTERONCREATE) {
                    penetrations.add(holder);
                } else {
                    relations.add(holder);
                }
            }
        }
    }
}
