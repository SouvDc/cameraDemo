package com.cnbot.facelib.camera;

import android.os.Binder;

/*
 *  @项目名：  demo 
 *  @包名：    com.cnbot.facelib.camera
 *  @文件名:   CameraBinderImpl
 *  @创建者:   Administrator
 *  @创建时间:  2018/8/28 11:24
 *  @描述：    BaseCameraService的Binder对象实现类，用于给外部提供可操作的后门
 */
public class CameraBinderImpl extends Binder implements ICameraBinder {

	private final BaseCameraService mService;

	public CameraBinderImpl(BaseCameraService baseCameraService) {
		mService = baseCameraService;
	}

	@Override
	public int getDisplayOrientation() {
		return mService.getDisplayOrientation();
	}

	@Override
	public void startRecognize() {
		mService.startRecognize();
	}

	@Override
	public void stopRecognize() {
		mService.stopRecognize();
	}

	@Override
	public void enableFaceRecognize() {
		mService.enableFaceRecognize();
	}

	@Override
	public void disableFaceRecognize() {
		mService.disableFaceRecognize();
	}

	@Override
	public boolean isRecognizing() {
		return mService.isRecognizing();
	}

	@Override
	public boolean isPreviewing() {
		return mService.isPreviewing();
	}

	@Override
	public void addCameraPreviewListener(ICameraPreviewListener l) {
		mService.addCameraPreviewListener(l);
	}

	@Override
	public void addFaceRecognizeListener(IFaceRecognizeListener l) {
		mService.addFaceRecognizeListener(l);
	}

	@Override
	public void removeListener() {
		mService.removeListener();
	}

	@Override
	public boolean removePreviewListener(ICameraPreviewListener l) {
		return mService.removePreviewListener(l);
	}

	@Override
	public boolean removeRecognizeListener(IFaceRecognizeListener l) {
		return mService.removeRecognizeListener(l);
	}
}
