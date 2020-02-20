package com.aimall.demo.faceverification.module.video;

import android.app.Activity;

import com.aimall.easylib.cameraengine.video.core.IVideoPlayListener;
import com.aimall.easylib.cameraengine.video.core.VideoInfo;
import com.aimall.easylib.mvp.BasePresenter;
import com.aimall.multifacetrackerlib.bean.FaceInfo;
import com.aimall.sdk.faceverification.L;
import com.cnbot.facelib.FaceRecognitionManager;
import com.cnbot.facelib.data.PhotoBeanList;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：aguai（吴红斌）Github地址：https://github.com/aguai1
 * 版    本：1.0
 * 创建日期：17-12-8
 * 描    述：
 * ================================================
 */
public class VideoPresenter extends BasePresenter<IVideoView> implements IVideoPlayListener {
    private final Activity activity;
    public static final int MAX_TRACKER_COUNT = 5;
    private List<FaceInfo> cacheFaceInfos;
    private Object cacheLock = new Object();

    public VideoPresenter(Activity activity) {
        super(activity);
        this.activity = activity;

        FaceRecognitionManager.getInstance().setAsyncExtractCallback(new FaceRecognitionManager.AsyncExtractCallback() {
            @Override
            public void onExtractStart(byte[] data, int width, int height, List<FaceInfo> faceInfos) {

            }

            @Override
            public void onExtractComplete(byte[] data, int width, int height, List<FaceInfo> faceInfos) {
                // 特征值提取完毕，此方法运行在子线程
                PhotoBeanList.getInstance().compareDatabase(faceInfos);
                synchronized (cacheLock) {
                    cacheFaceInfos = faceInfos;
                }
            }
        });
    }

    public void onDestory() {
        detachView();
    }

    @Override
    public void onStart(VideoInfo info) {

    }

    @Override
    public void onStop(VideoInfo info) {

    }

    @Override
    public void onResume(VideoInfo info) {

    }

    @Override
    public void onPause(VideoInfo info) {

    }

    @Override
    public void onPreviewFrame(VideoInfo info, byte[] data) {
        //testData(data, info.width, info.height);

        List<FaceInfo> faceInfos = FaceRecognitionManager.getInstance().updateFrameSync(data, info.width, info.height, 0);
        synchronized (cacheLock) {
            if (cacheFaceInfos != null) {
                for (FaceInfo faceInfo : faceInfos) {
                    for (FaceInfo cacheFaceInfo : cacheFaceInfos) {
                        if (faceInfo.id == cacheFaceInfo.id) {
                            // 保存异步线程缓存信息的匹配结果
                            faceInfo.setTag(cacheFaceInfo.getTag());
                        }
                    }
                }
            }
        }
        L.d("VideoPresenter", "faceInfos.size=" + faceInfos.size());
        if (faceInfos != null) {
            List<float[]> facePoints = new ArrayList<>();
            for (FaceInfo faceInfo : faceInfos) {
                facePoints.add(faceInfo.getPoints());
            }
            mMvpView.updateFaceInfos(faceInfos);
        } else {
            mMvpView.updateFaceInfos(null);
        }
    }

    @Override
    public void onProcessChanged(VideoInfo info) {

    }
}
