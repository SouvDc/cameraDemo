package com.aimall.demo.faceverification.module.video;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.aimall.demo.faceverification.R;
import com.aimall.demo.faceverification.utils.Constants;
import com.aimall.easylib.cameraengine.DrawInterface;
import com.aimall.easylib.cameraengine.widget.VideoContainer;
import com.aimall.easylib.ui.BaseActivity;
import com.aimall.multifacetrackerlib.bean.FaceInfo;
import com.cnbot.facelib.data.MatchResultBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * ================================================
 * 作    者：aguai（吴红斌）Github地址：https://github.com/aguai1
 * 版    本：1.0
 * 创建日期：17-12-6
 * 描    述：
 * ================================================
 */
public class VideoActivity extends BaseActivity implements IVideoView {
    @Bind(R.id.surface)
    VideoContainer videoContainer;
    private Paint paint;
    private VideoPresenter videoPresenter;
    private List<FaceInfo> faceInfos;
    private float lineWidth = 3;
    private float textSize = 34;
    private List<FaceInfo> ageInfos;

    @Override
    public int bindLayout() {
        Window window = getWindow();
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
        return R.layout.activity_video;
    }

    @Override
    public boolean translucentStatus() {
        return false;
    }

    @Override
    public void initView(View view) {

        videoPresenter = new VideoPresenter(this);

        videoContainer.registerListener(videoPresenter);

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        VideoContainer.UiConfig uiConfig = new VideoContainer.UiConfig()
                .showDrawPointsView(true)
                .drawInfoCanvas(new DrawInterface() {
                    @Override
                    public void onDrawToScreen(Canvas canvas) {
                    }
                    @Override
                    public void onDrawToPic(Canvas canvas, float scaleRoate) {
                        paint.setStrokeWidth(lineWidth / scaleRoate);
                        paint.setTextSize(textSize / scaleRoate);
                        float topPading = 20 / scaleRoate;
                        if (faceInfos != null) {
                            for (FaceInfo faceInfo : faceInfos) {
                                paint.setColor(Color.GRAY);
                                paint.setStyle(Paint.Style.STROKE);
                                canvas.drawRect(faceInfo.getRect(), paint);
                                paint.setColor(Color.parseColor("#66ff0000"));
                                paint.setStyle(Paint.Style.FILL);
                                float left = faceInfo.getRect().left;
                                float right = faceInfo.getRect().right;
                                float top = faceInfo.getRect().top - 3 * topPading;
                                float bottom = faceInfo.getRect().top;
                                canvas.drawRect(left, top, right, bottom, paint);
                                paint.setColor(Color.WHITE);

                                String content = "";// + faceInfo.id + " ";
                                Object tag = faceInfo.getTag();
                                if(tag != null) {
                                    MatchResultBean matchResultBean = (MatchResultBean)tag;
                                    if(matchResultBean.score > Constants.stardandScore) {
                                        content = "名字：" + matchResultBean.photoBean.name;
                                    } else {
                                        content = "未知";
                                    }
                                } else {
                                    content += "未知";
                                }
                                canvas.drawText(content, left, faceInfo.getRect().top - topPading, paint);
                            }
                        }
                    }
                });
        videoContainer.refreshConfig(uiConfig);
        videoContainer.play("video/demo.mp4");
    }

    @Override
    protected void onDestroy() {
        videoContainer.stop();
        videoContainer.unregisterListener(videoPresenter);
        videoPresenter.onDestory();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        videoContainer.resume();
    }

    @Override
    public void onPause() {
        videoContainer.pause();
        super.onPause();
    }

    @Override
    public void doBusiness(Context mContext) {
    }

    @Override
    public void onFailure(Throwable e) {

    }

    @Override
    public void updateFaceInfos(List<FaceInfo> faceInfos) {
        this.faceInfos = faceInfos;
        List<float[]> points = new ArrayList<>();
        for(FaceInfo faceInfo : faceInfos) {
            points.add(faceInfo.getPoints());
        }
//        videoContainer.showPoints(points);
    }
}
