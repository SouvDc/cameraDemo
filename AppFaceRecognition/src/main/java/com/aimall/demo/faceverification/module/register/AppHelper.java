package com.aimall.demo.faceverification.module.register;

import android.content.Context;
import android.os.Handler;

/*
 *  @项目名：  EcologyRobot2 
 *  @包名：    com.cnbot.ecologyrobot2.util
 *  @文件名:   AppHelper
 *  @创建者:   Administrator
 *  @创建时间:  2018/8/9 11:27
 *  @描述：    提供application级别的上下文和全局的Handler
 */
public class AppHelper {

	private static Context sContext;
	private static Handler sHandler;

	/**
	 * please init it in your custom application
	 * @param context
	 */
	public static void init(Context context) {
		sContext = context;
		sHandler = new Handler();

	}

	private static void checkNotNull(Object obj, String msg) {
		if (obj == null)
			throw new NullPointerException(msg);

	}

	private static void checkNotNull(Object obj) {
		if (obj == null)
			throw new NullPointerException("you must init the util in your application");

	}

	public static Context getContext() {
		checkNotNull(sContext);
		return sContext;
	}

	public static Handler getHandler() {
        checkNotNull(sHandler);
		return sHandler;
	}

    /**
     * work thread -->  main thread
     * @param r
     */
	public static void post(Runnable r) {
        checkNotNull(sHandler);
		sHandler.post(r);

	}

    /**
     * 延时发送
     * work thread -->  main thread
     * @param r
     * @param delay
     */
	public static void postDelayed(Runnable r, long delay) {
        checkNotNull(sHandler);
		sHandler.postDelayed(r, delay);
	}

    /**
     * 移除消息队列中的某个任务
     * @param r
     */
	public static void remove(Runnable r) {
        checkNotNull(sHandler);
		sHandler.removeCallbacks(r);

	}
    /**
     * 移除消息队列中的所有的任务
     */
	public static void remove() {
        checkNotNull(sHandler);
		sHandler.removeCallbacksAndMessages(null);

	}

}
