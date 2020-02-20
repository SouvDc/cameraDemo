package com.cnbot.facelib.camera;

/*
 *  @项目名：  demo 
 *  @包名：    com.aimall.demo.faceverification.module.camera
 *  @文件名:   ICameraBinder
 *  @创建者:   Administrator
 *  @创建时间:  2018/8/24 10:47
 *  @描述：    后台摄像头服务的Binder对象接口，定义了操作Service的方法
 */
public interface ICameraBinder {

	/**
	 * 获取当前摄像头的预览方向
	 * @return
	 */
	int getDisplayOrientation();


	/**
	 * 重新初始化摄像头并启动人脸识别
	 */
	void startRecognize();

	/**
	 * 释放摄像头资源同时停止人脸识别
	 */
	void stopRecognize();

	/**
	 * 开启人脸识别
	 */
	void enableFaceRecognize();

	/**
	 * 关闭人脸识别
	 */
	void disableFaceRecognize();

	/**
	 * 是否开启了人脸识别
	 * @return
	 */
	boolean isRecognizing();

	/**
	 * 是否开启了预览
	 * @return
	 */
	boolean isPreviewing();

    /**
     * 摄像头预览
     * @param l
     */
    void addCameraPreviewListener(ICameraPreviewListener l);

    /**
     * 人脸识别结果
     * @param l
     */
    void addFaceRecognizeListener(IFaceRecognizeListener l);

	/**
	 * 移除所有的预览和识别监听
	 */
    void removeListener();

	/**
	 * 移除指定的预览监听
	 */
	boolean removePreviewListener(ICameraPreviewListener l);
	/**
	 * 移除指定的识别监听
	 */
	boolean removeRecognizeListener(IFaceRecognizeListener l);


}
