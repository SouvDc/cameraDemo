package com.aimall.demo.faceverification.module.camera;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.aimall.demo.faceverification.R;
import com.cnbot.facelib.camera.CameraBinderImpl;
import com.cnbot.facelib.camera.CameraPreviewActivity;
import com.cnbot.facelib.camera.ICameraBinder;
import com.cnbot.facelib.camera.ICameraPreviewListener;
import com.cnbot.facelib.camera.IFaceRecognizeListener;

public class CameraServiceActivity extends AppCompatActivity implements ICameraPreviewListener, IFaceRecognizeListener {

	private static final String TAG = "CameraServiceActivity";
	private TextView mTvRecognizeResult;
	private ICameraBinder mCameraBinder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_service);

		initView();

		startService(new Intent(this, DefaultCameraService.class));

	}

	private void initView() {

		mTvRecognizeResult = (TextView) findViewById(R.id.tv_recognize_result);
	}

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mCameraBinder = (CameraBinderImpl) service;
			mCameraBinder.addCameraPreviewListener(CameraServiceActivity.this);
			mCameraBinder.addFaceRecognizeListener(CameraServiceActivity.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};
	private boolean isServiceBind;

	public void bind(View view) {
		if (!isServiceBind) {
			bindService(new Intent(this, DefaultCameraService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
			isServiceBind = true;
		}
	}

	public void unbind(View view) {
		unbindService();

	}

	private void unbindService() {
		if (isServiceBind) {
			mCameraBinder.removeListener();
			unbindService(mServiceConnection);
			isServiceBind = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		//		unbindService();

		//		stopService(new Intent(this, DefaultCameraService.class));
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {

	}

	@Override
	public void onRecognizeResult(String type, String userId, String name) {
		String result = "type:" + type + ",userId:" + userId + ",name:" + name;
		Log.e(TAG, result);
		mTvRecognizeResult.setText(result);
	}

	public void startRecognize(View view) {
		if (mCameraBinder != null)
			mCameraBinder.enableFaceRecognize();

	}

	public void stopRecognize(View view) {
		if (mCameraBinder != null)
			mCameraBinder.disableFaceRecognize();
	}

	public void startPreview(View view) {
		if (mCameraBinder != null)
			mCameraBinder.startRecognize();
	}

	public void stopPreview(View view) {
		if (mCameraBinder != null)
			mCameraBinder.stopRecognize();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (mCameraBinder != null) {
			mCameraBinder.stopRecognize();
			mCameraBinder.startRecognize();
		}
	}

	public void switchPreview(View view) {

		if (mCameraBinder != null) {
			mCameraBinder.stopRecognize();
		}
		startActivity(new Intent(this, CameraPreviewActivity.class));

	}
}
