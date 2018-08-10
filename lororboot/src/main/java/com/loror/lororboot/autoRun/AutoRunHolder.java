package com.loror.lororboot.autoRun;

import java.lang.reflect.Method;

public class AutoRunHolder {
    //切入点
    public static final int USERCALL = 0;
    public static final int AFTERONCREATE = 1;

    //关联型运行
    public static final int BEFOREMETHOD = 2;
    public static final int AFTERMETHOD = 3;

    //运行所在线程
    public static final int MAINTHREAD = 0;
    public static final int NEWTHREAD = 1;

    protected int when;
    protected String methodName;
    protected String relationMethod;
    protected int thread;
    protected Method method;
    protected AutoRunHolder previous;
    protected AutoRunHolder next;

    protected AutoRunHolder getLinkHead() {
        AutoRunHolder holder = this;
        while (holder.previous != null) {
            holder = holder.previous;
        }
        return holder;
    }

    //添加到链表头
    protected void addPrevious(AutoRunHolder previous) {
        AutoRunHolder holder = this;
        while (holder.previous != null) {
            holder = holder.previous;
        }
        holder.previous = previous;
    }

    //插入到链表当前位置与前一位置之间
    protected void insetPrevious(AutoRunHolder previous) {
        AutoRunHolder previousTemp = this.previous;
        this.previous = previous;
        this.previous.previous = previousTemp;
    }

    //添加到链表尾
    protected void addNext(AutoRunHolder next) {
        AutoRunHolder holder = this;
        while (holder.next != null) {
            holder = holder.next;
        }
        holder.next = next;
    }

    //插入到链表当前位置与下一位置之间
    protected void insetNext(AutoRunHolder next) {
        AutoRunHolder nextTemp = this.next;
        this.next = next;
        this.next.next = nextTemp;
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
}
