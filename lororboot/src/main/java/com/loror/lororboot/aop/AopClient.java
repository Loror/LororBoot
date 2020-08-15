package com.loror.lororboot.aop;

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
            run(penetration, null);
        }
    }

    /**
     * 通过节点名称执行
     */
    public void runByName(String methodName) {
        runByName(methodName, null);
    }

    /**
     * 通过节点名称执行
     */
    public boolean runByName(String methodName, Object param) {
        AopHolder penetration = AopUtil.findHolderByName(methodName, aopHolders);
        return run(penetration, param);
    }

    private boolean run(AopHolder penetration, Object param) {
        if (penetration == null) {
            return false;
        }

        final AopRunner aopRunner = new AopRunner().setAop(aop);
        aopRunner.call(penetration.getLinkHead(), param, aopAgent, aopAgent == null ? null : new AopRunner.GlobalData());
        return true;
    }

}
