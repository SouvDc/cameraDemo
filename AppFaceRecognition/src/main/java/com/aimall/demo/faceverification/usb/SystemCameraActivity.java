package com.aimall.demo.faceverification.usb;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.aimall.demo.faceverification.R;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class SystemCameraActivity
		extends AppCompatActivity {

	private static final String TAG = "SystemCameraActivity";
	private Camera mCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_preview);

		initView();

	}

	private void initView() {

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_view);

		SurfaceHolder holder = surfaceView.getHolder();
		holder.addCallback(mSurfaceHolderCallback);

	}

	private final SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d(TAG, "surfaceCreated");
			initCamera();

		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

			Log.d(TAG, "surfaceChanged");
			try {
				startPreview(holder);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(TAG, "surfaceDestroyed");
			releaseCamera();
		}
	};

	private void initCamera() {
		if (mCamera == null) {
			try {
				mCamera = Camera.open();
				mCamera.setDisplayOrientation(getDisplayOrientation());
			} catch (RuntimeException e) {
				Log.e(TAG, "the camera is in use by another process or device policy manager has disabled the camera");
			}
		}
	}

	/**
	 * 预览数据的回调
	 */
	private final Camera.PreviewCallback mJpegPreviewCallback = new Camera.PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (data != null && camera != null) {

			}

		}
	};

	/**
	 * 开始预览
	 * @param holder
	 * @throws IOException
	 */
	private void startPreview(SurfaceHolder holder) throws IOException {
		if (mCamera == null) {
			return;
		}
		mCamera.setPreviewDisplay(holder);
		// TODO: 2018/3/21 dc 会报错 日志为： getParameters failed (empty parameters)
		// ww 初步断定是摄像头占用问题
		Camera.Parameters params = null;
		try {
			params = mCamera.getParameters();
		} catch (RuntimeException ex) {
			ex.printStackTrace();
		}
		if (params == null) {
			return;
		}
		//预览的图片格式NV21（YUV420sp）
		//		int format = setPreviewFormat(params);
		//支持的成像帧率
		int[] range = setPreviewFpsRange(params);
		//设置预览的尺寸
		int[] size = setPreviewSize(params);
		//设置聚焦模式
		//        String mode = setFocusMode(params);

		//设置参数
		setCameraParameters(params);
		//打印配置参数信息
		//        Log.w(TAG,
		//              "Camera properties: " + "size=" + size[0] + "x" + size[1] + "; frameRates=" + range[0] + "-" + range[1] + ",focusMode=" + mode);
		//用于一个Camera复制多份数据在其他界面预览
		mCamera.setPreviewCallback(mJpegPreviewCallback);
		try {
			mCamera.startPreview();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	private int[] setPreviewFpsRange(Camera.Parameters params) {
		List<int[]> rangeList = params.getSupportedPreviewFpsRange();
		int[] range = getPreviewFpsRange(rangeList);
		params.setPreviewFpsRange(range[0], range[1]);
		return range;
	}

	/**
	 * 根据支持的成像帧率，默认指定第一个数组为当前的范围
	 * 子类可重写根据需要重写指定
	 * @param frameRates
	 * @return
	 */
	protected int[] getPreviewFpsRange(List<int[]> frameRates) {
		int[] fps = new int[2];
		Iterator var6 = frameRates.iterator();

		while (var6.hasNext()) {
			int[] intArr = (int[]) var6.next();
			if (fps[0] == 0) {
				fps[0] = intArr[0];
				fps[1] = intArr[1];
			} else if (intArr[1] <= 30000 && intArr[0] >= fps[0] && intArr[1] >= fps[1]) {
				fps[0] = intArr[0];
				fps[1] = intArr[1];
			}
		}
		return fps;
	}

	private int[] setPreviewSize(Camera.Parameters params) {
		List<Camera.Size> preSizes = params.getSupportedPreviewSizes();
		//		int[] size = { 640, 480 };
		int[] size = { 1280, 720 };
		params.setPreviewSize(size[0], size[1]);

		return size;
	}

	private void setCameraParameters(Camera.Parameters params) {
		if (mCamera != null) {
			mCamera.setParameters(params);
		}
	}

	private int getDisplayOrientation() {

		return 0;
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.setPreviewCallbackWithBuffer(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}

	}
}
