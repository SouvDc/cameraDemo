package com.cnbot.facelib.utils;

import android.graphics.Bitmap;

import com.aimall.easylib.utils.BitmapUtils;
import com.aimall.multifacetrackerlib.bean.FaceInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zhangchao on 18-1-29.
 */

public class ResultUtils {

	public static void saveResults(String originImagePath, FaceInfo faceInfo) {
		String cachePath = com.aimall.easylib.Constants.getCachePath();
		File fromFile = new File(originImagePath);
		com.aimall.easylib.utils.FileUtils.copyFile(fromFile, new File(cachePath, fromFile.getName()),
				new com.aimall.easylib.utils.FileUtils.OnReplaceListener() {
					@Override
					public boolean onReplace() {
						return true;
					}
				});
		saveFaceInfo(faceInfo);
	}



	public static void saveResults(Bitmap origin, FaceInfo faceInfo) {
		String cachePath = com.aimall.easylib.Constants.getCachePath();
		BitmapUtils.savePhotoToSDCard(origin, cachePath,  "test.jpg");
		saveFaceInfo(faceInfo);
	}

	/**
	 * ww add
	 * @param origin
	 * @param faceInfo
	 * @param photoName
	 * @date 2018.9.6
	 * @description  保存人脸录入的图像
	 */
	public static void saveBitmap(Bitmap origin, FaceInfo faceInfo, String photoName) {
		String cachePath = com.aimall.easylib.Constants.getCachePath();
		BitmapUtils.savePhotoToSDCard(origin, cachePath, photoName + ".jpg");

	}

	private static void saveFaceInfo(FaceInfo faceInfo) {
		String cachePath = com.aimall.easylib.Constants.getCachePath();

		// 可以建立一个子目录专门存放自己专属文件
		File dir = new File(cachePath);
		dir.mkdir();

		File file = new File(dir.getAbsolutePath(), "faceInfo.txt");

		// 创建这个文件，如果不存在
		try {
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);

			byte[] buffer = faceInfo.toString().getBytes();

			// 开始写入数据到这个文件。
			fos.write(buffer, 0, buffer.length);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
