package com.lxf.facebookdemo;

import android.app.Application;

import com.facebook.appevents.AppEventsLogger;

/**
 * @author: lixiaofei
 * @date: 2019/5/6
 * @version: 1.0.0
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppEventsLogger.activateApp(this);
    }
}
