package com.aimall.demo.faceverification;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.aimall.demo.faceverification.module.camera.DefaultCameraService;
import com.cnbot.facelib.FaceRecognitionManager;
import com.cnbot.facelib.camera.ICameraBinder;
import com.cnbot.facelib.constant.IConfig;

public class TestActivity extends AppCompatActivity {

	private ICameraBinder mCameraBinder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);



		checkKey();


		startService(new Intent(this, DefaultCameraService.class));

	}




	public boolean checkKey() {
		return FaceRecognitionManager.getInstance().initFaceRecognition(getApplicationContext(), IConfig.KEY, true);
	}

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mCameraBinder = (ICameraBinder) service;
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
		//
		//		stopService(new Intent(this, DefaultCameraService.class));
	}
}
