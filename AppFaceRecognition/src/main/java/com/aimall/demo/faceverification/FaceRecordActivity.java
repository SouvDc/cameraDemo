package com.aimall.demo.faceverification;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;

import com.aimall.demo.faceverification.module.camera.DefaultCameraService;
import com.aimall.easylib.utils.ScreenUtils;
import com.cnbot.facelib.camera.ICameraBinder;
import com.cnbot.facelib.camera.ICameraPreviewListener;
import com.cnbot.facelib.camera.IFaceRecognizeListener;
import com.cnbot.facelib.utils.BitmapUtils;

public class FaceRecordActivity extends AppCompatActivity implements IFaceRecognizeListener, ICameraPreviewListener {

	private SurfaceView mSurfaceView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face_record);

		mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);

		bindCameraService();

	}
	private int displayOrientation;
	private ICameraBinder mCameraBinder;
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mCameraBinder = (ICameraBinder) service;
			mCameraBinder.addFaceRecognizeListener(FaceRecordActivity.this);
			mCameraBinder.addCameraPreviewListener(FaceRecordActivity.this);
			displayOrientation = mCameraBinder.getDisplayOrientation();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};

	@Override
	public void onRecognizeResult(String type, String userId, String name) {

	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		drawBitmap(data);
	}

	/**
	 * 将后台服务预览的Camera数据绘制到SurfaceView上
	 * @param data
	 */
	private Bitmap drawBitmap(byte[] data) {

		if (data == null)
			return null;
		int[] dimens = { 640, 480 };

		Bitmap bitmap = com.cnbot.facelib.utils.BitmapUtils.createBitmap(data, dimens,displayOrientation);
		Canvas canvas = mSurfaceView.getHolder().lockCanvas();
		int width = ScreenUtils.convertDip2Px(this, 640);
		int height = ScreenUtils.convertDip2Px(this, 480);
		BitmapUtils.drawMirrorBitmap(canvas,bitmap, width, height);
		try {
			mSurfaceView.getHolder().unlockCanvasAndPost(canvas);
		} catch (IllegalStateException e) {

		}
		return bitmap;
	}

	protected void bindCameraService() {
		bindService(new Intent(this, DefaultCameraService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
	}

	protected void unbindCameraService() {
		mCameraBinder.removePreviewListener(this);
		mCameraBinder.removeRecognizeListener(this);
		unbindService(mServiceConnection);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unbindCameraService();

	}
}
