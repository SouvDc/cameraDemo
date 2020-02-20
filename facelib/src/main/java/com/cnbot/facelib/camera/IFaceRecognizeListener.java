package com.cnbot.facelib.camera;

/*
 *  @项目名：  demo 
 *  @包名：    com.cnbot.facelib.camera
 *  @文件名:   ICameraPreviewListener
 *  @创建者:   Administrator
 *  @创建时间:  2018/8/24 11:26
 *  @描述：    人脸识别结果回调
 */
public interface IFaceRecognizeListener {


    /**
     * 人脸识别结果回调
     * @param type 人脸识别的结果类
     * @see IFaceType
     * @param userId 用户id
     * @param name  三种  识别成功是姓名    未识别    无效
     */
    void onRecognizeResult(String type,String userId, String name);


}
