package com.aimall.demo.faceverification.usb;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.aimall.demo.faceverification.R;

import jerome.com.usbcamera.ICameraErrorListener;
import jerome.com.usbcamera.UsbCameraView;

public class MultiUsbCameraActivity extends AppCompatActivity implements ICameraErrorListener {

	private UsbCameraView mUsbCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_multi_usb);

		mUsbCamera = (UsbCameraView) findViewById(R.id.usb_camera_view);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				mUsbCamera.startPreview(MultiUsbCameraActivity.this);
			}
		}, 2000);
	}

	@Override
	public void onUsbCameraError(int code, String msg) {

	}
}
