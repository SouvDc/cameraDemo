package jerome.com.usbcamera;

/*
 *  @项目名：  demo 
 *  @包名：    jerome.com.usbcamera
 *  @文件名:   ICameraErrorListener
 *  @创建者:   Administrator
 *  @创建时间:  2018/8/30 9:41
 *  @描述：    针对jni加载usb摄像头的错误回调，交给上层处理
 */

public interface ICameraErrorListener {


    /**
     *
     * @param code 0：成功 -1：无法识别 -2：类型错误/非摄像头  -3：打开失败
     * @param msg
     */
    void onUsbCameraError(int code,String msg);


}
