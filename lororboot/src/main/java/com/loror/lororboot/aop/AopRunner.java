package com.loror.lororboot.aop;

import java.lang.reflect.InvocationTargetException;

public abstract class AopRunner implements Runnable {

    public static class AopRunnerSource {
        private AopHolder aopHolder;
        private Object aop;
        private Object param;

        public AopRunnerSource setAopHolder(AopHolder aopHolder) {
            this.aopHolder = aopHolder;
            return this;
        }

        public AopHolder getAopHolder() {
            return aopHolder;
        }

        public AopRunnerSource setAop(Object aop) {
            this.aop = aop;
            return this;
        }

        public AopRunnerSource setParam(Object param) {
            this.param = param;
            return this;
        }

        public Object getParam() {
            return param;
        }
    }

    private AopRunnerSource source;

    public void setSource(AopRunnerSource source) {
        this.source = source;
    }

    public AopRunnerSource getSource() {
        return source;
    }

    public Object call() {
        Object result = null;
        if (source != null) {
            source.aopHolder.method.setAccessible(true);
            try {
                Class<?>[] params = source.aopHolder.method.getParameterTypes();
                if (params == null || params.length == 0) {
                    result = source.aopHolder.method.invoke(source.aop);
                } else if (params.length == 1) {
                    try {
                        result = source.aopHolder.method.invoke(source.aop, source.param);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(source.aopHolder.methodName + "方法所需参数与其绑定的前一个方法"
                                + (source.aopHolder.previous != null ? source.aopHolder.previous.methodName : "") + "返回参数不匹配");
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
}
