package com.aimall.demo.faceverification.usb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.aimall.demo.faceverification.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import jerome.com.usbcamera.Usb;

public class UsbActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_usb);
	}

	public void usbCamera(View view) {
		if (exec())
			startActivity(new Intent(this, UsbCameraActivity.class));

	}

	private boolean exec() {
		File file = new File("/dev/video0");
		String exception = "初始化成功";
		boolean tag = true;
		try {
			new Usb(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			exception = e.toString();
			tag = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			exception = e.toString();
			tag = false;
		} catch (IOException e) {
			e.printStackTrace();
			exception = e.toString();
			tag = false;
		} catch (SecurityException e) {
			e.printStackTrace();
			exception = e.toString();
			tag = false;
		}
		Toast.makeText(this, exception, Toast.LENGTH_SHORT).show();
		return tag;
	}
	private boolean exec2() {
		File file = new File("/dev/video0");
		String exception = "初始化成功";
		boolean tag = true;
		try {
			new Usb(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			exception = e.toString();
			tag = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			exception = e.toString();
			tag = false;
		} catch (IOException e) {
			e.printStackTrace();
			exception = e.toString();
			tag = false;
		} catch (SecurityException e) {
			e.printStackTrace();
			exception = e.toString();
			tag = false;
		}
		Toast.makeText(this, exception, Toast.LENGTH_SHORT).show();
		return tag;
	}

	public void systemCamera(View view) {

		startActivity(new Intent(this, SystemCameraActivity.class));

	}

	public void multiCamera(View view) {

		if (exec())
			startActivity(new Intent(this, MultiCameraPreviewActivity.class));


	}

	public void multiUsb(View view) {
		if (exec2())
			startActivity(new Intent(this, MultiUsbCameraActivity.class));

	}
}
