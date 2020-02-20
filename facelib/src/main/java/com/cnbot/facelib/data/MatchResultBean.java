package com.cnbot.facelib.data;

/**
 * Created by root on 17-1-22.
 */

public class MatchResultBean {
	public PhotoBean photoBean;
	public int score;
	public float mMillTime; //执行时间，单位是ms

	public PhotoBean getPhotoBean() {
		return photoBean;
	}

	public void setPhotoBean(PhotoBean photoBean) {
		this.photoBean = photoBean;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public float getMillTime() {
		return mMillTime;
	}

	public void setMillTime(float millTime) {
		mMillTime = millTime;
	}
}
