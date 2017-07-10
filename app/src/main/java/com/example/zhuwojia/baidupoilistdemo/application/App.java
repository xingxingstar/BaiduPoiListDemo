package com.example.zhuwojia.baidupoilistdemo.application;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * author：shixinxin on 2017/6/26
 * version：v1.0
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());
    }
}
