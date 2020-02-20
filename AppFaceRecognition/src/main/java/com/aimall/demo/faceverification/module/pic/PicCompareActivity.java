package com.aimall.demo.faceverification.module.pic;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aimall.demo.faceverification.R;
import com.aimall.demo.faceverification.common.model.DetectResultBean;
import com.aimall.demo.faceverification.common.model.FaceFeatureBean;
import com.aimall.demo.faceverification.common.widget.ProgersssDialog;
import com.aimall.easylib.utils.BitmapUtils;
import com.aimall.multifacetrackerlib.bean.FaceInfo;
import com.aimall.sdk.faceverification.L;
import com.cnbot.facelib.FaceRecognitionManager;
import com.cnbot.facelib.utils.ResultUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.ContentValues.TAG;


/**
 */

public class PicCompareActivity extends Activity implements View.OnClickListener {
    String basePath = Environment.getExternalStorageDirectory().getPath() + "/ulsee/";
    //调用系统相册-选择图片
    private static final int IMAGE1 = 1;
    private static final int IMAGE2 = 2;
    private static final int INITDETECTSUCCESS = 3;
    private static final int INITFEATURESUCCESS = 4;
    private static final int SIMILARY_SUCCESS = 5;

    private Uri photoUri;
    private File picFile;
    private ImageView backImg, takePhotoImg1, takePhotoImg2, refreshImg;
    private RelativeLayout layout1, layout2;
    private ImageView PhotoImg1, PhotoImg2;

    private int screenWidth;
    private int screenHeigh;
    private ExecutorService executorService;

    private FaceFeatureBean featureBean1;
    private FaceFeatureBean featureBean2;

    ProgersssDialog progressDialog;
    Dialog resultDialog;
    private boolean isSelectFirstPicture = false;
    private boolean isSelectSecondPicture = false;

    Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            if (what == INITDETECTSUCCESS) {
                //Toast.makeText(PicCompareActivity.this, "detect init success", Toast.LENGTH_SHORT).show();
            } else if (what == INITFEATURESUCCESS) {
                //Toast.makeText(PicCompareActivity.this, "feature init success", Toast.LENGTH_SHORT).show();
            } else if (what == SIMILARY_SUCCESS) {
                float similary = (float) msg.obj;
                double douSimilary = (double) similary;

                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                //Toast.makeText(PicCompareActivity.this, "similary is " + similary * 100 + "%", Toast.LENGTH_SHORT).show();
                BigDecimal b = new BigDecimal(douSimilary);
                double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                showResultDialog("Similarity   : " + f1);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        executorService = Executors.newFixedThreadPool(1);
        initView();
        //application= (APP) getApplication();
    }

    private void showResultDialog(String result) {
        if (resultDialog == null) {
            resultDialog = new Dialog(this, R.style.dialog);
            resultDialog.setCancelable(true);
            resultDialog.setCanceledOnTouchOutside(false);
            resultDialog.setContentView(R.layout.dialog_layout);
        }

        TextView resultTv = (TextView) resultDialog
                .findViewById(R.id.result_tv);
        resultTv.setText(result);


        Button btnItem1 = (Button) resultDialog.findViewById(R.id.cancel_btn);
        btnItem1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultDialog.dismiss();//隐藏对话框
            }
        });

        resultDialog.show();//显示对话框
    }


    @Override
    protected void onResume() {
        super.onResume();

        DisplayMetrics dm = new DisplayMetrics();
        //获取屏幕信息
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        screenWidth = dm.widthPixels;
        screenHeigh = dm.heightPixels;
    }

    private void initView() {
        backImg = (ImageView) findViewById(R.id.back_btn);
        takePhotoImg1 = (ImageView) findViewById(R.id.take_photo1);
        takePhotoImg2 = (ImageView) findViewById(R.id.take_photo2);
        PhotoImg1 = (ImageView) findViewById(R.id.photo1);
        PhotoImg2 = (ImageView) findViewById(R.id.photo2);
        layout1 = (RelativeLayout) findViewById(R.id.layout1);
        layout2 = (RelativeLayout) findViewById(R.id.layout2);
        refreshImg = (ImageView) findViewById(R.id.refresh_btn);


        backImg.setOnClickListener(this);
        takePhotoImg2.setOnClickListener(this);
        takePhotoImg1.setOnClickListener(this);
        refreshImg.setOnClickListener(this);

        takePhotoImg1.setVisibility(View.VISIBLE);
        takePhotoImg2.setVisibility(View.VISIBLE);
        PhotoImg1.setVisibility(View.GONE);
        PhotoImg2.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                finish();
                break;
            case R.id.take_photo1:
                selectPhoto(IMAGE1);
                break;

            case R.id.take_photo2:
                selectPhoto(IMAGE2);
                break;

            case R.id.refresh_btn:
                if (isSelectSecondPicture == false || isSelectFirstPicture == false) {
                    Toast.makeText(this, "pleace select two picture!!", Toast.LENGTH_SHORT).show();
                } else {
                    if (featureBean1 == null || featureBean2 == null) {
                        Toast.makeText(this, "pleace wait detect finish!!", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog = new ProgersssDialog(this);
                        progressDialog.show();

                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                float result = startSimilary();
                                Message msg = Message.obtain();
                                msg.what = SIMILARY_SUCCESS;
                                msg.obj = result;
                                mainHandler.sendMessage(msg);
                            }
                        });
                    }
                }
                break;
        }
    }

    private float startSimilary() {
        float similary = 0;
        if (featureBean1 != null && featureBean2 != null) {
            float sim = FaceRecognitionManager.caculateSimilarity(featureBean1.getFeatures(), featureBean2.getFeatures());
            similary = sim * 100.0f;

        } else {
            //Toast.makeText(this, "pleace select two picture", Toast.LENGTH_SHORT).show();
        }
        return similary;

    }


    public void openThumb(int requestCode) {
        //调用相册
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }


    public void selectPhoto(int img_position) {
        /*ActionSheetDialog bottonDialog = new ActionSheetDialog(
                this);
        bottonDialog
                .builder()
                .setCancelable(true)
                .addSheetItem("相册", ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                //selectPhoto(PicCompareActivity.this);
                            }
                        })
                .addSheetItem("相机", ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                //takePicture(PicCompareActivity.this);// 用户点击了从照相机获取

                            }
                        });

        bottonDialog.show();*/


        //调用相册
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, img_position);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == IMAGE1 && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            showImage(imagePath, IMAGE1);
            c.close();
        }

        //获取图片路径
        if (requestCode == IMAGE2 && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            showImage(imagePath, IMAGE2);
            c.close();
        }
    }

    //加载图片
    private void showImage(String imaePath, int img_position) {
        //Bitmap bm = BitmapFactory.decodeFile(imaePath);
        //Bitmap bm = convertToBitmap(imaePath, screenWidth, screenHeigh);
        //Bitmap bm = reBitmap(imaePath);
        Bitmap bm = BitmapFactory.decodeFile(imaePath);
        if (bm != null) {
//            int w = bm.getWidth(), h = bm.getHeight();
//            int maxl = w > h ? w : h;
//            if (maxl > 600) {
//                float ratio = 600.0f / maxl;
//                int nw = (int) (w * ratio), nh = (int) (h * ratio);
//                bm = getResizedBitmap(bm, nw, nh);
//            }

            if (img_position == IMAGE1) {
                PhotoImg1.setVisibility(View.VISIBLE);
                layout1.setVisibility(View.GONE);

                PhotoImg1.setImageBitmap(bm);
                isSelectFirstPicture = true;
                featureBean1 = null;

            } else if (img_position == IMAGE2) {
                PhotoImg2.setVisibility(View.VISIBLE);
                layout2.setVisibility(View.GONE);

                PhotoImg2.setImageBitmap(bm);
                isSelectSecondPicture = true;
                featureBean2 = null;
            }

            executorService.execute(new detectTask(imaePath, bm, img_position));

        } else {
            Toast.makeText(PicCompareActivity.this, "please select camera album photo!", Toast.LENGTH_SHORT).show();
        }


    }


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }


    public class detectTask implements Runnable {
        private String imgPath;
        private Bitmap srcBitmap;
        private int img_position;


        private detectTask(String imgPath, Bitmap srcBitmap, int img_position) {
            this.imgPath = imgPath;
            this.srcBitmap = srcBitmap;
            this.img_position = img_position;

        }

        @Override
        public void run() {
            Bitmap bitmap = BitmapFactory.decodeFile(imgPath);

            List<FaceInfo> faceInfos = FaceRecognitionManager.getInstance().extractFeature(bitmap);

            if (bitmap != null) {
                //final int num = FaceRecognitionManager.getInstance().detectFace(imgMat.getNativeObjAddr(), rects, points);
                if (faceInfos.size() > 0) {
                    final FaceInfo faceInfo = faceInfos.get(0);
                    ResultUtils.saveResults(imgPath, faceInfo);
                    final Bitmap tempBitmap = srcBitmap.copy(Bitmap.Config.RGB_565, true);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            DetectResultBean resultBean = detectFaceUI(faceInfo, tempBitmap);
                            if (resultBean != null) {
                                Bitmap faceBitmap = resultBean.getFaceBitmap();
                                if (img_position == IMAGE1) {
                                    PhotoImg1.setImageBitmap(faceBitmap);
                                    featureBean1 = new FaceFeatureBean();

                                    featureBean1.setFeatures(faceInfo.getFeatures());
                                    featureBean1.setBean(faceInfo);
                                    featureBean1.setId(1);
                                    featureBean1.setMaxRectIndex(resultBean.getMaxRectIndex());

                                } else if (img_position == IMAGE2) {
                                    PhotoImg2.setImageBitmap(faceBitmap);
                                    featureBean2 = new FaceFeatureBean();

                                    featureBean2.setFeatures(faceInfo.getFeatures());

                                    featureBean2.setMaxRectIndex(resultBean.getMaxRectIndex());
                                    featureBean2.setBean(faceInfo);
                                    featureBean2.setId(2);
                                }
                            }
                        }
                    });

                }
                BitmapUtils.recycleBitmap(bitmap);
            }

        }
    }


    public DetectResultBean detectFaceUI(FaceInfo faceInfo, Bitmap srcFace) {
        int pointsStrokeWidth = 4;
        int reacStrokeWidth = 2;

        Canvas canvas = new Canvas(srcFace);
        Paint p_rect = new Paint();
        p_rect.setAntiAlias(true);
        p_rect.setStrokeWidth(reacStrokeWidth);
        p_rect.setStyle(Paint.Style.STROKE);
        p_rect.setColor(Color.GREEN);

        Paint p_point = new Paint();
        p_point.setAntiAlias(true);
        p_point.setStrokeWidth(pointsStrokeWidth);
        p_point.setStyle(Paint.Style.FILL);
        p_point.setColor(Color.RED);


        double rectAcreageMax = 0;
        int maxIndex = 0;//reac area max  index
        DetectResultBean resultBean = null;

        if (faceInfo != null) {
            canvas.drawRect(faceInfo.getRect(), p_rect);

            float[] points = faceInfo.getPoints();
            for(int i = 0; i < points.length / 2;  i++) {
                canvas.drawPoint(points[i * 2], points[i * 2 + 1], p_point);
            }

            resultBean = new DetectResultBean();
            resultBean.setFaceBitmap(srcFace);
            resultBean.setMaxRectIndex(maxIndex);
        }
        return resultBean;
    }

    public float[] feature(FaceFeatureBean featureBean) {
       /* float[] featureResult = null;
        if (featureBean != null&&application !=null&&application.face!=null&&application.getFeatureAddress()!=-1) {
            if (featureBean.getBean() != null && featureBean.getBean().getPts() != null) {
                float[] featureInputBuffer = new float[10];
                float[] detectPtsBuffer = featureBean.getBean().getPts();
                System.arraycopy(detectPtsBuffer, featureBean.getMaxRectIndex() * 10, featureInputBuffer, 0, 10);
                try {
                    featureResult = application.face.feature(application.getFeatureAddress(), featureBean.getImgeMat().getNativeObjAddr(), featureInputBuffer);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
        return featureResult;*/
        return null;
    }

    public double caculateSimilarity(float[] x1, float[] vmean, float[] x2, float[] v_range) {
        float[] cos_param1 = new float[320];
        float[] cos_param2 = new float[320];
        double score = -1;

        for (int i = 0; i < cos_param1.length; i++) {
            cos_param1[i] = (x1[i] - vmean[i]) / v_range[i];
            cos_param2[i] = (x2[i] - vmean[i]) / v_range[i];
        }

        double cosineValue = 1 - cosineSimilarity(cos_param1, cos_param2);

        //return cosineValue;

        score = 1 / (Math.exp(8 * (cosineValue - 0.42)) + 1);

        return score;
    }

    public static double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    @Override
    protected void onStop() {
        super.onStop();
        L.d(TAG, "onStop:  activity is   onstop>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>bai>>>>>>>>");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public float[] getArrayfromFile(String path) throws Exception {
        //String Vpath=basePath+"range.txt";
        File file = new File(path);
        InputStream input = new FileInputStream(file);
        byte[] b = new byte[(int) file.length()];
        input.read(b);
        String str = new String(b);
        String[] number = str.split("\n");
        float[] temp = new float[number.length];
        for (int i = 0; i < number.length; i++) {
            temp[i] = Float.parseFloat(number[i]);
        }
        input.close();
        return temp;
    }
}
