package com.cnbot.facelib.handler;
import android.graphics.Bitmap;

/*
 *  @项目名：  demo
 *  @包名：    com.cnbot.facelib.handler
 *  @文件名:   IBitmapHandlerListener
 *  @创建者:   Administrator
 *  @创建时间:  2018/9/10 9:47
 *  @描述：    BaseBitmapHandler处理预览数据后的回调
 */

public interface IBitmapHandlerListener {

    /**
     *
     * @param bitmap  Camera预览数据生成的图片
     */
    void onHandlerResult(Bitmap bitmap);



}
