package com.aimall.demo.faceverification.module.register;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.aimall.demo.faceverification.R;
import com.cnbot.facelib.camera.BaseFaceRecogActivity;
import com.cnbot.facelib.handler.BaseBitmapHandler;
import com.cnbot.facelib.handler.IBitmapHandlerListener;

import java.io.File;

/*
 *  @项目名：  EcologyRobot2
 *  @包名：    com.cnbot.ecologyrobot2.base
 *  @文件名:   BaseApp
 *  @创建者:   Administrator
 *  @创建时间:  2018/8/9 11:25
 *  @描述：    人脸注册
 */
public class FaceRegisterActivity extends BaseFaceRecogActivity implements IBitmapHandlerListener {

	private static final String TAG = "FaceRegisterActivity";

	/**
	 * 人脸录入的图像位置
	 */
	public static final String FACE_IMG = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "face" + File.separator;

	/**
	 * 人脸录入的时间限制，单位毫秒
	 */
	private static final long PERIOD = 20000;
	/**
	 * 识别的间隔，一般识别一张图片是300ms
	 */
	private static final long INTERVAL = 1000;
	private ImageView mIvFaceFrame;
	private BaseBitmapHandler mWorkHandler;
	/**
	 * 预览数据，用于生成Bitmap
	 */
	private Button mBtnRepeat;

	@Override
	protected SurfaceView initView() {
		return initUi();
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_face_register;
	}

	public static void startActivity(Activity context, UserBean bean) {
		Intent intent = new Intent(context, FaceRegisterActivity.class);
		intent.putExtra("user_info", bean);
		context.startActivity(intent);
		context.finish();
	}

	public SurfaceView initUi() {

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_view_preview);
		mIvFaceFrame = (ImageView) findViewById(R.id.iv_face_recog_frame);
		initSurfaceView(surfaceView);
		mBtnRepeat = (Button) findViewById(R.id.btn_register_repeat);
		mBtnRepeat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mBtnRepeat.setVisibility(View.GONE);
				cancelTimer();
				startTimer();
			}
		});
		return surfaceView;

	}

	@Override
	protected void onSurfaceCreated(SurfaceHolder holder) {
		initWorkHandler();

		startTimer();

	}

	@Override
	protected void onSurfaceDestroyed(SurfaceHolder holder) {
		quitHandlerThread();
		cancelTimer();
	}

	private void initWorkHandler() {
		HandlerThread mHandlerThread = new HandlerThread("preview");
		mHandlerThread.start();
		mWorkHandler = new BaseBitmapHandler(mHandlerThread, this);
	}

	private void quitHandler() {
		if (mWorkHandler != null)
			mWorkHandler.quitHandler();
	}

	private void quitHandlerThread() {
		if (mWorkHandler != null)
			mWorkHandler.quitHandlerThread();
	}

	private void startTimer() {
		if (mDownTimer != null)
			mDownTimer.start();
	}

	private void cancelTimer() {
		if (mDownTimer != null)
			mDownTimer.cancel();
	}

	/**
	 * 倒计时器，用于限制识别预览的人脸图片的时间
	 */
	private final CountDownTimer mDownTimer = new CountDownTimer(PERIOD, INTERVAL) {
		@Override
		public void onTick(long millisUntilFinished) {
			Log.e(TAG, "onTick");

			sendHandlerMsg();
		}

		@Override
		public void onFinish() {
			Toaster.showTopToast("人脸录入超时");
			mBtnRepeat.setVisibility(View.VISIBLE);
		}
	};

	private void sendHandlerMsg() {
		if (mWorkHandler != null) {
			mWorkHandler.sendCameraMsg(getPreviewSize(null), getPreviewData(), getDisplayOrientation());
		}

	}

	@Override
	public void onHandlerResult(Bitmap bitmap) {

	}

	@Override
	protected int getDisplayOrientation() {
		return 180;
	}
}
