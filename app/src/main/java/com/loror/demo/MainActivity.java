package com.loror.demo;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.loror.lororUtil.view.Click;
import com.loror.lororUtil.view.ItemClick;
import com.loror.lororboot.annotation.AutoRun;
import com.loror.lororboot.annotation.Bind;
import com.loror.lororboot.annotation.RunThread;
import com.loror.lororboot.annotation.RunTime;
import com.loror.lororboot.bind.BindHolder;
import com.loror.lororboot.startable.LororActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends LororActivity {

    /**
     * 修改变量值会自动重新显示内容
     */
    @Bind(id = R.id.checkBox)
    boolean checked = true;
    @Bind(id = R.id.textView)
    String text = "绑定TextView显示";
    @Bind(id = R.id.editText)
    String doubleBindText = "绑定EditText内容";
    @Bind(id = R.id.imageView, imagePlace = R.mipmap.ic_launcher)
    String image = "http://img.zcool.cn/community/0117e2571b8b246ac72538120dd8a4.jpg@1280w_1l_2o_100sh.jpg";
    @Bind(id = R.id.listView)
    List<ListItem> listItems = new ArrayList<>();
    @Bind(id = R.id.banner, imagePlace = R.mipmap.ic_launcher)
    List<String> listBanners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i = 0; i < 10; i++) {
            ListItem item = new ListItem();
            item.text = "第" + i + "行";
            listItems.add(item);
        }
        notifyListDataChangeById(R.id.listView);//若list的size发生变化，不调用该方法也会自动刷新，如仅修改了list中对象属性而size未改变应主动调用该方法通知刷新
        listBanners.add("http://img05.tooopen.com/images/20150820/tooopen_sy_139205349641.jpg");
        listBanners.add("http://i0.hdslb.com/bfs/archive/83a12dcbe6401c27e16a3333b1eba91191ac3c8e.jpg");
        notifyListDataChangeById(R.id.banner);
    }

    @Override
    public boolean onBindFind(BindHolder holder) {
        //Bind注解被找到时调用，可通过holder.getView().getId()比较id来确认是哪一个Bind，若返回true会拦截后续自动显示事件
        return super.onBindFind(holder);
    }

    @Override
    public void event(BindHolder holder, String oldValue, String newValue) {
        //Bind注解若注册了event属性，在变量改变时会自动调用该方法,可比较holder.getEvent()值来处理相应事件
    }

    @Click(id = R.id.button)
    public void buttonClick(View view) {
        Toast.makeText(this, doubleBindText, Toast.LENGTH_SHORT).show();
    }

    @ItemClick(id = R.id.listView)
    public void listViewClick(View view, int position) {
        Toast.makeText(this, "第" + position + "行点击", Toast.LENGTH_SHORT).show();
    }

    @ItemClick(id = R.id.banner)
    public void bannerClick(View view, int position) {
        Toast.makeText(this, "第" + position + "横幅点击", Toast.LENGTH_SHORT).show();
    }

    //RunTime.AFTERONCREATE,oncreate后自动执行;RunTime.BEFOREONDESTROY,ondestroy前自动执行;RunTime.USERCALL,用户主动通过切入点方法名调用
    @AutoRun(when = RunTime.AFTERONCREATE, thread = RunThread.LASTTHREAD)
    public void initData(String result) {
        Log.e("AUTO_RUN", result + " ");
        Log.e("AUTO_RUN", "initData" + (Looper.getMainLooper() == Looper.myLooper() ? "-主线程" : "-子线程"));
    }

    @AutoRun(when = RunTime.BEFOREMETHOD, relationMethod = "initData", thread = RunThread.NEWTHREAD)
    public String beforeCreate() {
        Log.e("AUTO_RUN", "beforeCreate" + (Looper.getMainLooper() == Looper.myLooper() ? "-主线程" : "-子线程"));
        return "传递参数，需和下一执行方法形参类型相同";
    }

    @AutoRun(when = RunTime.AFTERMETHOD, relationMethod = "initData", thread = RunThread.MAINTHREAD)
    public void afterCreate() {
        Log.e("AUTO_RUN", "afterCreate" + (Looper.getMainLooper() == Looper.myLooper() ? "-主线程" : "-子线程"));
    }

}
