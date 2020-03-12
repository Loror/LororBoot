package com.loror.demo;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.loror.lororUtil.http.HttpClient;
import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.http.Responce;
import com.loror.lororUtil.view.Click;
import com.loror.lororUtil.view.ItemClick;
import com.loror.lororUtil.view.ItemLongClick;
import com.loror.lororUtil.view.LongClick;
import com.loror.lororboot.annotation.AutoRun;
import com.loror.lororboot.annotation.Bind;
import com.loror.lororboot.annotation.BindAbleItemConnection;
import com.loror.lororboot.annotation.RequestPermission;
import com.loror.lororboot.annotation.RunThread;
import com.loror.lororboot.annotation.RunTime;
import com.loror.lororboot.annotation.DataRun;
import com.loror.lororboot.bind.BindHolder;
import com.loror.lororboot.dataBus.RemoteDataBusReceiver;
import com.loror.lororboot.httpApi.ApiClient;
import com.loror.lororboot.httpApi.ApiRequest;
import com.loror.lororboot.httpApi.ApiResult;
import com.loror.lororboot.httpApi.Observer;
import com.loror.lororboot.httpApi.OnRequestListener;
import com.loror.lororboot.startable.LororActivity;

import java.util.ArrayList;
import java.util.List;

//继承DataBusReceiver只能接收到同一进程发送的数据，继承RemoteDataBusReceiver可接收到同一进程和其他进程发送的消息，可根据需要选择
@RequestPermission(value = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, requestAnyway = true)
public class MainActivity extends LororActivity implements RemoteDataBusReceiver {

    /**
     * 修改变量值会自动重新显示内容
     */
    @Bind(id = R.id.checkBox)
    boolean checked = true;
    @Bind(id = R.id.textView)
    String text = "绑定TextView显示";
    @Bind(id = R.id.editText)
    String doubleBindText = "绑定EditText内容";
    @Bind(id = R.id.imageView, imagePlace = R.mipmap.ic_launcher, bitmapConverter = RoundBitmapConverter.class)
    String image;
    @Bind(id = R.id.listView)
    List<ListItem> listItems = new ArrayList<>();
    @BindAbleItemConnection(id = R.id.listView)
    OnItemClickConnect onItemClickConnect = new OnItemClickConnect() {
        @Override
        public void onItemClick(int type, int position) {
            Toast.makeText(context, "我点击了传入的接口，获得参数" + position, Toast.LENGTH_SHORT).show();
        }
    };
    @Bind(id = R.id.banner, imagePlace = R.mipmap.ic_launcher)
    List<Banner> listBanners = new ArrayList<>();

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
        initData();
    }

    private void initData() {
        new ApiClient()
                .setBaseUrl("https://www.baidu.com") //可在此设置，也可使用注解，注解优先度较高，会覆盖此处设置
                .setOnRequestListener(new OnRequestListener() {
                    @Override
                    public void onRequestBegin(HttpClient client, ApiRequest request) {
                        Log.e("RESULT_", request.getUrl() + " " + request.getParams());
                    }

                    @Override
                    public void onRequestEnd(HttpClient client, ApiResult result) {
                        result.setAccept(true);//该设置用于拦截请求返回
                        Responce responce = new Responce();
                        responce.result = "[{\"id\":1,\"name\":\"test\"}]".getBytes();
                        result.getObserver().success(JSON.parseObject(responce.toString(), result.getTypeInfo().getType()));
                    }
                })
                .create(ServerApi.class)
                .getResult("1")
                .subscribe(new Observer<List<Result>>() {
                    @Override
                    public void success(List<Result> data) {
                        Log.e("RESULT_", JSON.toJSONString(data) + " ");
                    }

                    @Override
                    public void failed(int code, Throwable e) {
                        Log.e("RESULT_", code + " = " + e);
                    }
                });
    }

    private void initView() {
        image = "http://img.zcool.cn/community/0117e2571b8b246ac72538120dd8a4.jpg@1280w_1l_2o_100sh.jpg";
        listBanners.add(new Banner("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1551797289462&di=c583d77f74705a9664e585111693e4cd&imgtype=0&src=http%3A%2F%2Fwww.pptbz.com%2Fpptpic%2FUploadFiles_6909%2F201203%2F2012031220134655.jpg"));
        listBanners.add(new Banner("http://i0.hdslb.com/bfs/archive/83a12dcbe6401c27e16a3333b1eba91191ac3c8e.jpg"));
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

    @Click(id = R.id.second)
    public void second(View view) {
        Intent data = new Intent();
        data.putExtra("data", "滞后消息送达");
        sendDataToBus("SecondActivity.sticky", data);
        startActivity(new Intent(this, SecondActivity.class));
    }

    @Click(id = R.id.dialog)
    public void dialog(View view) {
        startDialog(new Intent(this, SingleDialog.class));
    }

    @LongClick(id = R.id.dialog)
    public void dialogLong(View view) {
        Toast.makeText(this, "长按测试", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsResult(String permission, boolean success) {
        Log.e("PERMISSION", permission + " " + success);
        switch (permission) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                if (success) {
                    initView();
                }
                break;
        }
        if (!success) {
            Toast.makeText(this, "获取权限失败", Toast.LENGTH_SHORT).show();
        }
    }

    @ItemClick(id = R.id.listView)
    public void listViewClick(View view, int position) {
        Toast.makeText(this, "第" + position + "行点击", Toast.LENGTH_SHORT).show();
    }

    @ItemClick(id = R.id.banner)
    public void bannerClick(View view, int position) {
        Toast.makeText(this, "第" + position + "横幅点击", Toast.LENGTH_SHORT).show();
    }

    @ItemLongClick(id = R.id.listView)
    public void listViewLongClick(View view, int position) {
        Toast.makeText(this, "第" + position + "行长按", Toast.LENGTH_SHORT).show();
    }

    //RunTime.AFTERONCREATE,oncreate后自动执行;RunTime.BEFOREONDESTROY,ondestroy前自动执行;RunTime.USERCALL,用户主动通过切入点方法名调用
    @AutoRun(when = RunTime.AFTERONCREATE)
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

    @Override
    @DataRun(thread = RunThread.MAINTHREAD)
    public void receiveData(String name, Intent data) {
        if ("toast".equals(name)) {
            Toast.makeText(this, data.getStringExtra("msg"), Toast.LENGTH_SHORT).show();
            text = "收到消息了";
        }
        Log.e("DATA_BUS", (Looper.getMainLooper() == Looper.myLooper() ? "-主线程" : "-子线程"));
    }
}
