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

        final AopRunner aopRunner = new AopRunner().setAop(aop);
        aopRunner.call(penetration.getLinkHead(), null, aopAgent);
    }

}
