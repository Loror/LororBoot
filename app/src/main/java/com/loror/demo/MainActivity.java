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
import com.loror.lororboot.autoRun.AutoRunHolder;
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
        notifyListDataChangeById(R.id.listView);
        listBanners.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1528450620939&di=535a27dfdf37d32e9a3aab71a1b6da4c&imgtype=0&src=http%3A%2F%2Fimg.taopic.com%2Fuploads%2Fallimg%2F121019%2F234917-121019231h258.jpg");
        listBanners.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1528450648908&di=a1293186949b6145edb76256bb20ac54&imgtype=0&src=http%3A%2F%2Fimg07.tooopen.com%2Fimages%2F20170316%2Ftooopen_sy_201956178977.jpg");
        notifyListDataChangeById(R.id.banner);
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

    @AutoRun(when = RunTime.AFTERONCREATE, thread = RunThread.NEWTHREAD)
    public void initData(String result) {
        Log.e("RESULT__", result + " ");
        Log.e("RESULT__", "initData" + (Looper.getMainLooper() == Looper.myLooper() ? "-主线程" : "-子线程"));
    }

    @AutoRun(when = RunTime.BEFOREMETHOD, relationMethod = "initData", thread = RunThread.MAINTHREAD)
    public String before1() {
        Log.e("RESULT__", "before1" + (Looper.getMainLooper() == Looper.myLooper() ? "-主线程" : "-子线程"));
        return "传递参数，需和下一执行方法形参类型相同";
    }

    @AutoRun(when = RunTime.BEFOREMETHOD, relationMethod = "before1", thread = RunThread.NEWTHREAD)
    public void before2() {
        Log.e("RESULT__", "before2" + (Looper.getMainLooper() == Looper.myLooper() ? "-主线程" : "-子线程"));
    }

    @AutoRun(when = RunTime.AFTERMETHOD, relationMethod = "initData", thread = RunThread.NEWTHREAD)
    public void after1() {
        Log.e("RESULT__", "after1" + (Looper.getMainLooper() == Looper.myLooper() ? "-主线程" : "-子线程"));
    }

    @AutoRun(when = RunTime.AFTERMETHOD, relationMethod = "after1", thread = RunThread.MAINTHREAD)
    public void after2() {
        Log.e("RESULT__", "after2" + (Looper.getMainLooper() == Looper.myLooper() ? "-主线程" : "-子线程"));
    }

}
