package com.aimall.demo.faceverification.usb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.aimall.demo.faceverification.R;

import jerome.com.usbcamera.ICameraErrorListener;
import jerome.com.usbcamera.UsbCameraView;

public class UsbCameraActivity extends AppCompatActivity implements ICameraErrorListener {

	private UsbCameraView mUsbCamera;
	//	private CameraView mUsbCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

		mUsbCamera = (UsbCameraView) findViewById(R.id.usb_camera_view);
		//		mUsbCamera = (CameraView) findViewById(R.id.usb_camera_view);

		mUsbCamera.startPreview(this);

	}


	@Override
	public void onUsbCameraError(int code, String msg) {
		Toast.makeText(this, "errorCode:" + code, Toast.LENGTH_SHORT).show();
	}
}
