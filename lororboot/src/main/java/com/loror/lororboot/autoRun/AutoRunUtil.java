package com.loror.lororboot.autoRun;

import com.loror.lororboot.annotation.AutoRun;

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
                if (holder.when == AutoRunHolder.USERCALL || holder.when == AutoRunHolder.AFTERONCREATE) {
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

    //运行AutoRun
    public static void runAutoRunHolders(List<AutoRunHolder> penetrations, final AutoRunAble autoRunAble) {
        int size = penetrations.size();
        for (int i = 0; i < size; i++) {
            final AutoRunHolder[] head = new AutoRunHolder[]{penetrations.get(i).getLinkHead()};
            autoRunAble.run(head[0].thread, new Runnable() {
                @Override
                public void run() {
                    head[0].getMethod().setAccessible(true);
                    try {
                        head[0].getMethod().invoke(autoRunAble);
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
