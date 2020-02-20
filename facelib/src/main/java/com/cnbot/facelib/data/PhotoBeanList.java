package com.cnbot.facelib.data;

import android.support.annotation.NonNull;

import com.aimall.easylib.utils.FileUtils;
import com.aimall.multifacetrackerlib.bean.FaceInfo;
import com.aimall.sdk.faceverification.L;
import com.cnbot.facelib.FaceRecognitionManager;
import com.cnbot.facelib.db.DatabaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by uriah on 17-7-23.
 */

/**
 * 自定义photoBean队列。单例.未考虑并发，非线程安全
 */
public class PhotoBeanList {

	private List<PhotoBean> photoBeanList;

	private static PhotoBeanList sInstance = new PhotoBeanList();

	public static PhotoBeanList getInstance() {
		return sInstance;
	}

	private PhotoBeanList() {
		photoBeanList = new ArrayList<>();
	}

	public void addPhotoBean(@NonNull PhotoBean photoBean) {
		photoBeanList.add(photoBean);
	}

	public void deletePhotoBean(int position) {
		PhotoBean photoBean = photoBeanList.remove(position);

		if (photoBean != null) {
			DatabaseHelper.getInstance().deleteData(photoBean.path);
			FileUtils.deleteFile(photoBean.path);
		}

	}

	public void addList(List<PhotoBean> list) {
		photoBeanList.addAll(list);
	}

	public void clearList() {
		photoBeanList.clear();
	}

	/**
	 * 返回不可修改list
	 * @return
	 */
	public List<PhotoBean> getList() {

		L.d("uriah", "size" + photoBeanList.size());

		return Collections.unmodifiableList(photoBeanList);
	}

	public void compareDatabase(List<FaceInfo> faceInfos) {
		if (faceInfos != null && faceInfos.size() > 0) {
			List<PhotoBean> list = getList();
			if (list.size() == 0) {
				return;
			}

			for (FaceInfo faceInfo : faceInfos) {
				float[] faceInfoFeatures = faceInfo.getFeatures();
				if (faceInfoFeatures != null) {
					MatchResultBean matchResultBean = new MatchResultBean();
					float maxScore = 0.0f;
					for (PhotoBean photoBean : list) {
						float score = FaceRecognitionManager.caculateSimilarity(faceInfoFeatures, photoBean.feature);
						L.d("zhangc", "similarity = " + score);
						if (score > maxScore) {
							maxScore = score;
							matchResultBean.photoBean = photoBean;
							matchResultBean.score = (int) (score * 100);
						}
					}
					// 暂时存储在faceinfo的tag中
					faceInfo.setTag(matchResultBean);
				}
			}
		}
	}
}
