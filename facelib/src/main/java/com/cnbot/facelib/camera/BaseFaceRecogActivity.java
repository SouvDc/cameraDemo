package com.cnbot.facelib.camera;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.aimall.easylib.utils.ScreenUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/*
 *  @项目名：  EcologyRobot2
 *  @包名：    com.cnbot.ecologyrobot2.base
 *  @文件名:   BaseApp
 *  @创建者:   Administrator
 *  @创建时间:  2018/8/9 11:25
 *  @描述：    人脸录入和识别的Activity基类
 */
public abstract class BaseFaceRecogActivity extends AppCompatActivity {

	private static final String TAG = "BaseFaceRecogActivity";

	private Camera mCamera;
	/**
	 * 预览数据，用于生成Bitmap
	 */
	private volatile byte[] mPreviewData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(getLayoutId());

		SurfaceView surfaceView = initView();

		initSurfaceView(surfaceView);
	}

	/**
	 * 初始化子类布局，返回父类需要的SurfaceView
	 * @return
	 */
	protected abstract SurfaceView initView();

	/**
	 * 获取子类的布局文件
	 * @return
	 */
	protected abstract int getLayoutId();

	protected void initSurfaceView(SurfaceView surfaceView) {

		surfaceView.getHolder().addCallback(mSurfaceHolderCallback);

	}


	protected void onSurfaceCreated(SurfaceHolder holder){

	}

	protected void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height){

	}

	protected void onSurfaceDestroyed(SurfaceHolder holder){

	}

	private final SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d(TAG, "surfaceCreated");
			initCamera();
			onSurfaceCreated(holder);

		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

			Log.d(TAG, "surfaceChanged");
			try {
				startPreview(holder);
			} catch (IOException e) {
				e.printStackTrace();
			}
			onSurfaceChanged(holder,format,width,height);
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(TAG, "surfaceDestroyed");
			releaseCamera();
			onSurfaceDestroyed(holder);
		}
	};

	private void initCamera() {
		if (mCamera == null) {
			try {
				mCamera = Camera.open(getCameraId());
				mCamera.setDisplayOrientation(getDisplayOrientation());
			} catch (RuntimeException e) {
				Log.e(TAG, "the camera is in use by another process or device policy manager has disabled the camera");
			}
		}
	}

	/**
	 * 预览方向
	 * @return
	 */
	protected int getDisplayOrientation() {

		return 90;
	}

	/**
	 * 释放相机资源
	 */
	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.setPreviewCallbackWithBuffer(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}

	}

	/**
	 * 开始预览
	 * @param holder
	 * @throws IOException
	 */
	private void startPreview(SurfaceHolder holder) throws IOException {
		if (mCamera == null)
			return;
		mCamera.setPreviewDisplay(holder);
		// TODO: 2018/3/21 dc 会报错 日志为： getParameters failed (empty parameters)
		// ww 初步断定是摄像头占用问题
		Camera.Parameters params = null;
		try {
			params = mCamera.getParameters();
		} catch (RuntimeException ex) {
			ex.printStackTrace();
		}
		if (params == null)
			return;
		//预览的图片格式NV21（YUV420sp）
		//		int format = setPreviewFormat(params);
		//支持的成像帧率
		int[] range = setPreviewFpsRange(params);
		//设置预览的尺寸
		int[] size = setPreviewSize(params);
		//设置聚焦模式
		String mode = setFocusMode(params);

		//设置参数
		setCameraParameters(params);
		//打印配置参数信息
		Log.w(TAG, "Camera properties: " + "size=" + size[0] + "x" + size[1] + "; frameRates=" + range[0] + "-" + range[1] + ",focusMode=" + mode);
		mCamera.setPreviewCallback(mJpegPreviewCallback);
		try {
			mCamera.startPreview();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置摄像头参数
	 * @param params
	 */
	private void setCameraParameters(Camera.Parameters params) {
		if (mCamera != null)
			mCamera.setParameters(params);
	}

	private String setFocusMode(Camera.Parameters params) {
		List<String> supportedFlashModes = params.getSupportedFocusModes();
		String mode = getFocusMode(supportedFlashModes);
		if (!TextUtils.isEmpty(mode))
			params.setFocusMode(mode);
		return mode;
	}

	/**
	 * 子类可指定聚焦模式
	 * @param supportedFlashModes
	 * @return
	 */
	protected String getFocusMode(List<String> supportedFlashModes) {
		String mode = "";
		if (supportedFlashModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
			mode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
		} else if (supportedFlashModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			mode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
		} else if (supportedFlashModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			mode = Camera.Parameters.FOCUS_MODE_AUTO;
		}
		return mode;
	}

	/**
	 * 根据摄像头支持的预览尺寸列表，设置默认的预览尺寸640*480
	 * 子类可重写
	 * @param preSizes
	 */
	protected int[] getPreviewSize(List<Camera.Size> preSizes) {

		return new int[] { 640, 480 };
	}

	/**
	 * 根据支持的成像帧率，指定当前的范围
	 * @param params
	 * @return
	 */
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

	/**
	 * 根据摄像头支持的预览尺寸列表，设置默认的预览尺寸
	 * @param params
	 */
	private int[] setPreviewSize(Camera.Parameters params) {
		List<Camera.Size> preSizes = params.getSupportedPreviewSizes();
		Camera.Size tempSize = getPreviewSize(preSizes, ScreenUtils.getScreenWidth(getApplicationContext()));
		if (tempSize != null)
			Log.e(TAG, "calculate width:" + tempSize.width + ",height" + tempSize.height);
		for (int i = 0; i < preSizes.size(); i++) {
			Log.e(TAG, "width:" + preSizes.get(i).width + ",height" + preSizes.get(i).height);

		}
		int[] size = getPreviewSize(preSizes);
		params.setPreviewSize(size[0], size[1]);

		return size;
	}

	protected Camera.Size getPreviewSize(List<Camera.Size> preSizes, int width) {
		sortPreSize(preSizes);

		for (Camera.Size s : preSizes) {
			if ((s.width >= width) && equalRate(s, 1.33f)) {
				return s;
			}
		}

		return null;
	}

	public boolean equalRate(Camera.Size s, float rate) {
		float r = (float) (s.width) / (float) (s.height);
		return Math.abs(r - rate) <= 0.2;

	}

	/**
	 * 排序
	 * @param pres
	 */
	protected void sortPreSize(List<Camera.Size> pres) {

		int min = 0;
		Camera.Size temp;
		for (int i = 0; i < pres.size() - 1; i++) {
			min = i;
			for (int j = i + 1; j < pres.size(); j++) {
				if (pres.get(j).width < pres.get(min).width) {
					min = j;
				}
			}
			if (min != i) {
				temp = pres.get(min);
				pres.set(min, pres.get(i));
				pres.set(i, temp);
			}
		}

	}

	/**
	 * 设置要打开的摄像头id
	 * 由于不确定当前的设备是支持前置还是后置所以必须重写指定
	 * @return
	 */
	public int getCameraId() {
		return Camera.CameraInfo.CAMERA_FACING_BACK;
	}

	/**
	 * 预览数据的回调
	 */
	private final Camera.PreviewCallback mJpegPreviewCallback = new Camera.PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (data != null && camera != null) {
				if (mPreviewData == null)
					mPreviewData = new byte[data.length];
				System.arraycopy(data, 0, mPreviewData, 0, data.length);

			}
			onPreview(data, camera);

		}
	};

	protected Camera getCamera(){
		return mCamera;
	}
	
	protected byte[] getPreviewData(){
		return mPreviewData;
	}
	
	/**
	 * Camera预览数据
	 * @param data
	 * @param camera
	 */
	protected void onPreview(byte[] data, Camera camera){
		
	}

}
