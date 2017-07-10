package com.zyyoona7.sample;

import android.app.Application;

/**
 * Created by zyyoona7 on 2017/7/10.
 */

public class MyApplication extends Application {

    public long currentTime=System.currentTimeMillis();
    public String answer="";
    public boolean isUnlock=false;
    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance=this;
    }

    public static MyApplication getInstance(){
        return mInstance;
    }
}
