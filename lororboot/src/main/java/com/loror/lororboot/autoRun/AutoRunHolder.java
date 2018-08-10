package com.loror.lororboot.autoRun;

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
    protected String name;
    protected String relationMethod;
    protected int thread;

    public int getWhen() {
        return when;
    }

    public String getName() {
        return name;
    }

    public String getRelationMethod() {
        return relationMethod;
    }

    public int getThread() {
        return thread;
    }
}
