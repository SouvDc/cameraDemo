package com.aimall.demo.faceverification.module.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Camera;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.aimall.demo.faceverification.R;
import com.aimall.demo.faceverification.module.setting.SettingConfig;
import com.aimall.demo.faceverification.utils.Constants;
import com.aimall.easylib.cameraengine.CameraEngine;
import com.aimall.easylib.cameraengine.DrawInterface;
import com.aimall.easylib.cameraengine.camera.CameraUtils;
import com.aimall.easylib.cameraengine.widget.CameraContainer;
import com.aimall.easylib.ui.BaseActivity;
import com.aimall.easylib.utils.ToastUtils;
import com.aimall.multifacetrackerlib.bean.FaceInfo;
import com.aimall.sdk.faceverification.L;
import com.cnbot.facelib.data.MatchResultBean;

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
public class CameraActivity extends BaseActivity implements ICameraView {
    @Bind(R.id.surface)
    CameraContainer cameraContainer;
    private CameraEngine cameraEngine;
    private Paint paint;
    private CameraPresenter cameraPresenter;
    private List<FaceInfo> faceInfos;
    private float lineWidth = 3;
    private float textSize = 34;

    @Override
    public int bindLayout() {
        Window window = getWindow();
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
        return R.layout.activity_camera;
    }

    @Override
    public boolean translucentStatus() {
        return false;
    }

    @Override
    public void initView(View view) {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        cameraEngine = cameraContainer.getCameraEngine();
        cameraEngine.setCameraCallBack(new CameraEngine.CameraCallBack() {
            @Override
            public void openCameraError(Exception e) {
                ToastUtils.showShort(e.getMessage());
            }

            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                cameraPresenter.onPreView(data);
            }

            @Override
            public void justOneCamera() {
                L.d("whb", "justOneCamera");
            }

            @Override
            public void openCameraSucceed(Camera mCamera, int cameraId) {
                Camera.Size sizeValue = CameraUtils.findBestPreviewSizeValue(new Point(640, 480), mCamera);
                cameraEngine.setPreviewSize(sizeValue);
            }
        });

        CameraContainer.UiConfig uiConfig = new CameraContainer.UiConfig()
                .showTakePic(true)
                .showLog(true)
                .showDrawPointsView(true)
                .showChangeImageQuality(true)
                .setCameraRotateAdjust(SettingConfig.getCameraRotateAdjust()) // 特殊设备手动适配
                .setFlipX(SettingConfig.getCameraPreviewFlipX()) // 特殊设备手动适配
                .drawInfoCanvas(new DrawInterface() {
                    @Override
                    public void onDrawToScreen(Canvas canvas) {

                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(lineWidth);
                        paint.setTextSize(textSize);
                        paint.setColor(Color.BLACK);
                        canvas.drawText(cameraEngine.getPreviewSize().width + "x" + cameraEngine.getPreviewSize().height, 80, 80, paint);

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
                                            content = "名字：" + matchResultBean.photoBean.name + "  分数：" + matchResultBean.score;


                                        } else {
                                            content = "未知" + "  分数：" + matchResultBean.score;

                                        }

                                } else {
                                    content += "未知";
                                }
                                canvas.drawText(content, left, faceInfo.getRect().top - topPading, paint);
                            }
                        }
                    }
                });
        cameraContainer.refreshConfig(uiConfig);
        cameraPresenter = new CameraPresenter(this, cameraEngine);
    }

    @Override
    public void onResume() {
        super.onResume();
        //在这里打开摄像头
        cameraEngine.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraEngine.onPause();
    }

    @Override
    public void doBusiness(Context mContext) {
    }

    @Override
    public void onFailure(Throwable e) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraPresenter.onDestory();
    }

    @Override
    public void showLogInfo(String s) {
        cameraContainer.showUserLogInfo(s);
    }

    @Override
    public void setFaceInfos(List<FaceInfo> faceInfos) {
        this.faceInfos = faceInfos;
    }

}
