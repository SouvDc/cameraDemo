package com.aimall.demo.faceverification.module.camera;

import com.aimall.multifacetrackerlib.bean.FaceInfo;

import java.util.List;

import com.aimall.easylib.mvp.MvpView;

/**
 * ================================================
 * 作    者：aguai（吴红斌）Github地址：https://github.com/aguai1
 * 版    本：1.0
 * 创建日期：17-12-8
 * 描    述：
 * ================================================
 */
public interface ICameraView extends MvpView {

    void showLogInfo(String s);

    void setFaceInfos(List<FaceInfo> faceInfos);
}
