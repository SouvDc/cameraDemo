package com.aimall.demo.faceverification.module.pic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.aimall.demo.faceverification.R;
import com.aimall.demo.faceverification.utils.CanvasUtils;
import com.aimall.easylib.WidgetStartHelper;
import com.aimall.easylib.ui.BaseActivity;
import com.aimall.easylib.utils.BitmapUtils;
import com.aimall.easylib.utils.UriToPathUtil;
import com.aimall.multifacetrackerlib.bean.FaceInfo;
import com.cnbot.facelib.FaceRecognitionManager;
import com.cnbot.facelib.data.PhotoBeanList;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PicOpActivity extends BaseActivity {
    private static final int OPEN_ALBUM = 100;
    private static final int OPEN_CAMERA = 101;
    @Bind(R.id.iv_src)
    ImageView ivSrc;
    @Bind(R.id.useTime)
    TextView useTime;
    @Bind(R.id.compare)
    View compare;
    private Uri cameraUri;
    private long runTime;
    private Bitmap selectedBitmap;
    private Bitmap delBitmap;

    @Override
    public int bindLayout() {
        Window window = getWindow();
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
        return R.layout.activity_pic;
    }

    @Override
    public boolean translucentStatus() {
        return false;
    }

    @Override
    public void initView(View view) {
        compare.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (selectedBitmap !=null&&!selectedBitmap.isRecycled()){
                            ivSrc.setImageBitmap(selectedBitmap);
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        if (delBitmap !=null &&!delBitmap.isRecycled()){
                            ivSrc.setImageBitmap(delBitmap);
                        }
                        break;
                }
                return false;
            }
        });

    }

    @Override
    public void doBusiness(Context mContext) {

    }

    @OnClick({R.id.btn_album,R.id.btn_camera, R.id.btn_compare, R.id.delwidth})
    public void onViewClicked(View view) {
        switch (view.getId()){
            case R.id.btn_album:
                new RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(@NonNull Boolean aBoolean) throws Exception {
                                if(aBoolean) {
                                    //调用相册
                                    Intent intent = new Intent(Intent.ACTION_PICK,
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(intent, OPEN_ALBUM);
                                }
                            }
                        });
                break;
            case R.id.btn_camera:
                new RxPermissions(this).request(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(@NonNull Boolean aBoolean) throws Exception {
                                if(aBoolean) {
                                    cameraUri = WidgetStartHelper.takePhoto(PicOpActivity.this, OPEN_CAMERA);
                                }
                            }
                        });
                break;
            case R.id.btn_compare:
                new RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(@NonNull Boolean aBoolean) throws Exception {
                                if(aBoolean) {
                                    startActivity(new Intent(PicOpActivity.this, PicCompareActivity.class));
                                }
                            }
                        });
                break;
            case R.id.delwidth:
                showLoading();
                Observable.create(new ObservableOnSubscribe<List<FaceInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<FaceInfo>> e) throws Exception {
                        if (selectedBitmap ==null ||selectedBitmap.isRecycled()){
                            e.onError(new Exception("请重新选择图片"));
                            return;
                        }
                        long l1 = System.currentTimeMillis();
                        final List<FaceInfo> faceInfos = FaceRecognitionManager.getInstance().extractFeature(selectedBitmap);
                        long l2 = System.currentTimeMillis();
                        runTime =l2-l1;

                        PhotoBeanList.getInstance().compareDatabase(faceInfos);
                        if (faceInfos.size()>0) {
                            e.onNext(faceInfos);
                        } else {
                            e.onError(new Exception("未检测到人脸"));
                        }
                    }
                }).subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<FaceInfo>>() {
                                       @SuppressLint("SetTextI18n")
                                       @Override
                                       public void accept(List<FaceInfo> lists) throws Exception {
                                           delBitmap = CanvasUtils.drawFaceInfoOnBitmap(selectedBitmap, lists);
                                           ivSrc.setImageBitmap(delBitmap);
                                           useTime.setText("size："+selectedBitmap.getWidth()+"x"+selectedBitmap.getHeight() + "    use Time:"+ runTime);
                                           dismissLoading();
                                       }
                                   }, new Consumer<Throwable>() {
                                       @Override
                                       public void accept(Throwable throwable) throws Exception {
                                           useTime.setText(throwable.toString());
                                           dismissLoading();
                                       }
                                   }
                        );
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_ALBUM) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri selectedImage = data.getData();
                showImage(selectedImage);
            }
        } else if (requestCode ==OPEN_CAMERA&&cameraUri!=null){
            showImage(cameraUri);
        }
    }

    private void showImage(Uri selImagePath) {
        String realPathFromUri = UriToPathUtil.getRealFilePath(this, selImagePath);
        BitmapUtils.recycleBitmap(selectedBitmap);
        BitmapUtils.recycleBitmap(delBitmap);
        selectedBitmap = BitmapUtils.getFitSampleBitmap(realPathFromUri, 2000, 2000);
        useTime.setText("");
        ivSrc.setImageBitmap(selectedBitmap);
    }

    @Override
    protected void onDestroy() {
        BitmapUtils.recycleBitmap(selectedBitmap);
        BitmapUtils.recycleBitmap(delBitmap);
        super.onDestroy();
    }


}
