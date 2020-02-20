package com.cnbot.facelib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import com.aimall.sdk.faceverification.L;
import com.cnbot.facelib.data.PhotoBean;
import com.cnbot.facelib.utils.FileUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by uriah on 17-7-27.
 * @descirption 数据库管理者 - 提供数据库封装
 *
 * @date modified by ww 2018.8.16
 * 1.修改初始化方式
 * 2.去掉单例参数
 */
public class DatabaseHelper {
	private static final String TAG = "DatabaseManager";
	// 静态引用
	private volatile static DatabaseHelper mInstance;
	// DatabaseHelper
	private DBHelper dbHelper;

	private static Context sContext;

	private DatabaseHelper(Context context) {
		dbHelper = new DBHelper(context.getApplicationContext());
	}

	private DatabaseHelper() {
		checkNotNull(sContext, "please init it in your application");
		dbHelper = new DBHelper(sContext.getApplicationContext());
	}

	public static @NonNull <T> T checkNotNull(T reference, final Object errorMessage) {
		if (reference == null) {
			throw new NullPointerException(String.valueOf(errorMessage));
		}
		return reference;
	}

	public static void init(Context context) {
		sContext = context;
	}

	/**
	 * 获取单例引用
	 *
	 * @param
	 * @return
	 */
	public static DatabaseHelper getInstance() {
		DatabaseHelper inst = mInstance;
		if (inst == null) {
			synchronized (DatabaseHelper.class) {
				inst = mInstance;
				if (inst == null) {
					inst = new DatabaseHelper();
					mInstance = inst;
				}
			}
		}
		return inst;
	}

	/**
	 * 获取单例引用
	 *
	 * @param context
	 * @return
	 * @deprecated  replaced by getInstance()
	 * @see #getInstance() 
	 */
	public static DatabaseHelper getInstance(Context context) {
		DatabaseHelper inst = mInstance;
		if (inst == null) {
			synchronized (DatabaseHelper.class) {
				inst = mInstance;
				if (inst == null) {
					inst = new DatabaseHelper(context);
					mInstance = inst;
				}
			}
		}
		return inst;
	}

	/**
	 * 插入数据
	 *  @see #insert(PhotoBean)
	 *  @deprecated
	 */
	public void insertData(PhotoBean photoBean) {
		//获取写数据库
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		//生成要修改或者插入的键值
		ContentValues cv = new ContentValues();

		byte[] bytes = FileUtils.ObjectToByte(photoBean);

		cv.put(DBHelper.FIELD_FEATURE, bytes);
		cv.put(DBHelper.FIELD_PATH, photoBean.path);
		//ww
		cv.put(DBHelper.FIELD_USER_ID, photoBean.getUserId());
		//cv.put(DBHelper.FIELD_FLOAT1,buff);
		// insert 操作
		db.insert(DBHelper.TABLE_NAME, null, cv);
		//关闭数据库
		db.close();
	}

	/**
	 * 插入数据  增减返回值 判定插入是否成功
	 * @author ww 
	 * @description
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long insert(PhotoBean photoBean) {
		//获取写数据库
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		//生成要修改或者插入的键值
		ContentValues cv = new ContentValues();

		byte[] bytes = FileUtils.ObjectToByte(photoBean);

		cv.put(DBHelper.FIELD_FEATURE, bytes);
		cv.put(DBHelper.FIELD_PATH, photoBean.path);
		//ww
		cv.put(DBHelper.FIELD_USER_ID, photoBean.getUserId());
		//cv.put(DBHelper.FIELD_FLOAT1,buff);
		// insert 操作
		long insert = db.insert(DBHelper.TABLE_NAME, null, cv);
		//关闭数据库
		db.close();
		return insert;
	}

	/**
	 * 未开启事务批量插入
	 * @param testCount
	 */
	/*    public void insertDatasByNomarl(int testCount){
	    //获取写数据库
	    SQLiteDatabase db = dbHelper.getWritableDatabase();
	    for(int i =0;i<testCount;i++ ){
	        //生成要修改或者插入的键值
	        ContentValues cv = new ContentValues();
	        cv.put(DBHelper.FIELD_NAME, String.valueOf(i));
	        // insert 操作
	        db.insert(DBHelper.TABLE_NAME, null, cv);
	        L.e(TAG, "insertDatasByNomarl");
	    }
	    //关闭数据库
	    db.close();
	}*/

	/**
	 * 测试开启事务批量插入
	 * @param testCount
	 */
	/*   public void insertDatasByTransaction(int testCount){
	    //获取写数据库
	    SQLiteDatabase db = dbHelper.getWritableDatabase();
	    db.beginTransaction();  //手动设置开始事务
	    try{
	        //批量处理操作
	        for(int i =0;i<testCount;i++ ){
	            //生成要修改或者插入的键值
	            ContentValues cv = new ContentValues();
	            cv.put(DBHelper.FIELD_NAME, String.valueOf(i));
	            // insert 操作
	            db.insert(DBHelper.TABLE_NAME, null, cv);
	            L.e(TAG, "insertDatasByTransaction");
	        }
	        db.setTransactionSuccessful(); //设置事务处理成功，不设置会自动回滚不提交
	    }catch(Exception e){
	
	    }finally{
	        db.endTransaction(); //处理完成
	        //关闭数据库
	        db.close();
	    }
	}*/

	/**
	 * 删除数据(根据路径)
	 */
	public void deleteData(String path) {

		String sql = "delete from photo where path = '" + path + "'";
		execSQL(sql);

		//生成条件语句
		// StringBuffer whereBuffer = new StringBuffer();
		//byte[] bytes = FileUtils.ObjectToByte(photoBean);
		//whereBuffer.append(DBHelper.FIELD_ID).append(" = ").append(position).append("'");
		//获取写数据库
		//SQLiteDatabase db = dbHelper.getWritableDatabase();
		// delete 操作
		//db.delete(DBHelper.TABLE_NAME, whereBuffer.toString(), null);
		//关闭数据库
		//db.close();
	}

	/**
	 * 删除所有数据
	 */
	public void deleteDatas() {
		String sql = "delete from " + DBHelper.TABLE_NAME;
		execSQL(sql);
	}

	/**
	 * 更新数据
	 */
	/*   public void updateData(String name) {
	    //生成条件语句
	    StringBuffer whereBuffer = new StringBuffer();
	    whereBuffer.append(DBHelper.FIELD_NAME).append(" = ").append("'").append(name).append("'");
	    //生成要修改或者插入的键值
	    ContentValues cv = new ContentValues();
	    cv.put(DBHelper.FIELD_NAME, name+name);
	    //获取写数据库
	    SQLiteDatabase db = dbHelper.getWritableDatabase();
	    // update 操作
	    db.update(DBHelper.TABLE_NAME, cv, whereBuffer.toString(), null);
	    //关闭数据库
	    db.close();
	}*/

	/**
	 * 指定条件查询数据
	 */
	/*    public void queryDatas(String name){
	    //生成条件语句
	    StringBuffer whereBuffer = new StringBuffer();
	    whereBuffer.append(DBHelper.FIELD_NAME).append(" = ").append("'").append(name).append("'");
	    //指定要查询的是哪几列数据
	    String[] columns = {DBHelper.FIELD_NAME};
	    //获取可读数据库
	    SQLiteDatabase db = dbHelper.getReadableDatabase();
	    //查询数据库
	    Cursor cursor = null;
	    try {
	        cursor = db.query(DBHelper.TABLE_NAME, columns, whereBuffer.toString(), null, null, null, null);
	        while (cursor.moveToNext()) {
	            int count = cursor.getColumnCount();
	            String columName = cursor.getColumnName(0);
	            String  tname = cursor.getString(0);
	            L.e(TAG, "count = " + count + " columName = " + columName + "  name =  " +tname);
	        }
	        if (cursor != null) {
	            cursor.close();
	        }
	    } catch (SQLException e) {
	        L.e(TAG, "queryDatas" + e.toString());
	    }
	    //关闭数据库
	    db.close();
	}*/

	/**
	 * 查询全部数据
	 */
	public List<PhotoBean> queryDatas() {

		List<PhotoBean> photoBeanList = new ArrayList<>();
		//指定要查询的是哪几列数据
		String[] columns = { DBHelper.FIELD_FEATURE };
		//获取可读数据库
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		//查询数据库
		Cursor cursor = null;
		try {
			cursor = db.query(DBHelper.TABLE_NAME, columns, null, null, null, null, null);//获取数据游标

			while (cursor.moveToNext()) {

				byte[] bytes = cursor.getBlob(0);

				PhotoBean photoBean = FileUtils.ByteToObject(bytes);
				photoBeanList.add(photoBean);
			}
			//关闭游标防止内存泄漏
			if (cursor != null) {
				cursor.close();
			}
		} catch (SQLException e) {
			L.e(TAG, "queryDatas" + e.toString());
		}
		//关闭数据库
		db.close();
		return photoBeanList;

	}

	/**
	 * 执行sql语句
	 */
	private void execSQL(String sql) {
		//获取写数据库
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		//直接执行sql语句
		db.execSQL(sql);//或者
		//关闭数据库
		db.close();
	}

	/**
	 * 更新数据
	 * @author ww
	 * @description 通过userId或者memberId过滤
	 *
	 */
	public boolean updateByFilter(PhotoBean photoBean) {
		if (photoBean == null)
			return false;
		final String userId = photoBean.getUserId();
		PhotoBean bean = queryByUserId(userId);
		//数据库未存在此userId则直接插入
		if (bean == null) {
			long insert = insert(photoBean);
			return insert != -1;
		}
		return updateByUserId(photoBean) > 0;
	}

	/**
	 * 通过userId更新，必须确保包含此userId
	 * @param photoBean
	 * @return update 大于0表示有更新
	 */
	public int updateByUserId(PhotoBean photoBean) {
		//获取写数据库
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		//生成要修改或者插入的键值
		ContentValues cv = new ContentValues();

		byte[] bytes = FileUtils.ObjectToByte(photoBean);

		cv.put(DBHelper.FIELD_FEATURE, bytes);
		cv.put(DBHelper.FIELD_PATH, photoBean.path);

		String where = DBHelper.FIELD_USER_ID + "=?";
		final String userId = photoBean.getUserId();
		int update = db.update(DBHelper.TABLE_NAME, cv, where, new String[] { userId });
		//关闭数据库
		db.close();
		return update;
	}

	/**
	 * @author ww
	 * @date 2018.9.13
	 * @param userId
	 * @return
	 * @description 根据userId查询PhotoBean
	 */
	public PhotoBean queryByUserId(String userId) {
		//获取可读数据库
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		//指定要查询的数据
		String[] columns = { DBHelper.FIELD_FEATURE, DBHelper.FIELD_PATH };
		String selection = DBHelper.FIELD_USER_ID + "=?";
		String[] selectionArgs = { userId };
		Cursor cursor = db.query(DBHelper.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
		if (cursor == null)
			return null;
		PhotoBean photoBean = null;
		if (cursor.moveToNext()) {
			byte[] bytes = cursor.getBlob(0);
			photoBean = FileUtils.ByteToObject(bytes);

		}
		cursor.close();
		db.close();
		return photoBean;
	}
}
