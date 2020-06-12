package com.loror.lororboot.autoRun;

import com.loror.lororboot.annotation.Aop;
import com.loror.lororboot.annotation.RunTime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AopUtil {
    //找到所有AutoRun
    public static List<AopHolder> findAutoRunHolders(Object aop) {
        List<AopHolder> penetrations = new ArrayList<>();//切入点
        List<AopHolder> relations = new ArrayList<>();//关系点
        List<String> methodNames = new ArrayList<>();//方法名字
        Method[] methods = aop.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            Aop run = (Aop) method.getAnnotation(Aop.class);
            if (run != null) {
                AopHolder holder = new AopHolder();
                holder.when = run.when();
                holder.methodName = method.getName();
                holder.relationMethod = run.relationMethod().length() == 0 ? null : run.relationMethod();
                holder.thread = run.thread();
                holder.delay = run.delay();
                holder.method = method;
                if (holder.when == RunTime.TOP) {
                    penetrations.add(holder);
                    methodNames.add(holder.methodName);
                } else if (holder.relationMethod != null) {
                    relations.add(holder);
                    methodNames.add(holder.methodName);
                }
            }
        }
        List<AopHolder> all = new ArrayList<>();//有效关系点
        for (int i = 0; i < relations.size(); i++) {
            AopHolder holder = relations.get(i);
            if (methodNames.contains(holder.relationMethod)) {
                all.add(holder);
            }
        }//有效关系点归总
        while (all.size() > 0) {
            List<AopHolder> remove = new ArrayList<>();//已链接
            int size = all.size();
            for (int i = 0; i < size; i++) {
                AopHolder holder = all.get(i);
                int penetrationsSize = penetrations.size();
                for (int j = 0; j < penetrationsSize; j++) {
                    AopHolder head = penetrations.get(j).getLinkHead();//链表头
                    do {
                        if (head.methodName.equals(holder.relationMethod)) {
                            remove.add(holder);
                            if (holder.getWhen() == RunTime.BEFOREMETHOD) {
                                head.insetPrevious(holder);
                            } else if (holder.getWhen() == RunTime.AFTERMETHOD) {
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

    /**
     * 查找AopHolder
     */
    public static AopHolder findHolderByName(String methodName, List<AopHolder> aopHolders) {
        AopHolder aopHolder = null;
        for (AopHolder holder : aopHolders) {
            if (holder.methodName.equals(methodName)) {
                aopHolder = holder;
                break;
            }
        }
        return aopHolder;
    }
}
