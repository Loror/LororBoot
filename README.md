# 基于注解的Android快速开发框架

[![License](https://img.shields.io/badge/License%20-Apache%202-337ab7.svg)](https://www.apache.org/licenses/LICENSE-2.0)

## Studio中引入项目

```
dependencies {
    compile 'com.github.Loror:LororBoot:v1.0.34release'
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
compile('com.github.Loror:LororBoot:v1.0.34release') {
        exclude group: 'com.android.support'
        exclude module: 'appcompat-v7'
        exclude module: 'support-v4'
    }
```

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

* 示例代码
```
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
    @Bind(id = R.id.banner, imagePlace = R.mipmap.ic_launcher)
    List<Banner> listBanners = new ArrayList<>();
```

CheckBox（双向绑定）仅支持绑定boolean(Boolean)类型；TextView绑定的参数显示时将使用String.valueOf(object)获取内容显示；EditText的双向绑定仅在绑定String/CharSequence时生效；
ImageView支持绑定String类型（自动适配sd卡/网络图片地址进行加载），int类型（此时只能使用drawable，否则会出错）；AbsListView（ListView/GridView等）/RecyclerView仅支持绑定List(或ArrayList)<? extends BindAbleItem>，
否则抛出异常；BindAbleBannerView（内部banner控件）支持绑定List(或ArrayList)<? extends BindAbleItem>/List<String>类型，使用BindAbleItem可指定banner控件样式，否则将仅显示图片；
ProgressBar仅支持绑定int(Integer)，long(Long)类型。
注：CheckBox，EditText自动双向绑定，控件内容改变时参数将自动修改。



* 注解@GET @POST @PUT @DELETE
    * 网络访问注解封装，类似retrofit，修饰于接口上的方法
    * 支持返回类型原生responce（Responce），字符串（String），对象（将使用Json解释器生成对象）
    * 返回Observable对象时为异步请求，直接返回所需对象将同步请求
    * 内部未内置json解析框架，请在Application中指定Json解释器
    
* 注解@BaseUrl
    * 网络访问注解封装，修饰于接口上
    
* 注解@Header @Param @ParamObject @ParamJson
    * 网络访问注解封装，修饰于方法中参数，用于标示请求时传递的参数
    * 分别为添加到header，添加一个参数，抽取对象中所有属性到参数，以Json方式上传（指定传参为json后，其他参数将被拼接到url中）
    
</br>
内部已引入库LororUtil(https://github.com/Loror/LororUtil)  
</br>
推荐图片压缩框架Luban(https://github.com/Curzibn/Luban)

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