package com.loror.lororboot.aop;

import android.os.Handler;
import android.os.Looper;

import com.loror.lororUtil.flyweight.ObjectPool;
import com.loror.lororboot.annotation.RunThread;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class AopClient {

    private Object aop;
    private List<AopHolder> aopHolders = new ArrayList<>();

    public AopClient(Object aop) {
        this.aop = aop;
        aopHolders.addAll(AopUtil.findAutoRunHolders(aop));
    }

    /**
     * 执行所有节点
     */
    public void runAll() {
        for (AopHolder penetration : aopHolders) {
            run(penetration);
        }
    }

    /**
     * 通过节点名称执行
     */
    public void runByPenetration(String methodName) {
        AopHolder penetration = AopUtil.findHolderByName(methodName, aopHolders);
        run(penetration);
    }

    private void run(AopHolder penetration) {
        if (penetration == null) {
            return;
        }
        final Object[] result = new Object[1];
        final AopHolder[] head = new AopHolder[]{penetration.getLinkHead()};
        call(head[0].thread, head[0].delay, new Runnable() {
            @Override
            public void run() {
                head[0].getMethod().setAccessible(true);
                try {
                    Class<?>[] params = head[0].method.getParameterTypes();
                    if (params == null || params.length == 0) {
                        result[0] = head[0].method.invoke(aop);
                    } else if (params.length == 1) {
                        try {
                            result[0] = head[0].method.invoke(aop, result[0]);
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException(head[0].methodName + "方法所需参数与其绑定的前一个方法"
                                    + (head[0].previous != null ? head[0].previous.methodName : "") + "返回参数不匹配");
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
                    call(head[0].thread, head[0].delay, this);
                }
            }
        });
    }

    private void call(@RunThread int thread, final int delay, final Runnable runnable) {
        Handler handler = ObjectPool.getInstance().getHandler();
        if (thread == RunThread.MAINTHREAD) {
            if (delay > 0) {
                handler.postDelayed(runnable, delay);
            } else {
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    runnable.run();
                } else {
                    handler.post(runnable);
                }
            }
        } else if (thread == RunThread.NEWTHREAD) {
            if (delay > 0) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runnable.run();
                    }
                }.start();
            } else {
                new Thread(runnable).start();
            }
        } else {
            if (delay > 0) {
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    handler.postDelayed(runnable, delay);
                } else {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runnable.run();
                }
            } else {
                runnable.run();
            }
        }
    }

}
