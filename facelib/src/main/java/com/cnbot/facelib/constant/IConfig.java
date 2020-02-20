package com.cnbot.facelib.constant;

/*
 *  @项目名：  demo 
 *  @包名：    com.cnbot.facelib.constant
 *  @文件名:   IConfig
 *  @创建者:   Administrator
 *  @创建时间:  2018/8/16 19:47
 *  @描述：    人脸相关的常量配置
 */
public interface IConfig {

	String KEY = "C988EEAB6DF85E0D"; //用来验证是否在有效期内
	// 使用异步接口updateFrame时最大人脸数，越小性能越好，建议使用5
	int MAX_TRACKER_COUNT = 5;
	// 建议使用的特征值匹配阈值
	int STANDARD_SCORE = 75;

}
