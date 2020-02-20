package com.cnbot.facelib;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.aimall.easylib.utils.ToastUtils;
import com.aimall.multifacetrackerlib.TrackerManager;
import com.aimall.multifacetrackerlib.bean.FaceInfo;
import com.aimall.multifacetrackerlib.utils.PointUtils;
import com.aimall.sdk.faceverification.FaceVerificationMgr;
import com.aimall.sdk.faceverification.L;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangchao on 18-1-30.
 */

public class FaceRecognitionManager {
	private static final String TAG = "FaceRecognitionManager";
	//单例实现
	private static FaceRecognitionManager sInstance = new FaceRecognitionManager();
	private FaceVerificationMgr faceVerificationMgr;
	private TrackerManager trackerManager;
	private AsyncExtractCallback AsyncExtractCallback;

	private boolean recongnitionIsRun;
	private HandlerThread faceThread;
	private Handler faceHandler;
	private Object cacheLock = new Object();
	private List<FaceInfo> cacheFaceInfos;
	private boolean initKeyResult;

	public static FaceRecognitionManager getInstance() {
		return sInstance;
	}

	/**
	 * 算两个人脸特征的相似度
	 *
	 * @param features1
	 * @param features2
	 * @return
	 */
	public static float caculateSimilarity(@NonNull float[] features1, @NonNull float[] features2) {

		return FaceVerificationMgr.caculateSimilarity(features1, features2);
	}

	public boolean initFaceRecognition(Context context, String key, boolean initTracker) {
		boolean ret = true;
		openFaceAttibuteMgrThread();
		if (faceVerificationMgr == null) {
			faceVerificationMgr = new FaceVerificationMgr(context);
		}
		if (trackerManager == null) {
			trackerManager = new TrackerManager(context).setMaxTrackers(5).checkPerfect(true);

		}
		boolean succeed = faceVerificationMgr.init(key);
		if (!succeed) {
			L.e(TAG, "授权失败,请确保Constants.java变量KEY填写正确！！！");
			ret = false;
		} else if (initTracker) {
			boolean initTrackerKeySucceed = trackerManager.init(key);
			if (!initTrackerKeySucceed) {
				L.e(TAG, "Tracker 授权失败,请确保Constants.java变量KEY填写正确！！！");
				ret = false;
			}
		}
		initKeyResult = ret;
		return ret;
	}

	public boolean initFaceRecognition(Context context, String key, String modelPath, boolean initTracker) {
		boolean ret = true;
		openFaceAttibuteMgrThread();
		if (!new File(modelPath).isDirectory()) {
			ToastUtils.showShort("请导入模型文件在" + modelPath + "目录");
			return false;
		}
		if (faceVerificationMgr == null) {
			faceVerificationMgr = new FaceVerificationMgr(context);
		}
		if (trackerManager == null) {
			trackerManager = new TrackerManager(context).setMaxTrackers(5).checkPerfect(true);

		}
		boolean succeed = faceVerificationMgr.init(key, modelPath);
		if (!succeed) {
			L.e(TAG, "授权失败,请确保Constants.java变量KEY填写正确！！！");
			ret = false;
		} else if (initTracker) {
			boolean initTrackerKeySucceed = trackerManager.init(key, modelPath);
			if (!initTrackerKeySucceed) {
				L.e(TAG, "Tracker 授权失败,请确保Constants.java变量KEY填写正确！！！");
				ret = false;
			}
		}
		initKeyResult = ret;
		return ret;
	}

	private void openFaceAttibuteMgrThread() {
		if (faceThread != null) {
			faceThread.quit();
		}
		faceThread = new HandlerThread("Recognition Thread");
		faceThread.start();
		faceHandler = new Handler(faceThread.getLooper());
	}

	/**
	 * 获取单张图片的特征值
	 *
	 * @param bitmap
	 * @return
	 */
	public List<FaceInfo> extractFeature(Bitmap bitmap) {
		if (!initKeyResult) {
			L.e("please init key");
			return new ArrayList<>();
		}
		//ww 8.24
		if (trackerManager == null)
			return new ArrayList<>();
		List<FaceInfo> faceInfos = trackerManager.trackerBitmap(bitmap);
		if (faceInfos.size() == 0) {
			return new ArrayList<>();
		}
		float[][] points = PointUtils.getPointFromFaceInfo(faceInfos);
		List<float[]> featuresList = faceVerificationMgr.extractFeature(bitmap, points);
		setFeatureResult(faceInfos, featuresList);
		return faceInfos;
	}

	private void setFeatureResult(List<FaceInfo> faceInfos, List<float[]> featuresList) {
		for (int i = 0; i < faceInfos.size(); ++i) {
			FaceInfo faceInfo = faceInfos.get(i);
			faceInfo.setFeatures(featuresList.get(i));
		}
	}

	/**
	 * 同步接口
	 *
	 * @param data        yuv-nv21数据
	 * @param width
	 * @param height
	 * @param imageRotate
	 * @return
	 */
	public List<FaceInfo> updateFrameSync(final byte[] data, final int width, int height, int imageRotate) {
		if (!initKeyResult) {
			L.e("please init key");
			return new ArrayList<>();
		}
		//ww 8.24
		if (trackerManager ==null)
			return new ArrayList<>();
		List<FaceInfo> faceInfos = trackerManager.updateFrame(data, width, height, imageRotate);
		return updateFrameSync(data, width, height, faceInfos);
	}

	/**
	 * 同步接口
	 *
	 * @param data   yuv-nv21数据
	 * @param width
	 * @param height
	 * @return
	 */
	public List<FaceInfo> updateFrameSync(final byte[] data, final int width, int height, List<FaceInfo> faceInfos) {
		if (!initKeyResult) {
			L.e("please init key");
			return new ArrayList<>();
		}
		if (faceInfos != null) {
			float[][] points = PointUtils.getPointFromFaceInfo(faceInfos);
			List<float[]> featuresList = faceVerificationMgr.updateFrameSync(data, width, height, points);
			setFeatureResult(faceInfos, featuresList);
			return faceInfos;
		}
		return new ArrayList<>();
	}

	public List<FaceInfo> updateFrameAsync(final byte[] data, final int width, final int height, List<FaceInfo> faceInfos) {
		if (!initKeyResult) {
			L.e("please init key");
			return new ArrayList<>();
		}
		if (faceInfos != null) {
			if (!recongnitionIsRun) {
				final List<FaceInfo> paramFaceInfos = new ArrayList<>();
				for (FaceInfo faceInfo : faceInfos) {
					paramFaceInfos.add(faceInfo.clone());
				}
				recongnitionIsRun = true;
				final AsyncExtractCallback fpListener = AsyncExtractCallback;
				faceHandler.post(new Runnable() {
					@Override
					public void run() {

						if (fpListener != null) {
							fpListener.onExtractStart(data, width, height, paramFaceInfos);
						}
						float[][] points = PointUtils.getPointFromFaceInfo(paramFaceInfos);
						List<float[]> featuresList = faceVerificationMgr.updateFrameSync(data, width, height, points);
						setFeatureResult(paramFaceInfos, featuresList);
						if (fpListener != null) {
							fpListener.onExtractComplete(data, width, height, paramFaceInfos);
						}
						synchronized (cacheLock) {
							cacheFaceInfos = paramFaceInfos;
						}
						recongnitionIsRun = false;

					}
				});
			}
			synchronized (cacheLock) {
				if (cacheFaceInfos != null) {
					for (FaceInfo faceInfo : faceInfos) {
						for (FaceInfo cacheFaceInfo : cacheFaceInfos) {
							if (faceInfo.id == cacheFaceInfo.id) {
								faceInfo.setFeatures(cacheFaceInfo.getFeatures());
							}
						}
					}
				}
			}
		}
		return faceInfos;
	}

	/**
	 * 异步接口，会缓存上次结果
	 *
	 * @param data
	 * @param width
	 * @param height
	 * @param imageRotate
	 * @return
	 */
	public List<FaceInfo> updateFrameAsync(byte[] data, int width, int height, int imageRotate) {
		if (!initKeyResult) {
			L.e("please init key");
			return new ArrayList<>();
		}
		//ww 8.24
		if (trackerManager ==null)
			return new ArrayList<>();

		List<FaceInfo> faceInfos = trackerManager.updateFrame(data, width, height, imageRotate);
		return updateFrameAsync(data, width, height, faceInfos);
	}

	/**
	 * 设置特征识别成功回调
	 *
	 * @param asyncExtractCallback
	 */
	public void setAsyncExtractCallback(AsyncExtractCallback asyncExtractCallback) {
		this.AsyncExtractCallback = asyncExtractCallback;
	}

	/**
	 * 析够函数
	 */
	public void release() {
		//ww
		if (faceThread != null)
			faceThread.quit();
		if (faceVerificationMgr != null) {
			faceVerificationMgr.onDestory();
			faceVerificationMgr = null;
		}

		if (trackerManager != null) {
			trackerManager.destory();
			trackerManager = null;
		}
	}

	@WorkerThread
	public interface AsyncExtractCallback {
		void onExtractStart(byte[] data, int width, int height, List<FaceInfo> faceInfos);

		void onExtractComplete(byte[] data, int width, int height, List<FaceInfo> faceInfos);
	}
}
