package com.cnbot.facelib.camera;

import android.app.Activity;
import android.hardware.Camera;

import com.aimall.easylib.cameraengine.CameraEngine;
import com.aimall.easylib.mvp.BasePresenter;
import com.aimall.multifacetrackerlib.bean.FaceInfo;
import com.aimall.multifacetrackerlib.utils.PointUtils;
import com.cnbot.facelib.FaceRecognitionManager;
import com.cnbot.facelib.constant.IConfig;
import com.cnbot.facelib.data.MatchResultBean;
import com.cnbot.facelib.data.PhotoBeanList;
import com.cnbot.facelib.setting.SettingConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：aguai（吴红斌）Github地址：https://github.com/aguai1
 * 版    本：1.0
 * 创建日期：17-12-8
 * 描    述：
 * ================================================
 */
public class CameraPresenter extends BasePresenter<ICameraView> {

	private final Activity activity;
	private CameraEngine cameraEngine;
	private List<FaceInfo> cacheFaceInfos;
	private Object cacheLock = new Object();

	public CameraPresenter(final Activity activity, CameraEngine cameraEngine) {
		super(activity);
		this.activity = activity;
		this.cameraEngine = cameraEngine;

		FaceRecognitionManager.getInstance().setAsyncExtractCallback(new FaceRecognitionManager.AsyncExtractCallback() {
			@Override
			public void onExtractStart(byte[] data, int width, int height, List<FaceInfo> faceInfos) {

			}

			@Override
			public void onExtractComplete(byte[] data, int width, int height, final List<FaceInfo> faceInfos) {
				// 特征值提取完毕，此方法运行在子线程
				PhotoBeanList.getInstance().compareDatabase(faceInfos);
				synchronized (cacheLock) {
					cacheFaceInfos = faceInfos;
				}
			}
		});
	}

	/**
	 * 相机数据回调
	 *
	 * @param data
	 */
	public void onPreView(byte[] data) {
		Camera.Size previewSize = cameraEngine.getPreviewSize();
		int cameraRotate = cameraEngine.getCameraOrientation();
		cameraRotate += SettingConfig.getCameraRotateAdjust();
		cameraRotate = SettingConfig.normalizationRotate(cameraRotate);

		int imageRotate = cameraEngine.getImageRoate();
		if (SettingConfig.getNotHandDevice()) {
			imageRotate = cameraRotate;
		}
		boolean flipx = cameraEngine.isFrontCamera();
		if (SettingConfig.getCameraPreviewFlipX()) {
			flipx = !flipx;
		}
		List<FaceInfo> faceInfos;
		boolean isSync = false;
		if (isSync) {
			faceInfos = FaceRecognitionManager.getInstance().updateFrameSync(data, previewSize.width, previewSize.height, imageRotate);
			faceInfos = PointUtils.convertFaceInfo(cameraRotate, faceInfos, previewSize.width, previewSize.height, flipx);
			PhotoBeanList.getInstance().compareDatabase(faceInfos);
		} else {
			//返回的faceInfos是相对于原始数据的face信息
			faceInfos = FaceRecognitionManager.getInstance().updateFrameAsync(data, previewSize.width, previewSize.height, imageRotate);
			//将faceInfo rect 以及points旋转为屏幕方向
			faceInfos = PointUtils.convertFaceInfo(cameraRotate, faceInfos, previewSize.width, previewSize.height, flipx);
			synchronized (cacheLock) {
				if (cacheFaceInfos != null) {
					for (FaceInfo faceInfo : faceInfos) {
						for (FaceInfo cacheFaceInfo : cacheFaceInfos) {
							if (faceInfo.id == cacheFaceInfo.id) {
								// 保存异步线程缓存信息的匹配结果
								MatchResultBean tag = (MatchResultBean) cacheFaceInfo.getTag();
								faceInfo.setTag(tag);
							}
						}
					}
				}
			}

		}
		if (faceInfos != null) {
			List<float[]> facePoints = new ArrayList<>();
			for (FaceInfo faceInfo : faceInfos) {
				facePoints.add(faceInfo.getPoints());
			}
			//绘制点信息
			cameraEngine.showPoint(facePoints);
			mMvpView.setFaceInfos(faceInfos);
		} else {
			cameraEngine.showPoint(null);
			mMvpView.setFaceInfos(null);
		}
	}

	public void onDestory() {
		detachView();
	}

	public int getMaxTrackers() {
		return IConfig.MAX_TRACKER_COUNT;
	}
}
