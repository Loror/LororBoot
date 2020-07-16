package com.loror.lororboot.aop;

import android.support.annotation.CallSuper;

public interface AopAgent {

    void onAgent(AopHolder aopHolder, AopAgentCall aopAgentCall);

    abstract class AopAgentCall {

        private Object param, result;

        public AopAgentCall setParam(Object param) {
            this.param = param;
            return this;
        }

        public AopAgentCall setResult(Object result) {
            this.result = result;
            return this;
        }

        public Object getParam() {
            return param;
        }

        public Object getResult() {
            return result;
        }

        @CallSuper
        public void callOn() {
            call();
            next();
        }

        public abstract void call();

        public abstract void next() ;
    }
}
