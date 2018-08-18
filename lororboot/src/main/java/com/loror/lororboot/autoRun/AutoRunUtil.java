package com.loror.lororboot.autoRun;

import com.loror.lororboot.annotation.AutoRun;
import com.loror.lororboot.annotation.RunTime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AutoRunUtil {
    //找到所有AutoRun
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
                if (holder.when == RunTime.USERCALL || holder.when == RunTime.AFTERONCREATE) {
                    penetrations.add(holder);
                    methodNames.add(holder.methodName);
                } else if (holder.relationMethod != null) {
                    relations.add(holder);
                    methodNames.add(holder.methodName);
                }
            }
        }
        List<AutoRunHolder> all = new ArrayList<>();//有效关系点
        for (int i = 0; i < relations.size(); i++) {
            AutoRunHolder holder = relations.get(i);
            if (methodNames.contains(holder.relationMethod)) {
                all.add(holder);
            }
        }//有效关系点归总
        while (all.size() > 0) {
            List<AutoRunHolder> remove = new ArrayList<>();//已链接
            int size = all.size();
            for (int i = 0; i < size; i++) {
                AutoRunHolder holder = all.get(i);
                int penetrationsSize = penetrations.size();
                for (int j = 0; j < penetrationsSize; j++) {
                    AutoRunHolder head = penetrations.get(j).getLinkHead();//链表头
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

    //运行AutoRun
    public static void runAutoRunHolders(List<AutoRunHolder> penetrations, final AutoRunAble autoRunAble) {
        int size = penetrations.size();
        for (int i = 0; i < size; i++) {
            final Object[] result = new Object[1];
            final AutoRunHolder[] head = new AutoRunHolder[]{penetrations.get(i).getLinkHead()};
            autoRunAble.run(head[0].thread, new Runnable() {
                @Override
                public void run() {
                    head[0].getMethod().setAccessible(true);
                    try {
                        Class<?>[] parmas = head[0].method.getParameterTypes();
                        if (parmas == null || parmas.length == 0) {
                            result[0] = head[0].method.invoke(autoRunAble);
                        } else if (parmas.length == 1) {
                            try {
                                result[0] = head[0].method.invoke(autoRunAble, result[0]);
                            } catch (IllegalArgumentException e) {
                                throw new IllegalArgumentException(head[0].methodName + "方法所需参数与其绑定的前一个方法" + (head[0].previous != null ? head[0].previous.methodName : "") + "返回参数不匹配");
                            }
                        } else {
                            throw new IllegalArgumentException("不允许方法包含两个及以上参数");
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    if (head[0].next != null) {
                        head[0] = head[0].next;
                        autoRunAble.run(head[0].thread, this);
                    }
                }
            });
        }
    }
}
