package com.aimall.demo.faceverification;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aimall.demo.faceverification.module.camera.CameraServiceActivity;
import com.aimall.demo.faceverification.module.manage.FaceManageActivity;
import com.aimall.demo.faceverification.module.pic.PicOpActivity;
import com.aimall.demo.faceverification.module.register.FaceRegisterActivity;
import com.aimall.demo.faceverification.module.setting.SettingActivity;
import com.aimall.demo.faceverification.usb.UsbActivity;
import com.aimall.easylib.EasyLibUtils;
import com.aimall.easylib.utils.SharedPreferencesUtils;
import com.aimall.easylib.utils.ToastUtils;
import com.aimall.easylib.widget.DialogInputKey;
import com.aimall.sdk.faceverification.BuildConfig;
import com.aimall.sdk.faceverification.FaceVerificationMgr;
import com.cnbot.facelib.FaceRecognitionManager;
import com.cnbot.facelib.camera.CameraPreviewActivity;
import com.cnbot.facelib.constant.IConfig;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/1/9.
 */

public class MainActivity extends Activity {

	@Bind(R.id.recyclerView)
	RecyclerView recyclerView;
	@Bind(R.id.tv_version)
	TextView tvVersion;
	private static DialogInputKey dialogInputKey;

	List<String> titles = new ArrayList<>();
	List<Class> activities = new ArrayList<>();
	private boolean succeedInit;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hello);
		ButterKnife.bind(this);

		activities.add(FaceManageActivity.class);
		activities.add(PicOpActivity.class);
		//        activities.add(CameraActivity.class);
		//TODO:
		activities.add(CameraPreviewActivity.class);
		//ww
		activities.add(CameraServiceActivity.class);
		activities.add(UsbActivity.class);
		activities.add(FaceRegisterActivity.class);
		activities.add(FaceRecordActivity.class);
		//        activities.add(VideoActivity.class);
		titles.add("底库管理");
		titles.add("图片处理");
		titles.add("相机预览");

		//WW 2018.8.22
		titles.add("后台识别");
		titles.add("USB摄像头");
		titles.add("实时预览录入人脸");
		titles.add("后台人脸预览");
		//        titles.add("视频预览");
		Adpter adpter = new Adpter(activities, titles);
		recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
		recyclerView.setAdapter(adpter);
		tvVersion.setText("sdk版本:" + FaceVerificationMgr.getVersion() + "  demo版本:" + BuildConfig.VERSION_NAME);

		dialogInputKey = new DialogInputKey(EasyLibUtils.sTopActivityWeakRef.get()).setOkListenter(new DialogInputKey.OnClickOkListener() {
			@Override
			public void onClick(String text) {
				succeedInit = checkKey(text);
				if (succeedInit) {
					dialogInputKey.dismiss();
				} else {
					ToastUtils.showShort("授权失败,请确保Constants.java变量KEY填写正确！！！");
				}
				SharedPreferencesUtils.put("appkey", text);
			}
		}).builder();

		succeedInit = checkKey(IConfig.KEY);

		if (!succeedInit) {
			String appkey = (String) SharedPreferencesUtils.get("appkey", "");
			succeedInit = checkKey(appkey);
			if (!succeedInit) {
				dialogInputKey.show(appkey);
			}
		}
	}

	public boolean checkKey(String key) {
		return FaceRecognitionManager.getInstance().initFaceRecognition(getApplicationContext(), key, true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		FaceRecognitionManager.getInstance().release();
	}

	@OnClick(R.id.setting)
	public void onCLickSetting() {
		startActivity(new Intent(this, SettingActivity.class));
	}

	class Adpter extends RecyclerView.Adapter<Adpter.ViewHodler> {

		private final List<Class> activitys;
		private final List<String> titles;

		public Adpter(List<Class> activities, List<String> titles) {
			this.activitys = activities;
			this.titles = titles;
		}

		@Override
		public ViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.item_function, parent, false);
			return new ViewHodler(view);
		}

		@Override
		public void onBindViewHolder(ViewHodler holder, int position) {

			holder.itemName.setText(titles.get(position));
		}

		@Override
		public int getItemCount() {
			return activitys.size();
		}

		class ViewHodler extends RecyclerView.ViewHolder {
			TextView itemName;

			public ViewHodler(View itemView) {
				super(itemView);
				itemName = itemView.findViewById(R.id.item_name);
				itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						startActivity(new Intent(MainActivity.this, activities.get(getAdapterPosition())));
					}
				});
			}
		}
	}

}
