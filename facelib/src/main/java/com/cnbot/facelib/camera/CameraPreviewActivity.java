package com.cnbot.facelib.camera;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.aimall.easylib.cameraengine.CameraEngine;
import com.aimall.easylib.cameraengine.DrawInterface;
import com.aimall.easylib.cameraengine.camera.CameraUtils;
import com.aimall.easylib.cameraengine.widget.CameraContainer;
import com.aimall.easylib.utils.ToastUtils;
import com.aimall.multifacetrackerlib.bean.FaceInfo;
import com.aimall.sdk.faceverification.L;
import com.cnbot.facelib.constant.IConfig;
import com.cnbot.facelib.data.MatchResultBean;
import com.cnbot.facelib.setting.SettingConfig;

import java.util.List;

/**
 * ================================================
 * 作    者：aguai（吴红斌）Github地址：https://github.com/aguai1
 * 版    本：1.0
 * 创建日期：17-12-6
 * 描    述：
 * ================================================
 */
public class CameraPreviewActivity extends AppCompatActivity implements ICameraView {
	CameraContainer cameraContainer;
	private CameraEngine cameraEngine;
	private Paint paint;
	private CameraPresenter cameraPresenter;
	private List<FaceInfo> faceInfos;
	private float lineWidth = 3;
	private float textSize = 34;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initView();

	}

	private void initCameraContainer() {

		cameraContainer = new CameraContainer(this);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		cameraContainer.setLayoutParams(params);
		setContentView(cameraContainer);

	}

	public void initView() {

		initCameraContainer();

		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		cameraEngine = this.cameraContainer.getCameraEngine();
		cameraEngine.setCameraCallBack(new CameraEngine.CameraCallBack() {
			@Override
			public void openCameraError(Exception e) {
				ToastUtils.showShort(e.getMessage());
			}

			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				cameraPresenter.onPreView(data);
			}

			@Override
			public void justOneCamera() {
				L.d("whb", "justOneCamera");
			}

			@Override
			public void openCameraSucceed(Camera mCamera, int cameraId) {
				Camera.Size sizeValue = CameraUtils.findBestPreviewSizeValue(new Point(640, 480), mCamera);
				cameraEngine.setPreviewSize(sizeValue);
			}
		});

		CameraContainer.UiConfig uiConfig = new CameraContainer.UiConfig().showTakePic(true).showLog(true).showDrawPointsView(true)
				.showChangeImageQuality(true).setCameraRotateAdjust(SettingConfig.getCameraRotateAdjust()) // 特殊设备手动适配
				.setFlipX(SettingConfig.getCameraPreviewFlipX()) // 特殊设备手动适配
				.drawInfoCanvas(new DrawInterface() {
					@Override
					public void onDrawToScreen(Canvas canvas) {

						paint.setStyle(Paint.Style.STROKE);
						paint.setStrokeWidth(lineWidth);
						paint.setTextSize(textSize);
						paint.setColor(Color.BLACK);
						canvas.drawText(cameraEngine.getPreviewSize().width + "x" + cameraEngine.getPreviewSize().height, 80, 80, paint);

					}

					@Override
					public void onDrawToPic(Canvas canvas, float scaleRoate) {
						paint.setStrokeWidth(lineWidth / scaleRoate);
						paint.setTextSize(textSize / scaleRoate);
						float topPading = 20 / scaleRoate;
						if (faceInfos != null) {
							for (FaceInfo faceInfo : faceInfos) {
								paint.setColor(Color.GRAY);
								paint.setStyle(Paint.Style.STROKE);
								canvas.drawRect(faceInfo.getRect(), paint);
								paint.setColor(Color.parseColor("#66ff0000"));
								paint.setStyle(Paint.Style.FILL);
								float left = faceInfo.getRect().left;
								float right = faceInfo.getRect().right;
								float top = faceInfo.getRect().top - 3 * topPading;
								float bottom = faceInfo.getRect().top;
								canvas.drawRect(left, top, right, bottom, paint);
								paint.setColor(Color.WHITE);

								String content = "";// + faceInfo.id + " ";

								Object tag = faceInfo.getTag();
								if (tag != null) {
									MatchResultBean matchResultBean = (MatchResultBean) tag;

									if (matchResultBean.score > IConfig.STANDARD_SCORE) {
										content = "名字：" + matchResultBean.photoBean.name + "  分数：" + matchResultBean.score;

									} else {
										content = "未知" + "  分数：" + matchResultBean.score;

									}

								} else {
									content += "未知";
								}
								canvas.drawText(content, left, faceInfo.getRect().top - topPading, paint);
							}
						}
					}
				});
		this.cameraContainer.refreshConfig(uiConfig);
		cameraPresenter = new CameraPresenter(this, cameraEngine);
	}

	@Override
	public void onResume() {
		super.onResume();
		cameraEngine.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		cameraEngine.onPause();
	}

	@Override
	public void onFailure(Throwable e) {

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cameraPresenter.onDestory();
	}

	@Override
	public void showLogInfo(String s) {
		cameraContainer.showUserLogInfo(s);
	}

	@Override
	public void setFaceInfos(List<FaceInfo> faceInfos) {
		this.faceInfos = faceInfos;
	}

}
