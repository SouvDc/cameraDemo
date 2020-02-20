package com.cnbot.facelib.camera;

import android.hardware.Camera;

/*
 *  @项目名：  demo 
 *  @包名：    com.cnbot.facelib.camera
 *  @文件名:   ICameraPreviewListener
 *  @创建者:   Administrator
 *  @创建时间:  2018/8/24 11:26
 *  @描述：    摄像头预览回调接口
 */
public interface ICameraPreviewListener {

    /**
     * 将视频预览的数据回调给其他页面
     *
     * @param data
     * @param camera
     */
    void onPreviewFrame(byte[] data, Camera camera);


}
