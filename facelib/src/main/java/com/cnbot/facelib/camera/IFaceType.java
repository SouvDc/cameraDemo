package com.cnbot.facelib.camera;

/*
 *  @项目名：  demo 
 *  @包名：    com.aimall.demo.faceverification.module.camera
 *  @文件名:   IFaceType
 *  @创建者:   Administrator
 *  @创建时间:  2018/8/24 14:46
 *  @描述：    人脸识别的结果类型
 */
public interface IFaceType {

    /**
     * 无效人脸
     */
    String INVALID = "-1";
    /**
     * 匹配分值<75，未知人脸，人脸被遮挡或者人脸未注册
     */
	String UNKNOW = "0";
    /**
     * 成功识别人脸
     */
	String FACE = "1";

}
