package com.cnbot.facelib.camera;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.aimall.easylib.cameraengine.DeviceRotationDetector;
import com.aimall.easylib.cameraengine.camera.CameraUtils;
import com.aimall.easylib.utils.ScreenUtils;
import com.aimall.multifacetrackerlib.bean.FaceInfo;
import com.cnbot.facelib.FaceRecognitionManager;
import com.cnbot.facelib.constant.IConfig;
import com.cnbot.facelib.data.MatchResultBean;
import com.cnbot.facelib.data.PhotoBeanList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 *  @项目名：  demo
 *  @包名：    com.cnbot.facelib.camera
 *  @文件名:   faceLib
 *  @创建者:   Administrator
 *  @创建时间:  2018/8/16 17:25
 *  @描述：    人脸后台识别服务
 */
public abstract class BaseCameraService extends Service implements FaceRecognitionManager.AsyncExtractCallback {

	private static final String TAG = "BaseCameraService";

	private SurfaceView mDummySurfaceView;

	private Camera mCamera;

	/**
	 * 是否正在进行人脸识别,默认开启
	 */
	private volatile boolean isRecognizing = true;
	/**
	 * 是否开启了预览，默认关闭
	 */
	private volatile boolean isPreviewing = false;
	private byte[] cameraData;

	private List<FaceInfo> cacheFaceInfos;
	private List<FaceInfo> faceInfos = null;
	private DeviceRotationDetector mDeviceRotationDetector;

	@Override
	public void onCreate() {

		//人脸识别的接口回调
		extractFace(true);
		//设置后台人脸识别的区域
		makeAndAddSurfaceView();

	}

	/**
	 * 设置人脸识别结果的回调
	 */
	private void extractFace(boolean tag) {

		FaceRecognitionManager.getInstance().setAsyncExtractCallback(tag ? this : null);
	}

	@Override
	public void onExtractStart(byte[] data, int width, int height, List<FaceInfo> faceInfos) {

	}

	@Override
	public void onExtractComplete(byte[] data, int width, int height, List<FaceInfo> faceInfos) {
		// 特征值提取完毕，此方法运行在子线程
		PhotoBeanList.getInstance().compareDatabase(faceInfos);
		synchronized (cacheLock) {
			cacheFaceInfos = faceInfos;
		}
		Log.e(TAG, "onExtractComplete:" + cacheFaceInfos.size());
	}

	/**
	 * 设置后台人脸识别的区域
	 */
	private void makeAndAddSurfaceView() {
		//创建SurfaceView
		SurfaceView surfaceView = createSurfaceView();

		//将SurfaceView添加到窗口，指定宽高为1个像素，设置视图在所有Activity之上，不可获得焦点、不可触摸，全透明
		WindowManager.LayoutParams params = initWindowParams();
		//将SurfaceView添加到窗口
		addSurfaceView(surfaceView, params);

		mDummySurfaceView = surfaceView;
	}

	/**
	 * 将SurfaceView添加到窗口
	 * @param surfaceView
	 * @param params
	 */
	private void addSurfaceView(SurfaceView surfaceView, WindowManager.LayoutParams params) {
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		windowManager.addView(surfaceView, params);
	}

	/**
	 * 将SurfaceView添加到窗口，指定宽高为1个像素，
	 * 设置视图在所有Activity之上，
	 * 设置不可获得焦点、不可触摸，全透明
	 * @return
	 */
	@NonNull
	private WindowManager.LayoutParams initWindowParams() {
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSPARENT);
		params.gravity = Gravity.TOP | Gravity.RIGHT;
		params.alpha = PixelFormat.TRANSPARENT;
		//TODO:
		params.x = params.y = 1;
		return params;
	}

	/**
	 * 创建SurfaceView
	 * @return
	 */
	@NonNull
	private SurfaceView createSurfaceView() {
		SurfaceView surfaceView = new SurfaceView(this.getApplicationContext());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			surfaceView.setAlpha(0);
		}
		SurfaceHolder holder = surfaceView.getHolder();
		holder.addCallback(mSurfaceHolderCallback);
		return surfaceView;
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
		//用于一个Camera复制多份数据在其他界面预览
		mCamera.setPreviewCallbackWithBuffer(mJpegPreviewCallback);
		//		mCamera.setPreviewCallback(mJpegPreviewCallback);
		mCamera.addCallbackBuffer(new byte[640 * 480 * 3 / 2]);
		try {
			mCamera.startPreview();
		} catch (RuntimeException e) {
			e.printStackTrace();
			disablePreview();
		}
		enablePreview();
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
	 * 设置预览输出格式
	 * @return
	 */
	private int setPreviewFormat(Camera.Parameters params) {
		int format = getPreviewFormat();
		params.setPreviewFormat(format);
		return format;
	}

	/**
	 * 设置预览输出格式
	 * @return
	 */
	protected int getPreviewFormat() {
		return ImageFormat.NV21;
	}

	/**
	 * 设置摄像头参数
	 * @param params
	 */
	private void setCameraParameters(Camera.Parameters params) {
		if (mCamera != null)
			mCamera.setParameters(params);
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
	 * 预览数据的回调
	 */
	private final Camera.PreviewCallback mJpegPreviewCallback = new Camera.PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (data != null && camera != null) {
				camera.addCallbackBuffer(data);
				//人脸识别是否开启
				if (isRecognizing())
					onPreView(data, camera);

				synchronized (BaseCameraService.class) {

					if (cameraData == null)
						cameraData = new byte[data.length];
					System.arraycopy(data, 0, cameraData, 0, data.length);
					updatePreviewListeners(camera);

				}
			}

		}
	};

	private void updatePreviewListeners(Camera camera) {
		if (mPreviewListeners.size() > 0) {
			for (int i = 0; i < mPreviewListeners.size(); i++) {
				mPreviewListeners.get(i).onPreviewFrame(cameraData, camera);
			}
		}
	}

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
	 * 设置要打开的摄像头id
	 * 由于不确定当前的设备是支持前置还是后置所以必须重写指定
	 * @return
	 */
	public abstract int getCameraId();

	/**
	 * 设置摄像头的预览角度
	 * @return 角度
	 */
	protected int getDisplayOrientation() {
		return getImageRotate();
	}

	protected void removeListener() {
		mPreviewListeners.clear();
		mRecognizeListeners.clear();
	}

	protected boolean removePreviewListener(ICameraPreviewListener l) {
		if (mPreviewListeners.size() > 0)
			return mPreviewListeners.remove(l);
		return false;
	}

	protected boolean removeRecognizeListener(IFaceRecognizeListener l) {
		if (mRecognizeListeners.size() > 0)
			return mRecognizeListeners.remove(l);
		return false;
	}

	private final List<ICameraPreviewListener> mPreviewListeners = new ArrayList<>();
	private final List<IFaceRecognizeListener> mRecognizeListeners = new ArrayList<>();

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");

		return new CameraBinderImpl(this);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUnbind");
		return true;
	}

	@Override
	public void onRebind(Intent intent) {
		Log.d(TAG, "onRebind");
	}

	/**
	 * 释放摄像头资源同时停止人脸识别
	 */
	protected void stopRecognize() {
		if (!isPreviewing())
			return;
		extractFace(false);
		disableFaceRecognize();
		releaseCamera();
		disablePreview();
		Log.e(TAG, "stopRecognize");
	}

	/**
	 * 重新初始化摄像头并启动人脸识别
	 */
	protected void startRecognize() {
		if (isPreviewing())
			return;
		extractFace(true);
		enableFaceRecognize();
		initCamera();
		SurfaceHolder holder = mDummySurfaceView.getHolder();
		try {
			startPreview(holder);
		} catch (IOException e) {
			e.printStackTrace();
			disablePreview();
		}
		enablePreview();

		Log.e(TAG, "startRecognize");
	}

	/**
	 * 开启人脸识别
	 */
	protected void enableFaceRecognize() {
		isRecognizing = true;
	}

	/**
	 * 关闭人脸识别
	 */
	protected void disableFaceRecognize() {

		isRecognizing = false;

	}

	/**
	 * 是否开启了人脸识别
	 * @return
	 */
	protected boolean isRecognizing() {
		return isRecognizing;
	}

	/**
	 * 开启预览
	 */
	private void enablePreview() {
		isPreviewing = true;
	}

	/**
	 * 关闭预览
	 */
	private void disablePreview() {

		isPreviewing = false;

	}

	/**
	 * 是否开启了预览
	 * @return
	 */
	protected boolean isPreviewing() {
		return isPreviewing;
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

	@Override
	public void onDestroy() {
		stopRecognize();
		Log.d(TAG, "摄像头服务退出");
		super.onDestroy();

		resetDetector();
	}

	protected void addCameraPreviewListener(ICameraPreviewListener l) {
		if (!mPreviewListeners.contains(l))
			mPreviewListeners.add(l);
	}

	protected void addFaceRecognizeListener(IFaceRecognizeListener l) {
		if (!mRecognizeListeners.contains(l)) {
			mRecognizeListeners.add(l);
			//由于绑定服务有延迟，如果后台已经识别到了人脸，在userId相同时是不会通知接口的，所有必须重置
			resetLastUserId();
		}

	}

	private void resetLastUserId() {
		mLastUserId = null;
	}

	/**
	 * 是否过滤相同的人脸，如果要打分的话应该是不过滤
	 * @return
	 */
	protected boolean isFilterSameFace() {
		return true;
	}

	/**
	 * 图像旋转角度
	 * @return
	 */
	protected int getImageRotate() {
		if (!isHandlerDevice()) {
			return getCameraRotate();
		}
		DeviceRotationDetector detector = getDetector();
		return CameraUtils.getImageOrient(getCameraInfo(), detector.getRotationDegree(), getCameraId());
	}

	/**
	 * the sensor listeners size has exceeded the maximum limit 128
	 * 设备旋转角度识别器有注册数量限制
	 * @return
	 */
	private DeviceRotationDetector getDetector() {
		if (mDeviceRotationDetector == null)
			mDeviceRotationDetector = new DeviceRotationDetector(getApplicationContext());
		return mDeviceRotationDetector;
	}

	private void resetDetector() {
		mDeviceRotationDetector = null;
	}

	private Camera.CameraInfo getCameraInfo() {
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		Camera.getCameraInfo(getCameraId(), cameraInfo);
		return cameraInfo;
	}

	/**
	 * 是否是手持设备，设备是否支持旋转
	 * 默认用于机器人属于固定横屏，不支持旋转
	 * @return
	 */
	protected boolean isHandlerDevice() {
		return false;
	}

	/**
	 * 相机旋转角度
	 * @return
	 */
	protected int getCameraRotate() {
		int rotate = CameraUtils.getImageOrient(getCameraInfo(), getActivityRotate(), getCameraId());

		return normalizationRotate(rotate);
	}

	public int normalizationRotate(int rotate) {
		while (rotate < 0) {
			rotate += 360;
		}
		return (rotate % 360);
	}

	public int getActivityRotate() {
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		int rotation = windowManager.getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
		case 0:
			degrees = 0;
			break;
		case 1:
			degrees = 90;
			break;
		case 2:
			degrees = 180;
			break;
		case 3:
			degrees = 270;
		}

		return degrees;
	}

	private Object cacheLock = new Object();

	/**
	 * 预览数据进行人脸识别
	 *
	 * @param data
	 */
	protected void onPreView(byte[] data, Camera camera) {
		Camera.Size previewSize = null;
		try {
			previewSize = camera.getParameters().getPreviewSize();
		} catch (RuntimeException ex) {
			ex.printStackTrace();
		}
		if (previewSize == null)
			return;
		//返回的faceInfos是相对于原始数据的face信息    用于计算多帧图像中的人脸特征信息，是异步方法 第四个参数非常重要，决定了识别速度
		faceInfos = FaceRecognitionManager.getInstance().updateFrameAsync(data, previewSize.width, previewSize.height, getImageRotate());
		//将faceInfo rect 以及points旋转为屏幕方向,flipx相机预览横向翻转
		//		faceInfos = PointUtils.convertFaceInfo(getCameraRotate(), faceInfos, previewSize.width, previewSize.height, true);
		synchronized (cacheLock) {
			//元素1：userId 元素2：描述 元素3：识别结果类型
			String[] strArrays = new String[3];
			if (cacheFaceInfos == null || cacheFaceInfos.size() == 0 || faceInfos == null || faceInfos.size() == 0) {

				strArrays[1] = assignInvalidFaceDes();
				strArrays[2] = IFaceType.INVALID;
			} else {

				for (FaceInfo faceInfo : faceInfos) {
					for (FaceInfo cacheFaceInfo : cacheFaceInfos) {
						if (faceInfo.id == cacheFaceInfo.id) {

							recognizeResult(cacheFaceInfo, strArrays);
							// 保存异步线程缓存信息的匹配结果
							//							faceInfo.setTag(cacheFaceInfo.getTag());
						} else {
							Log.e("onPreView", "id is not equals");
						}
					}
				}
			}
			updateRecognizeListeners(strArrays);
		}
	}

	/**
	 * 实时获取人脸识别结果
	 * @param type 人脸识别的结果类
	 * @see IFaceType
	 * @param userId 用户id
	 * @param name  三种  识别成功是姓名    未识别    无效
	 */
	protected void onRecognizeDynamic(String type,String userId, String name){

	}

	private void updateRecognizeListeners(String[] arrs) {
		if (isFilterSameFace())
			if (filterSameFace(arrs))
				return;

		onRecognizeDynamic(arrs[2], arrs[0], arrs[1]);

		if (mRecognizeListeners.size() > 0) {
			for (int i = 0; i < mRecognizeListeners.size(); i++) {
				mRecognizeListeners.get(i).onRecognizeResult(arrs[2], arrs[0], arrs[1]);
			}
		}
	}

	/**
	 * 上一次识别的人脸
	 */
	private String mLastUserId = null;

	private boolean filterSameFace(String[] arrs) {
		String userId = arrs[0];

		if (userId != null && mLastUserId != null && mLastUserId.equals(userId))
			return true;

		mLastUserId = userId;
		return false;
	}

	private void recognizeResult(FaceInfo faceInfo, String[] strArrays) {

		Object tag = faceInfo.getTag();
		if (tag != null) {
			MatchResultBean matchResultBean = (MatchResultBean) tag;
			//								Log.e(TAG, "score" + + matchResultBean.score);
			if (matchResultBean.score > IConfig.STANDARD_SCORE) {
				strArrays[0] = matchResultBean.photoBean.getUserId();
				strArrays[1] = matchResultBean.photoBean.name;
				strArrays[2] = IFaceType.FACE;
				Log.e(TAG, "识别到的人脸 = " + strArrays[1] + "   userId = " + strArrays[0]);

			} else {
				strArrays[1] = assignUnKnowFaceDes();
				strArrays[2] = IFaceType.UNKNOW;
				Log.e(TAG, "content" + strArrays[1]);
			}
		} else {
			Log.e(TAG, "tag=null");
			strArrays[1] = assignInvalidFaceDes();
			strArrays[2] = IFaceType.INVALID;
		}

	}

	/**
	 * 指定无效人脸的描述，子类可自己重新
	 * @return
	 */
	protected String assignInvalidFaceDes() {
		return "无效人脸";
	}

	/**
	 * 指定未知人脸的描述，子类可自己重新
	 * @return
	 */
	protected String assignUnKnowFaceDes() {
		return "未知人脸";
	}

}
