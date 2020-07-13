package com.loror.lororboot.aop;

import java.lang.reflect.Method;

public class AopHolder {

    protected int when;
    protected String methodName;
    protected String relationMethod;
    protected int thread;
    protected int delay;
    protected Method method;
    protected AopHolder previous;
    protected AopHolder next;

    public AopHolder getLinkHead() {
        AopHolder holder = this;
        while (holder.previous != null) {
            holder = holder.previous;
        }
        return holder;
    }

    //添加到链表头
    protected void addPrevious(AopHolder previous) {
        AopHolder holder = this;
        while (holder.previous != null) {
            holder.previous.next = holder;
            holder = holder.previous;
        }
        previous.next = holder;
        holder.previous = previous;
    }

    //插入到链表当前位置与前一位置之间
    protected void insetPrevious(AopHolder previous) {
        AopHolder previousTemp = this.previous;
        this.previous = previous;
        this.previous.previous = previousTemp;
        previous.next = this;
    }

    //添加到链表尾
    protected void addNext(AopHolder next) {
        AopHolder holder = this;
        while (holder.next != null) {
            holder.next.previous = holder;
            holder = holder.next;
        }
        next.previous = holder;
        holder.next = next;
    }

    //插入到链表当前位置与下一位置之间
    protected void insetNext(AopHolder next) {
        AopHolder nextTemp = this.next;
        this.next = next;
        this.next.next = nextTemp;
        next.previous = this;
    }

    public AopHolder getPrevious() {
        return previous;
    }

    public AopHolder getNext() {
        return next;
    }

    public int getWhen() {
        return when;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getRelationMethod() {
        return relationMethod;
    }

    public int getThread() {
        return thread;
    }

    public Method getMethod() {
        return method;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }
}
