package com.luxcine.luxcine_ota;


import android.app.Application;



public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    private static MyApplication context;


    @Override
    public void onCreate() {
        super.onCreate();

        context = this;


    }

    public static MyApplication getContext() {
        return context;

    }

}
