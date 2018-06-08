package com.loror.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.loror.lororboot.annotation.Bind;
import com.loror.lororboot.annotation.Click;
import com.loror.lororboot.annotation.ItemClick;
import com.loror.lororboot.startable.LororActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends LororActivity {

    /**
     * 修改变量值会自动重新显示内容
     */
    @Bind(id = R.id.textView)
    String text = "绑定TextView显示";
    @Bind(id = R.id.editText)
    String doubleBindText = "绑定EditText内容";
    @Bind(id = R.id.imageView, imagePlace = R.mipmap.ic_launcher)
    String image = "http://img.taopic.com/uploads/allimg/120727/201995-120HG1030762.jpg";
    @Bind(id = R.id.listView)
    List<ListItem> listItems = new ArrayList<>();

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
    }

    @Click(id = R.id.button)
    public void buttonClick(View view) {
        Toast.makeText(this, doubleBindText, Toast.LENGTH_SHORT).show();
    }

    @ItemClick(id = R.id.listView)
    public void listViewClick(View view, int position) {
        Toast.makeText(this, "第" + position + "行点击", Toast.LENGTH_SHORT).show();
    }

}
