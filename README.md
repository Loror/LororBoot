# 基于注解的Android快速开发框架

[![License](https://img.shields.io/badge/License%20-Apache%202-337ab7.svg)](https://www.apache.org/licenses/LICENSE-2.0)

## Studio中引入项目

```
dependencies {
    compile 'com.github.Loror:LororBoot:v1.1.58release'
}

allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

如有v4包等冲突建议引入时排除v4包，框架对recyclerView进行了支持，如排除了support包请手动为你的项目引入recyclerView，
否则请删除exclude group: 'com.android.support'
```
compile('com.github.Loror:LororBoot:v1.1.58release') {
        exclude group: 'com.android.support'
        exclude module: 'appcompat-v7'
        exclude module: 'support-v4'
    }
```

## 如已更新到androidx，请更换到mvvm，可参考mvvmbase


LororBootX(https://github.com/Loror/LororBootX)


可参考demo使用，使用时需继承LororActivity，LororFragment，LororDialog，BindAbleItem使用，以上均实现于BindAble接口，也可自己实现BindAble接口复写绑定、事件处理。

## 主框架bind

* 注解@Bind
    * 绑定控件与变量关系，支持TextView，ImageView，ListView，GridView，RecyclerView，BandAbleBannerView等，自动为EditText双向绑定
    * id() 控件id
    * format() 格式化显示，%s填充变量值
    * event() 注册事件名,如被注册变量值变更时会触发event方法
    * empty() 空值显示
    * imagePlace() ImageView加载占位图
    * imageWidth() 指定ImageView缓存图宽度
    * visibility() 控件显示状态
    * onlyEvent() 是否不显示变量到控件只触发事件

* Bind示例代码
```
    @Bind(id = R.id.checkBox)
    boolean checked = true;
    @Bind(id = R.id.textView)
    String text = "绑定TextView显示";
    @Bind(id = R.id.editText)
    String doubleBindText = "绑定EditText内容";
    @Bind(id = R.id.imageView, imagePlace = R.mipmap.ic_launcher, bitmapConverter = RoundBitmapConverter.class)
    String image;
    @Bind(id = R.id.progressBar)
    int progress;
    @Bind(id = R.id.listView)
    List<ListItem> listItems = new ArrayList<>();
    @Bind(id = R.id.banner, imagePlace = R.mipmap.ic_launcher)
    List<Banner> listBanners = new ArrayList<>();
```

CheckBox（双向绑定）仅支持绑定boolean(Boolean)类型；TextView绑定的参数显示时将使用String.valueOf(object)获取内容显示；EditText的双向绑定仅在绑定String/CharSequence时生效；
ImageView支持绑定String类型（自动适配sd卡/网络图片地址进行加载），int类型（此时只能使用drawable，否则会出错）；AbsListView（ListView/GridView等）/RecyclerView仅支持绑定List(或ArrayList)<? extends BindAbleItem>，
否则抛出异常；BindAbleBannerView（内部banner控件）支持绑定List(或ArrayList)<? extends BindAbleItem>/List<String>类型，使用BindAbleItem可指定banner控件样式，否则将仅显示图片；
ProgressBar仅支持绑定int(Integer)，long(Long)类型。
注：CheckBox，EditText自动双向绑定，控件内容改变时参数将自动修改。

* BindAbleItem示例代码
```
public class ListItem extends BindAbleItem {

    @Bind(id = R.id.text)
    public String text;

    @Override
    public int viewType() {
        return obtainPosition() % 2;
    }

    @Override
    public int viewTypeCount() {
        return 2;
    }

    @Override
    public int getLayout(int viewType) {
        return viewType == 0 ? R.layout.item_list_view : R.layout.item_list_view_2;
    }
}
```

继承BindAbleItem后可实现上述方法，指定layout等。内置方法obtainPosition()可获取当前item所处位置。obtainOutBindAble()可获取通过@Bind绑定该BindAbleItem的对象。
</br>
可通过@BindAbleItemConnection向BindAbleItem传递数据，@BindAbleItemConnection为发送方，@Connection为接收方
</br>
注：默认bind为自动刷新模式，可定义BaseActivity继承于LororActivity，在onCreate中使用方法setBindAbleAutoRefresh(false)关闭自动刷新，
若关闭自动刷新，请在修改bind的参数后手动调用changeState(null)刷新控件显示

## 消息传递框架

* 示例代码

* 接口DataBusReceiver(同进程消息)，RemoteDataBusReceiver(跨进程消息)
    * 继承于LororActivity，LororFragment，LororDialog的类实现接口自动管理生命周期，通过方法sendDataToBus(String name, Intent data)发送消息
    
```
public class MainActivity extends LororActivity implements RemoteDataBusReceiver {
    @Override
    @DataRun(thread = RunThread.MAINTHREAD)//指定执行线程
    public void receiveData(String name, Intent data) {
        if ("toast".equals(name)) {
            Toast.makeText(this, data.getStringExtra("msg"), Toast.LENGTH_SHORT).show();
            text = "收到消息了";
        }
        Log.e("DATA_BUS", (Looper.getMainLooper() == Looper.myLooper() ? "-主线程" : "-子线程"));
    }
}
```

## 动态权限申请

* 注解@RequestPermission
    * 放在继承于LororActivity的Activity上，自动进行动态权限申请，可覆写onPermissionsResult方法监听申请结果

其它方法详见LororUtil库


</br>
内部已引入库LororUtil(https://github.com/Loror/LororUtil)  
</br>
推荐图片压缩框架Luban(https://github.com/Curzibn/Luban)
</br>
推荐下拉刷新框架SmartRefreshLayout(https://github.com/scwang90/SmartRefreshLayout)

License
-------

    Copyright 2018 Loror

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.