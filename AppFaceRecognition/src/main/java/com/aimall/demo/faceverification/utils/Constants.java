package com.aimall.demo.faceverification.utils;

/**
 * Created by Administrator on 2017/1/16.
 */

public class Constants {
    public static final String KEY = "-----put your key here------"; //用来验证是否在有效期内
    // 使用异步接口updateFrame时最大人脸数，越小性能越好，建议使用5
    public static final int MAX_TRACKER_COUNT = 5;
    // 建议使用的特征值匹配阈值
    public static final int stardandScore = 75;

}
