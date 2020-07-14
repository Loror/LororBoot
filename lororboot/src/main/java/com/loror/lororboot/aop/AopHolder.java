package com.loror.lororboot.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class AopHolder {

    protected int when;
    protected String methodName;
    protected String relationMethod;
    protected int thread;
    protected int delay;
    protected Method method;
    protected AopHolder previous;
    protected List<AopHolder> next;

    public AopHolder getLinkHead() {
        AopHolder holder = this;
        while (holder.previous != null) {
            holder = holder.previous;
        }
        return holder;
    }

    //插入到链当前位置与前一位置之间
    protected void insetPrevious(AopHolder previous) {
        if (this.previous != null) {
            AopHolder previousTemp = this.previous;
            previousTemp.next.remove(this);
            previousTemp.next.add(previous);
            previous.previous = previousTemp;
        }
        this.previous = previous;
        if (previous.next == null) {
            previous.next = new LinkedList<>();
        }
        if (!previous.next.contains(this)) {
            previous.next.add(this);
        }
    }

    //添加到链尾
    protected void addNext(AopHolder next) {
        next.previous = this;
        if (this.next == null) {
            this.next = new LinkedList<>();
        }
        if (!this.next.contains(next)) {
            this.next.add(next);
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public Method getMethod() {
        return method;
    }

    public int getWhen() {
        return when;
    }

    public int getThread() {
        return thread;
    }

    public int getDelay() {
        return delay;
    }

    public Annotation[] getAnnotations() {
        return method == null ? null : method.getAnnotations();
    }
}
