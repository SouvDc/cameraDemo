package com.cnbot.facelib.data;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/1/22.
 */

public class PhotoBean implements Serializable {
	private static final long serialVersionUID = -8632786943821178661L;
	// public Bitmap bitmap;
	//public byte[] bitmapArray;
	public String path;
	public float[] feature;
	public String name;
	public SerialBitmap bitmap;
	private String userId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public float[] getFeature() {
		return feature;
	}

	public void setFeature(float[] feature) {
		this.feature = feature;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SerialBitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(SerialBitmap bitmap) {
		this.bitmap = bitmap;
	}
}
