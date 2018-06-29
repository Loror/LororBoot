# 基于注解的Android快速开发框架

[![License](https://img.shields.io/badge/License%20-Apache%202-337ab7.svg)](https://www.apache.org/licenses/LICENSE-2.0)

## Studio中引入项目

```
dependencies {
    compile 'com.github.Loror:LororBoot:v1.0.3rc'
}

allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

可参考demo使用，使用时需继承LororActivity，LororFragment，LororDialog，BindAbleItem使用，以上均实现于BindAble接口，也可自己实现BindAble接口复写绑定、事件处理。

## annotation包

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
    
* 注解@Click
    * 绑定控件点击事件
    * id() 控件id
    * clickSpace() 点击允许间隔
    
* 注解@ItemClick
    * 绑定AdapterView，RecyclerView，BandAbleBannerView控件item点击事件
    * id() 控件id
    * clickSpace() 点击允许间隔
    
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