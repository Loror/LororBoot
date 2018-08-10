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
}
