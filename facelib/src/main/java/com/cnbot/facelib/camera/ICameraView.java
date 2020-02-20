package com.cnbot.facelib.camera;

import com.aimall.easylib.mvp.MvpView;
import com.aimall.multifacetrackerlib.bean.FaceInfo;

import java.util.List;

/**
 * ================================================
 * 作    者：aguai（吴红斌）Github地址：https://github.com/aguai1
 * 版    本：1.0
 * 创建日期：17-12-8
 * 描    述：
 * ================================================
 */
public interface ICameraView
        extends MvpView {

    void showLogInfo(String s);

    void setFaceInfos(List<FaceInfo> faceInfos);
}
