package com.cnbot.facelib.db;

/**
 * Created by uriah on 17-7-27.
 */

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.aimall.sdk.faceverification.L;

/**
 * Created by uriah on 17-7-10.
 * ww modify 2018/8/22 add new field userId
 */

public class DBHelper extends SQLiteOpenHelper {
	private static final String TAG = "DatabaseHelper";
	private static final String DB_NAME = "faceVerifation";//数据库名字
	public static String TABLE_NAME = "photo";// 表名
	public static String FIELD_ID = "id";// 列名,主键
	public static String FIELD_FEATURE = "feature";
	public static String FIELD_PATH = "path";
	//ww
	public static String FIELD_USER_ID = "userId";
	private static final int DB_VERSION = 2; // 数据库版本

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	/**
	 * 创建数据库
	 * @param db
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		//创建表
		String sql = "CREATE TABLE " + TABLE_NAME + "(" + FIELD_ID + " integer primary key autoincrement , " + FIELD_FEATURE + " BLOB," + FIELD_PATH
				+ " char(200), " + FIELD_USER_ID + " varchar(200)" + ");";
		L.d("uriah", sql);
		try {
			db.execSQL(sql);
		} catch (SQLException e) {
			L.e(TAG, "onCreate " + TABLE_NAME + "Error" + e.toString());
			return;
		}
	}

	/**
	 * 数据库升级
	 * @param db
	 * @param oldVersion
	 * @param newVersion
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

}