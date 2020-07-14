package com.loror.lororboot.aop;

import android.support.annotation.CallSuper;

public interface AopAgent {

    void onAgent(AopHolder aopHolder, AopAgentCall aopAgentCall);

    abstract class AopAgentCall {

        private Object param, result;

        protected AopAgentCall setParam(Object param) {
            this.param = param;
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
            result = run();
        }

        protected abstract Object run();
    }
}
