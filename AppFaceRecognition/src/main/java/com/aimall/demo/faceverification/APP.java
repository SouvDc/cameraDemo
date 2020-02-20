package com.aimall.demo.faceverification;

import android.support.multidex.MultiDexApplication;

import com.aimall.demo.faceverification.module.register.AppHelper;
import com.aimall.easylib.EasyLibUtils;
import com.cnbot.facelib.data.PhotoBeanList;
import com.cnbot.facelib.db.DatabaseHelper;
import com.cnbot.facelib.utils.FileUtils;

/**
 * Created by Administrator on 2017/1/22.
 */

public class APP extends MultiDexApplication {
    public static float time=0;
    public static String name=null;
    private static APP sInstance;

    public static APP getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AppHelper.init(this);

        EasyLibUtils.init(this);
        FileUtils.init(this);

        sInstance = this;
        new Thread(){
            @Override
            public void run() {
                DatabaseHelper.init(APP.this);
                PhotoBeanList.getInstance().clearList();
                PhotoBeanList.getInstance().addList(DatabaseHelper.getInstance().queryDatas());
            }
        }.start();
    }

}
