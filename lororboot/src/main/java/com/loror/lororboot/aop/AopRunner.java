package com.loror.lororboot.aop;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class AopRunner {

    /**
     * 同一执行链条上共用数据
     */
    public static class GlobalData {
        public int arg1, arg2;
        private HashMap<String, Object> data;

        public void put(String key, Object value) {
            if (data == null) {
                data = new HashMap<>();
            }
            data.put(key, value);
        }

        public Object get(String key) {
            if (data == null) {
                return null;
            }
            return data.get(key);
        }
    }

    private Object aop;

    public AopRunner setAop(Object aop) {
        this.aop = aop;
        return this;
    }

    private Object run(AopHolder aopHolder, Object param) {
        Object result = null;
        if (aopHolder != null) {
            aopHolder.method.setAccessible(true);
            try {
                Class<?>[] paramTypes = aopHolder.method.getParameterTypes();
                if (paramTypes == null || paramTypes.length == 0) {
                    result = aopHolder.method.invoke(aop);
                } else if (paramTypes.length == 1) {
                    try {
                        result = aopHolder.method.invoke(aop, param);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(aopHolder.previous != null ? (aopHolder.methodName + "方法所需参数与其绑定的前一个方法"
                                + aopHolder.previous.methodName + "返回参数不匹配") :
                                (aopHolder.methodName + "方法所需参数错误"));
                    }
                } else if (param != null && param.getClass().isArray()) {
                    try {
                        result = aopHolder.method.invoke(aop, (Object[]) param);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(aopHolder.previous != null ? (aopHolder.methodName + "方法所需参数与其绑定的前一个方法"
                                + aopHolder.previous.methodName + "返回参数不匹配") :
                                (aopHolder.methodName + "方法所需参数错误"));
                    }
                } else {
                    throw new IllegalArgumentException("不允许方法包含两个及以上参数");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void call(final AopHolder aopHolder, final Object param, final AopAgent aopAgent, final GlobalData globalData) {
        AopUtil.run(new AopUtil.AopHolderRunnable(aopHolder) {

            @Override
            public void run() {
                if (aopAgent != null) {
                    aopAgent.onAgent(aopHolder, new AopAgent.AopAgentCall() {

                        @Override
                        public void call() {
                            setResult(AopRunner.this.run(aopHolder, getParam()));
                        }

                        @Override
                        public void next() {
                            if (aopHolder.next != null) {
                                for (AopHolder aopHolder : aopHolder.next) {
                                    AopRunner.this.call(aopHolder, getResult(), aopAgent, globalData);
                                }
                            }
                        }
                    }.setParam(param).setGlobalData(globalData));
                } else {
                    Object result = AopRunner.this.run(aopHolder, param);
                    if (aopHolder.next != null) {
                        for (AopHolder aopHolder : aopHolder.next) {
                            call(aopHolder, result, aopAgent, globalData);
                        }
                    }
                }
            }
        });

    }

}
