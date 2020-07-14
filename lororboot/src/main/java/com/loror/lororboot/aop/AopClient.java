package com.loror.lororboot.aop;

import android.os.Handler;
import android.os.Looper;

import com.loror.lororUtil.flyweight.ObjectPool;
import com.loror.lororboot.annotation.RunThread;

import java.util.ArrayList;
import java.util.List;

public class AopClient {

    private Object aop;
    private List<AopHolder> aopHolders = new ArrayList<>();
    private AopAgent aopAgent;

    public AopClient(Object aop) {
        this.aop = aop;
        aopHolders.addAll(AopUtil.findAutoRunHolders(aop));
    }

    /**
     * 设置aop代理执行
     */
    public void setAopAgent(AopAgent aopAgent) {
        this.aopAgent = aopAgent;
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

        AopHolder aopHolder = penetration.getLinkHead();
        call(aopHolder.thread, aopHolder.delay, new AopRunner() {
            @Override
            public void run() {
                AopHolder aopHolder = getSource().getAopHolder();
                final Object[] result = new Object[1];
                if (aopAgent != null) {
                    aopAgent.onAgent(aopHolder, new AopAgent.AopAgentCall() {
                        @Override
                        protected Object run() {
                            return result[0] = call();
                        }
                    }.setParam(getSource().getParam()));
                } else {
                    result[0] = call();
                }
                if (aopHolder.next != null) {
                    for (AopHolder holder : aopHolder.next) {
                        AopClient.this.call(holder.thread, holder.delay, this, new AopRunnerSource()
                                .setAop(aop)
                                .setAopHolder(holder)
                                .setParam(result[0]));
                    }
                }
            }
        }, new AopRunner.AopRunnerSource()
                .setAop(aop)
                .setAopHolder(penetration.getLinkHead()));
    }

    private void call(@RunThread int thread, final int delay, final AopRunner runnable, final AopRunner.AopRunnerSource source) {
        Handler handler = ObjectPool.getInstance().getHandler();
        final Runnable finalRunnable = new Runnable() {
            @Override
            public void run() {
                runnable.setSource(source);
                runnable.run();
            }
        };
        if (thread == RunThread.MAINTHREAD) {
            if (delay > 0) {
                handler.postDelayed(finalRunnable, delay);
            } else {
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    finalRunnable.run();
                } else {
                    handler.post(finalRunnable);
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
                        finalRunnable.run();
                    }
                }.start();
            } else {
                new Thread(finalRunnable).start();
            }
        } else {
            if (delay > 0) {
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    handler.postDelayed(finalRunnable, delay);
                } else {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finalRunnable.run();
                }
            } else {
                finalRunnable.run();
            }
        }
    }

}
