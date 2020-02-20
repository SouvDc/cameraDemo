package com.cnbot.facelib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;

import com.aimall.easylib.utils.ScreenUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/*
 *  @项目名：  demo 
 *  @包名：    com.cnbot.facelib.utils
 *  @文件名:   BitmapUtils
 *  @创建者:   Administrator
 *  @创建时间:  2018/9/10 9:05
 *  @描述：    处理人脸识别后的图片
 */
public class BitmapUtils {

	/**
     * 预览时录入人脸的Bitmap的宽高与识别到人脸的RectF的边界
	 * 边界判定和处理
	 * @param rectF
	 * @return
	 */
	private static RectF checkBounds(Context context,Bitmap bitmap, RectF rectF) {
		float startX = rectF.left;
		float startY = rectF.top;
		int temp = ScreenUtils.convertDip2Px(context, 150f);
		startY = startY > temp ? startY - temp : 0;
		if (startX < 0)
			startX = 0;
		float rectW = rectF.right - startX;
		float rectH = rectF.bottom - startY;
		int bitmapW = bitmap.getWidth();
		int bitmapH = bitmap.getHeight();
		//取最大值作为边长
		float cropWidth = rectW > rectH ? rectW : rectH;
		float tempX = startX + cropWidth;
		float tempY = startY + cropWidth;
		//x方向越界
		boolean tag1 = true;
		boolean tag2 = true;
		if (tempX > bitmapW) {
			tag1 = false;
			startX = bitmapW - cropWidth;
		}
		//y方向越界
		if (tempY > bitmapH) {
			tag2 = false;
			startY = bitmapH - cropWidth;
		}
		//如果x,y方向都未超过bitmap边界
		if (tag1 && tag2) {
			//由于框已做加高处理，需要修改x方向的值让头像居中
			if (rectH > rectW) {
				startX -= (rectH - rectW) / 2;
				if (startX < 0)
					startX = 0;
			}
		}

		return new RectF(startX, startY, startX + cropWidth, startY + cropWidth);
	}


	/**
	 * 截取识别到的人脸
	 * @param bitmap
	 * @param rect
	 * @return
	 */
	public static Bitmap cropBitmap(Context context,Bitmap bitmap, RectF rect) {
		//考虑到返回的RectF并未完全框住人脸,主要是顶部未包含头发，需要新建一个合适的RectF
		RectF rectF = checkBounds(context,bitmap, rect);
		int startX = (int) rectF.left;
		int startY = (int) rectF.top;
		int cropWidth = (int) (rectF.right - startX);
		bitmap = Bitmap.createBitmap(bitmap, startX, startY, cropWidth, cropWidth);
		return cropCircleBitmap(bitmap, cropWidth);
	}

	/**
	 *
	 * @param bitmap
	 * @param diameter 圆形直径
	 * @return
	 * 把图片裁剪成圆形
	 */
	public static Bitmap cropCircleBitmap(Bitmap bitmap, int diameter) {

		Bitmap circleBitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(circleBitmap);
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(Color.WHITE);
		RectF rect = new RectF(0, 0, diameter, diameter);
		canvas.drawRoundRect(rect, diameter, diameter, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		final Rect src = new Rect(0, 0, diameter, diameter);
		canvas.drawBitmap(bitmap, src, rect, paint);
		return circleBitmap;
	}


	/**
	 * 将预览的一帧数据转成bitmap
	 * @date 2018.9.14
	 * @param previews
	 * @param dimens
	 * @param rotation
	 * @return
	 */
	public static Bitmap createBitmap(byte[] previews, int[] dimens,int rotation) {
		final int width  = dimens[0];
		final int height = dimens[1];
		YuvImage  image  = new YuvImage(previews, ImageFormat.NV21, width, height, null);
		Bitmap    bitmap = null;
		if (null != image && !image.equals("")) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			image.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
			bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
			Matrix matrix = new Matrix();
			matrix.postRotate(rotation);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),matrix,true);
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}

	/**
	 * 绘制镜像的Bitmap
	 * @date 2018.9.14
	 * @param canvas
	 * @param src
	 * @param width
	 * @param height
	 */
	public static Bitmap drawMirrorBitmap(Canvas canvas,Bitmap src,int width,int height) {

		Bitmap scaledBitmap = Bitmap.createScaledBitmap(src,width , height, true);
		Matrix matrix = new Matrix();
		matrix.setScale(-1, 1);
		matrix.postTranslate(scaledBitmap.getWidth(), 0);
		if (canvas!=null)
			canvas.drawBitmap(scaledBitmap,matrix,null);
		return scaledBitmap;
	}


}
