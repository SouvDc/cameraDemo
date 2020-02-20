package com.aimall.demo.faceverification.module.camera;

import android.hardware.Camera;
import android.util.Log;

import com.cnbot.facelib.camera.BaseCameraService;

/*
 *  @项目名：  demo 
 *  @包名：    com.cnbot.test
 *  @文件名:   DefaultCameraService
 *  @创建者:   Administrator
 *  @创建时间:  2018/8/20 9:27
 *  @描述：    TODO
 */
public class DefaultCameraService extends BaseCameraService {

	private static final String TAG = "DefaultCameraService";

	@Override
	public int getCameraId() {
		return Camera.CameraInfo.CAMERA_FACING_BACK;
	}

	@Override
	protected boolean isHandlerDevice() {
		return false;
	}

	@Override
	protected void onRecognizeDynamic(String type, String userId, String name) {
		Log.e(TAG, "type:" + type + ",userId:" + userId + ",name:" + name);
	}
}
