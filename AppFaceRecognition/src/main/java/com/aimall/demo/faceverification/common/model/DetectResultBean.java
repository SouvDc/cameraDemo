package com.aimall.demo.faceverification.common.model;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2017/1/17.
 */

public class DetectResultBean {
    private Bitmap faceBitmap;
    private int maxRectIndex;

    public Bitmap getFaceBitmap() {
        return faceBitmap;
    }

    public void setFaceBitmap(Bitmap faceBitmap) {
        this.faceBitmap = faceBitmap;
    }

    public int getMaxRectIndex() {
        return maxRectIndex;
    }

    public void setMaxRectIndex(int maxRectIndex) {
        this.maxRectIndex = maxRectIndex;
    }
}
