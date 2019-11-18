package com.vhallyun.videouplod;

import android.app.Application;

import com.vhall.framework.VhallSDK;

/**
 * @author hkl
 * Date: 2019-11-18 17:30
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VhallSDK.getInstance().init(getApplicationContext(), "appid", "userid");
    }
}
