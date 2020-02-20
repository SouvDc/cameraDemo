package com.aimall.demo.faceverification.module.manage;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.WorkerThread;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.aimall.demo.faceverification.R;
import com.aimall.demo.faceverification.common.widget.ProgersssDialog;
import com.aimall.easylib.WidgetStartHelper;
import com.aimall.easylib.ui.BaseActivity;
import com.aimall.easylib.utils.UriToPathUtil;
import com.aimall.multifacetrackerlib.bean.FaceInfo;
import com.aimall.sdk.faceverification.L;
import com.cnbot.facelib.FaceRecognitionManager;
import com.cnbot.facelib.ScanTask;
import com.cnbot.facelib.data.PhotoBean;
import com.cnbot.facelib.data.PhotoBeanList;
import com.cnbot.facelib.db.DatabaseHelper;
import com.cnbot.facelib.utils.FileUtils;
import com.cnbot.facelib.utils.ResultUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnItemLongClick;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;



/**
 * Created by Administrator on 2017/1/20.
 */

public class FaceManageActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener, FolderChooserDialog.FolderCallback{
    @Bind(R.id.listview)
    ListView listView;
    @Bind(R.id.image_add)
    ImageView showImg;
    @Bind(R.id.name_edt)
    EditText name_edt;
    @Bind(R.id.add_face_layout)
    RelativeLayout addFaceLayout;

    private PhotoManageAdapter adapter;
    private static final int IMAGE_RESULT_CODE = 2;// 表示打开照相机
    private static final int RESULT_REQUEST_CODE = 3; // 裁剪
    private static final int ALBUM_REQUEST_CODE = 4; // 相册
    private Bitmap currBitmap;
    ProgersssDialog progressDialog;

    int faceCount = 0;
    Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                progressDialog.dismiss();
                Toast.makeText(FaceManageActivity.this, "扫描完成 个数：" + faceCount, Toast.LENGTH_LONG).show();
                adapter.notifyDataSetChanged();
            } else if (msg.what == 1) {
                faceCount++;
            } else if (msg.what == 2) {
                //用来从数据库查询完成
                adapter.notifyDataSetChanged();
                Toast.makeText(FaceManageActivity.this, "查询数据库完成", Toast.LENGTH_SHORT).show();

            }
        }
    };
    private Uri uri;

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.take_pic:
                new RxPermissions(this).request(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(@NonNull Boolean aBoolean) throws Exception {
                                if(aBoolean) {
                                    uri = WidgetStartHelper.takePhoto(FaceManageActivity.this, IMAGE_RESULT_CODE);
                                }
                            }
                        });
                break;
            case R.id.album:
                new RxPermissions(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(@NonNull Boolean aBoolean) throws Exception {
                                if(aBoolean) {
                                    //调用相册
                                    Intent intent = new Intent(Intent.ACTION_PICK,
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(intent, ALBUM_REQUEST_CODE);
                                }
                            }
                        });
                break;
            case R.id.folder:
                new RxPermissions(this).request(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(@NonNull Boolean aBoolean) throws Exception {
                                if(aBoolean) {
                                    //TODO:
                                    FileUtils.selectFolder(FaceManageActivity.this);
                                }
                            }
                        });
                break;
        }
        return false;
    }

    @OnItemLongClick(R.id.listview)
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        PhotoBean bean = (PhotoBean) parent.getItemAtPosition(position);
        if (bean != null && bean.name != null) {
            showDeleteDialog(position, bean.name);

        }
        return false;
    }

    @OnClick({R.id.ok, R.id.iv_add})
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ok:
                final String name = name_edt.getText().toString().trim();
                if (!TextUtils.isEmpty(name) && currBitmap != null) {

                    progressDialog = new ProgersssDialog(this);
                    progressDialog.show();

                    new Thread() {
                        @Override
                        public void run() {
                            featureAndSaveFacePhoto(currBitmap, uri.getPath(), name);
                        }
                    }.start();

                } else {
                    Toast.makeText(this, "fill your infomation", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.iv_add:
                PopupMenu popup = new PopupMenu(this, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.manage_add, popup.getMenu());
                popup.setOnMenuItemClickListener(this);
                popup.show();
                break;
        }
    }

    @WorkerThread
    private boolean featureAndSaveFacePhoto(Bitmap currBitmap, String headPath, String name) {
        List<FaceInfo> faceInfos = FaceRecognitionManager.getInstance().extractFeature(currBitmap);
        final int num = faceInfos.size();
        if (num > 0) {
            FaceInfo faceInfo = faceInfos.get(0);
            //无用
            ResultUtils.saveResults(currBitmap, faceInfo);
            final PhotoBean bean = new PhotoBean();
            bean.feature = faceInfo.features;
            bean.name = name;
            bean.path = headPath;
            bean.setUserId(String.valueOf(System.currentTimeMillis()));
            PhotoBeanList.getInstance().addPhotoBean(bean);
            DatabaseHelper.getInstance().insertData(bean);
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                addFaceLayout.setVisibility(View.GONE);
                if (num > 0) {
                    Toast.makeText(FaceManageActivity.this, "add success!!", Toast.LENGTH_LONG).show();

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter = new PhotoManageAdapter(FaceManageActivity.this, PhotoBeanList.getInstance().getList());
                        listView.setAdapter(adapter);
                    }
                } else {
                    Toast.makeText(FaceManageActivity.this, "no face foundl!!", Toast.LENGTH_LONG).show();
                }
            }
        });


        return true;
    }

    private void scanDirMethod(String dirName) {
        File file = new File(dirName);
        if (!file.exists()) {
            return;
        }

        Toast.makeText(this, "正在扫描目录,请耐心等待.", Toast.LENGTH_SHORT).show();
        progressDialog = new ProgersssDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        faceCount = 0;
        new Thread(new ScanTask(this, dirName, mainHandler)).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        L.d("bai", "RESULT_REQUEST_CODE: 11");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        L.d("bai", "RESULT_REQUEST_CODE: requestCode  " + requestCode);
        switch (requestCode) {
            // 表示 调用照相机拍照
            case IMAGE_RESULT_CODE:
                String realPathFromUri = UriToPathUtil.getRealFilePath(this, uri);
                File file = new File(realPathFromUri);
                uri = FileUtils.startPictureCut(this, file, RESULT_REQUEST_CODE);
                break;

            case RESULT_REQUEST_CODE:
                //裁剪后的图片
                L.d("bai", "RESULT_REQUEST_CODE: ");
                if (data != null) {
                    getImageToView();
                    addFaceLayout.setVisibility(View.VISIBLE);
                }
                break;
            case ALBUM_REQUEST_CODE:
                Uri selectedImage = data.getData();
                String albumPath = UriToPathUtil.getRealFilePath(this, selectedImage);
                File albumFile = new File(albumPath);
                uri = FileUtils.startPictureCut(this, albumFile, RESULT_REQUEST_CODE);
                break;
        }
    }

    /**
     * 保存裁剪之后的图片数据
     */
    private void getImageToView() {
        Bitmap photo = FileUtils.getBitmapByUri(getBaseContext(), uri);
        currBitmap = photo;
        showImg.setImageBitmap(photo);
    }

    public void showDeleteDialog(final int position, final String fileName) {
        Dialog dialog = new AlertDialog.Builder(this).
                setTitle("delete photo")
                .setMessage("delete ?")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PhotoBeanList.getInstance().deletePhotoBean(position);
                        if (adapter != null)
                            adapter.notifyDataSetChanged();
                        Toast.makeText(FaceManageActivity.this, "delete success!", Toast.LENGTH_SHORT).show();

                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();

        dialog.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_facemanage;
    }

    @Override
    public boolean translucentStatus() {
        return false;
    }

    @Override
    public void initView(View view) {
        adapter = new PhotoManageAdapter(FaceManageActivity.this, PhotoBeanList.getInstance().getList());
        listView.setAdapter(adapter);

        if (PhotoBeanList.getInstance().getList().size() == 0) {
            Toast.makeText(this, "请等待，正在查询数据库", Toast.LENGTH_SHORT).show();
            new Thread() {

                @Override
                public void run() {
                    super.run();
                    PhotoBeanList.getInstance().clearList();
                    PhotoBeanList.getInstance().addList(DatabaseHelper.getInstance().queryDatas());
                    mainHandler.sendEmptyMessage(2);
                }
            }.start();
        }
    }

    @Override
    public void doBusiness(Context mContext) {

    }

    @Override
    public void onFolderSelection(@android.support.annotation.NonNull FolderChooserDialog dialog, @android.support.annotation.NonNull File folder) {
        scanDirMethod(folder.getAbsolutePath());
    }

    @Override
    public void onFolderChooserDismissed(@android.support.annotation.NonNull FolderChooserDialog dialog) {

    }
}
