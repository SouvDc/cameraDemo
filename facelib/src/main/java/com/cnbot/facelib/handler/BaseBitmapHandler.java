package com.cnbot.facelib.handler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/*
 *  @项目名：  demo 
 *  @包名：    com.cnbot.facelib.handler
 *  @文件名:   BaseBitmapHandler
 *  @创建者:   Administrator
 *  @创建时间:  2018/9/10 9:20
 *  @描述：    主要用于处理camera预览数据相关的Handler，与HandlerThread结合使用
 *  @see #HandlerThread
 */
public class BaseBitmapHandler extends Handler {

	private static final String TAG = "BaseBitmapHandler";
	private HandlerThread mHandlerThread;
	private IBitmapHandlerListener mListener;

	public BaseBitmapHandler(HandlerThread handlerThread) {
		this(handlerThread, null);
	}

	public BaseBitmapHandler(HandlerThread handlerThread, IBitmapHandlerListener l) {
		super(handlerThread.getLooper());

		this.mHandlerThread = handlerThread;
		mListener = l;
	}

	@Override
	public void handleMessage(Message msg) {
		Bundle bundle = msg.getData();
		final byte[] previews = bundle.getByteArray("preview");
		if (previews == null || previews.length == 0) {
			Log.e(TAG, "preview data is null or empty,please check camera is preview success");
			return;
		}
		final int[] dimens = bundle.getIntArray("dimen");
		final int rotation = bundle.getInt("rotation");
		Bitmap bitmap = createBitmap(previews, dimens, rotation);
		if (mListener != null)
			mListener.onHandlerResult(bitmap);

	}

	/**
	 *
	 * @param dimens 预览的宽高
	 * @param previewData
	 * @param rotation
	 */
	public void sendCameraMsg(int[] dimens, byte[] previewData, int rotation) {
		Message message = Message.obtain();
		Bundle bundle = new Bundle();
		bundle.putIntArray("dimen", dimens);
		bundle.putByteArray("preview", previewData);
		//针对某些摄像头安装方向不正常
		bundle.putInt("rotation", rotation);
		message.setData(bundle);
		sendMessage(message);
	}

	/**
	 * 移除队列中的所有消息
	 */
	public void quitHandler() {
		removeCallbacks(null);
	}

	/**
	 * 停止子线程消息轮询
	 * notice：调用该方法后线程会停止，如果还需要继续使用handler建议调用
	 * @see #quitHandler() 在界面销毁时可以调用此方法
	 */
	public void quitHandlerThread() {
		if (mHandlerThread != null)
			mHandlerThread.quit();
	}

	/**
	 * 将预览的一帧数据转成bitmap
	 * @param previews
	 * @param dimens
	 * @param rotation
	 * @return
	 */
	protected Bitmap createBitmap(byte[] previews, int[] dimens, int rotation) {
		final int width = dimens[0];
		final int height = dimens[1];
		YuvImage image = new YuvImage(previews, ImageFormat.NV21, width, height, null);
		Bitmap bitmap = null;
		if (null != image && !image.equals("")) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			image.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
			Matrix matrix = new Matrix();
			matrix.postRotate(rotation);
			bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}

}
