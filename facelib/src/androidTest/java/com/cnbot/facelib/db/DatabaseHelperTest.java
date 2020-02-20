package com.cnbot.facelib.db;

import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.cnbot.facelib.data.PhotoBean;

import org.junit.Test;

/*
 *  @项目名：  demo 
 *  @包名：    com.cnbot.facelib.db
 *  @文件名:   DatabaseHelperTest
 *  @创建者:   Administrator
 *  @创建时间:  2018/9/13 15:46
 *  @描述：    TODO
 */
public class DatabaseHelperTest {

	@Test
	public void insert() throws Exception {

		DatabaseHelper helper = DatabaseHelper.getInstance(InstrumentationRegistry.getTargetContext());

		PhotoBean photoBean = new PhotoBean();
		for (int i = 0; i < 5; i++) {

			photoBean.setUserId(i + "");
			photoBean.setName("w" + i);
			long insert = helper.insert(photoBean);
			Log.v("insert()", "insert:" + insert);
		}

	}

	@Test
	public void updateByFilter() throws Exception {
		insert();
		PhotoBean photoBean = new PhotoBean();
		photoBean.setUserId("2");
		photoBean.setName("www");
		boolean filter = DatabaseHelper.getInstance(InstrumentationRegistry.getTargetContext()).updateByFilter(photoBean);
		Log.v("updateByFilter", "filter:" + filter);

	}

	@Test
	public void queryByUserId() throws Exception {

		insert();
		PhotoBean bean = DatabaseHelper.getInstance(InstrumentationRegistry.getTargetContext()).queryByUserId("6");
		String name = bean == null ? "null" : bean.getName();

		Log.v("queryByUserId", "PhotoBean:" + name);

	}

	@Test
	public void updateByUserId() throws Exception {
		insert();
		PhotoBean photoBean = new PhotoBean();
		photoBean.setUserId("4");
		photoBean.setName("www");
		int index = DatabaseHelper.getInstance(InstrumentationRegistry.getTargetContext()).updateByUserId(photoBean);

		Log.v("updateByUserId", "result:" + index);

	}
}