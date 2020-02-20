package com.cnbot.facelib;

/**
 * Created by uriah on 17-7-27.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import com.aimall.easylib.utils.BitmapUtils;
import com.aimall.multifacetrackerlib.bean.FaceInfo;
import com.aimall.sdk.faceverification.L;
import com.cnbot.facelib.db.DatabaseHelper;
import com.cnbot.facelib.data.PhotoBean;
import com.cnbot.facelib.data.PhotoBeanList;
import com.cnbot.facelib.utils.FileUtils;
import com.cnbot.facelib.utils.ResultUtils;

import java.io.File;
import java.util.List;

/**
 * 扫描文件夹
 */
public class ScanTask implements Runnable {
	private String dirName;
	private Context context;
	private Handler handler;

	/**
	 *
	 * @param context
	 * @param dirName
	 * @param handler
	 * @deprecated
	 * @see #ScanTask(String dirName, Handler handler)
	 */
	public ScanTask(Context context, String dirName, Handler handler) {
		this.context = context;
		this.dirName = dirName;
		this.handler = handler;
	}

	/**
	 *
	 * @param dirName
	 * @param handler
	 */
	public ScanTask(String dirName, Handler handler) {
		this.dirName = dirName;
		this.handler = handler;
	}

	@Override
	public void run() {
		File[] files = FileUtils.scanFile(dirName);
		//        PhotoBeanList.getInstance().clearList();
		//        DatabaseHelper.getInstance(context).deleteDatas();
		if (files == null) {
			return;
		}
		Bitmap bitmap;
		for (File file : files) {
			if (FileUtils.isImageFile(file)) {
				String absolutePath = file.getAbsolutePath();
				L.d("uriah", "absolutePath " + absolutePath);
				bitmap = BitmapFactory.decodeFile(absolutePath);
				List<FaceInfo> faceInfos = FaceRecognitionManager.getInstance().extractFeature(bitmap);
				if (faceInfos != null && faceInfos.size() > 0) {
					FaceInfo faceInfo = faceInfos.get(0);
					ResultUtils.saveResults(bitmap, faceInfo);
					PhotoBean bean = new PhotoBean();
					bean.feature = faceInfo.getFeatures();
					bean.path = absolutePath;

					String name = absolutePath.substring(absolutePath.lastIndexOf('/') + 1, absolutePath.lastIndexOf('.'));
					bean.name = name;
					PhotoBeanList.getInstance().addPhotoBean(bean);
					DatabaseHelper.getInstance().insertData(bean);

					handler.sendEmptyMessage(1);
				}
				BitmapUtils.recycleBitmap(bitmap);
			}
		}
		handler.sendEmptyMessage(0);
	}
}