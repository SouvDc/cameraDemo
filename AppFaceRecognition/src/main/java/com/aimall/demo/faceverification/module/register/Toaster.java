package com.aimall.demo.faceverification.module.register;


import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * @author ww
 * @date 2017.11.30
 * @des 支持上中下位置的toast
 */
public class Toaster {

	private Toaster() {}

	public static Toast toast = null;

	public static void showToast(Context context, String msg) {
		if (context == null) {
			return;
		}
		if (context instanceof Activity) {
			context = context.getApplicationContext();
		}
		if (toast == null) {
			toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
		} else {
			toast.setText(msg);
		}
		toast.show();
	}

	public static void showBottomToast(String msg) {

		if (toast == null) {
			toast = Toast.makeText(AppHelper.getContext(), msg, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.BOTTOM, 0, 100);
		} else {
			toast.setText(msg);
		}
		toast.show();
	}

	public static void showTopToast(String msg) {

		if (toast == null) {
			toast = Toast.makeText(AppHelper.getContext(), msg, Toast.LENGTH_SHORT);
		}
		toast.setGravity(Gravity.TOP, 0, 100);
		toast.setText(msg);

		toast.show();
	}
	public static void showCenterToast(String msg) {

		if (toast == null) {
			toast = Toast.makeText(AppHelper.getContext(), msg, Toast.LENGTH_LONG);
		}
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setText(msg);

		toast.show();
	}
}
