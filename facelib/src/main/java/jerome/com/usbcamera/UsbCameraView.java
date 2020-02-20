package jerome.com.usbcamera;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cnbot.facelib.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/*
 *  @项目名：  demo
 *  @包名：    jerome.com.usbcamera
 *  @文件名:   faceLib
 *  @创建者:   ww
 *  @创建时间:  2018/8/30
 *  @描述：    通过jni实现USB摄像头预览,预览尺寸只支持640*480   1280*720
 */
public class UsbCameraView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

	private static final String TAG = "UsbCameraView";
	protected Context mContext;
	private SurfaceHolder mSurfaceHolder;
	private boolean isCameraOpenSuccess = false;
	private ImageProc mUsbCameraNative = new ImageProc();

	private int mPreviewWidth;
	private int mPreviewHeight;
	private ICameraErrorListener mErrorListener;
	private int mPixelFormat;
	private int mLeft;
	private int mTop;
	/**
	 * 摄像头节点
	 * 比如：/dev/video0中的0
	 */
	private int mCameraNode;
	public UsbCameraView(Context context) {
		this(context, null);
	}

	public UsbCameraView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.mContext = context;

		initDefault();
		initAttrs(context, attrs);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

		mLeft = getLeft();
		mTop = getTop();

	}

	/**
	 * 初始化默认值
	 */
	private void initDefault() {
		setFocusable(true);

	}

	private void initAttrs(Context context, AttributeSet attrs) {

		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.USBCameraView);
		mPreviewWidth = (int) typedArray.getDimension(R.styleable.USBCameraView_preview_width, dip2Px(ImageProc.IMG_WIDTH_1280));
		mPreviewHeight = (int) typedArray.getDimension(R.styleable.USBCameraView_preview_height, dip2Px(ImageProc.IMG_HEIGHT_720));
		mPixelFormat = typedArray.getInt(R.styleable.USBCameraView_pixel_format, ImageProc.CAMERA_PIX_FMT_MJPEG);
		typedArray.recycle();

	}

	public void setPreviewSize_640_480() {
		mPreviewWidth = dip2Px(ImageProc.IMG_WIDTH_640);
		mPreviewHeight = dip2Px(ImageProc.IMG_HEIGHT_480);
	}

	public void setPreviewSize_1280_720() {
		mPreviewWidth = dip2Px(ImageProc.IMG_WIDTH_1280);
		mPreviewHeight = dip2Px(ImageProc.IMG_HEIGHT_720);
	}

	/**
	 * 开始摄像头预览
	 */
	public void startPreview() {
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);

	}

	/**
	 * 开始摄像头预览
	 */
	public void startPreview(int node,ICameraErrorListener l) {
		mCameraNode = node;
		startPreview(l);

	}



	/**
	 * 开始摄像头预览
	 */
	public void startPreview(ICameraErrorListener l) {
		addCameraErrorListener(l);
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);

	}

	@Override
	public void run() {

		preview();

	}

	private void preview() {
		Rect rect = new Rect(mLeft, mTop, mLeft + mPreviewWidth, mTop + mPreviewHeight);
		//只支持ARGB_8888
		Bitmap bitmap = Bitmap.createBitmap(mPreviewWidth, mPreviewHeight, Bitmap.Config.ARGB_8888);
		//小于0.2s延迟
		while (isCameraReady()) {
			//			long start = System.currentTimeMillis();
			mUsbCameraNative.nativeProcessCamera();
			mUsbCameraNative.nativePixelToBmp(bitmap);

			Canvas canvas = getHolder().lockCanvas();
			if (canvas != null) {
				canvas.drawBitmap(bitmap, null, rect, null);
				getHolder().unlockCanvasAndPost(canvas);
			}
			//			long end = System.currentTimeMillis();
			//			Log.e(TAG, "start-end:" + ((end - start) * 1.0 / 1000));

		}
		bitmap.recycle();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		Log.d(TAG, "surfaceCreated");

		int ret = mUsbCameraNative.nativePrepareCamera(mPreviewWidth, mPreviewHeight, mPixelFormat);
		if (ret == 0) {
			isCameraOpenSuccess = true;

			new Thread(this).start();
		}
		if (mErrorListener != null)
			mErrorListener.onUsbCameraError(ret, "");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "surfaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		isCameraOpenSuccess = false;
		mUsbCameraNative.nativeStopCamera();
		Log.d(TAG, "surfaceDestroyed");

	}

	/**
	 * 初始化摄像头
	 * @return
	 */
	public int prepareCamera() {

		return mUsbCameraNative.nativePrepareCamera(mPreviewWidth, mPreviewHeight, mPixelFormat);
	}

	public int prepareCamera(int previewWidth, int previewHeight) {
		mPreviewWidth = previewWidth;
		mPreviewHeight = previewHeight;
		return mUsbCameraNative.nativePrepareCamera(mPreviewWidth, mPreviewHeight, mPixelFormat);
	}

	/**
	 * 停止摄像头
	 * @return
	 */
	public int stopCamera() {
		return mUsbCameraNative.nativeStopCamera();
	}

	/**
	 * 处理摄像头数据
	 *  与pixelToBmp(Bitmap bitmap)一起配合使用将数据转换为bitmap
	 *  @see #pixelToBmp(Bitmap)
	 * @return
	 */
	public int processCamera() {

		return mUsbCameraNative.nativeProcessCamera();
	}

	/**
	 *	将数据转换为bitmap
	 * @param bitmap
	 */
	public void pixelToBmp(Bitmap bitmap) {

		mUsbCameraNative.nativePixelToBmp(bitmap);
	}

	public boolean isCameraReady() {
		return isCameraOpenSuccess;
	}

	public void addCameraErrorListener(ICameraErrorListener l) {
		mErrorListener = l;
	}

	private void savePictureFile(Bitmap bitmap, String fileName) {

		String defaultFolder = FileUtils.getPhotoStorageDir();

		File f = new File(defaultFolder + fileName);

		FileOutputStream fOut = null;

		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected int dip2Px(float dip) {
		float scale = getResources().getDisplayMetrics().density;
		return (int) (scale * dip + 0.5F);
	}

}
