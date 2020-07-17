package com.loror.lororboot.aop;

import android.support.annotation.CallSuper;

public interface AopAgent {

    void onAgent(AopHolder aopHolder, AopAgentCall aopAgentCall);

    abstract class AopAgentCall {

        private Object param, result;
        private AopRunner.GlobalData globalData;

        public AopAgentCall setParam(Object param) {
            this.param = param;
            return this;
        }

        public AopAgentCall setResult(Object result) {
            this.result = result;
            return this;
        }

        public AopAgentCall setGlobalData(AopRunner.GlobalData globalData) {
            this.globalData = globalData;
            return this;
        }

        public Object getParam() {
            return param;
        }

        public Object getResult() {
            return result;
        }

        public AopRunner.GlobalData getGlobalData() {
            return globalData;
        }

        @CallSuper
        public void callOn() {
            call();
            next();
        }

        public abstract void call();

        public abstract void next();
    }
}
