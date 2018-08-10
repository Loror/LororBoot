package com.loror.lororboot.autoRun;

import com.loror.lororboot.annotation.AutoRun;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AutoRunUtil {
    public static List<AutoRunHolder> findAutoRunHolders(AutoRunAble autoRunAble) {
        List<AutoRunHolder> penetrations = new ArrayList<>();//切入点
        List<AutoRunHolder> relations = new ArrayList<>();//关系点
        List<String> methodNames = new ArrayList<>();//方法名字
        Method[] methods = autoRunAble.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            AutoRun run = (AutoRun) method.getAnnotation(AutoRun.class);
            if (run != null) {
                AutoRunHolder holder = new AutoRunHolder();
                holder.when = run.when();
                holder.methodName = method.getName();
                holder.relationMethod = run.relationMethod().length() == 0 ? null : run.relationMethod();
                holder.thread = run.thread();
                holder.method = method;
                if (holder.when == AutoRunHolder.USERCALL || holder.when == AutoRunHolder.AFTERONCREATE) {
                    penetrations.add(holder);
                    methodNames.add(holder.methodName);
                } else if (holder.relationMethod != null) {
                    relations.add(holder);
                    methodNames.add(holder.methodName);
                }
            }
        }
        List<AutoRunHolder> all = new ArrayList<>(penetrations);//有效关系点
        for (int i = 0; i < relations.size(); i++) {
            AutoRunHolder holder = relations.get(i);
            if (methodNames.contains(holder.relationMethod)) {
                all.add(holder);
            }
        }//有效关系点归总
        while (all.size() > 0) {
            List<AutoRunHolder> remove = new ArrayList<>();//已链接
            for (int i = 0; i < all.size(); i++) {
                AutoRunHolder holder = all.get(i);
                for (int j = 0; j < penetrations.size(); j++) {
                    AutoRunHolder head = penetrations.get(j).getLinkHead();//链表头
                    do {
                        if (head.methodName.equals(holder.relationMethod)) {
                            remove.add(holder);
                            if (holder.getWhen() == AutoRunHolder.BEFOREMETHOD) {
                                head.insetPrevious(holder);
                            } else if (holder.getWhen() == AutoRunHolder.AFTERMETHOD) {
                                head.addNext(holder);
                            }//建立切入点链表
                        }
                    } while ((head = head.next) != null);
                }
            }
            all.removeAll(remove);
        }
        return penetrations;
    }
}
