package com.aimall.demo.faceverification.module.register;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.aimall.multifacetrackerlib.bean.FaceInfo;
import com.cnbot.facelib.FaceRecognitionManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/*
 *  @项目名：  EcologyRobot2
 *  @包名：    com.cnbot.ecologyrobot2.face
 *  @文件名:   WorkHandler
 *  @创建者:   Administrator
 *  @创建时间:  2018/9/5 20:05
 *  @描述：    子线程生成图片，并进行人脸校验
 */
public class WorkHandler
        extends Handler {
	private static final int FACE = 0;

	public interface IHandlerListener {

		/**
		 *
		 * @param faceInfos
		 * @param bitmap
		 */
		void onResult(List<FaceInfo> faceInfos, Bitmap bitmap);
	}

	private IHandlerListener mIHandlerListener;

	public WorkHandler(Looper looper) {
		super(looper);
	}

	public void setListener(IHandlerListener l) {
		mIHandlerListener = l;
	}

	@Override
	public void handleMessage(Message msg) {
		Bundle bundle = msg.getData();
		final byte[] previews = bundle.getByteArray("preview");
		final int[] dimens = bundle.getIntArray("dimen");
		final int rotation = bundle.getInt("rotation");
		Bitmap bitmap = createBitmap(previews, dimens,rotation);
		if (bitmap == null)
			return;

		List<FaceInfo> faceInfos = FaceRecognitionManager.getInstance().extractFeature(bitmap);
		if (mIHandlerListener != null)
			mIHandlerListener.onResult(faceInfos,bitmap);

	}

	private Bitmap createBitmap(byte[] previews, int[] dimens, int rotation) {
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
