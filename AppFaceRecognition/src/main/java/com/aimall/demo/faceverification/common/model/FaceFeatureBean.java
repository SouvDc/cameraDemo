package com.aimall.demo.faceverification.common.model;


import com.aimall.multifacetrackerlib.bean.FaceInfo;

/**
 * Created by Administrator on 2017/1/17.
 */

public class FaceFeatureBean {
    public FaceInfo getBean() {
        return bean;
    }

    public void setBean(FaceInfo bean) {
        this.bean = bean;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private FaceInfo bean;
    private int id;

    public int getMaxRectIndex() {
        return maxRectIndex;
    }

    public void setMaxRectIndex(int maxRectIndex) {
        this.maxRectIndex = maxRectIndex;
    }

    public float[] getFeatures() {
        return features;
    }

    public void setFeatures(float[] features) {
        this.features = features;
    }

    private float[] features;


    private int maxRectIndex;
}
