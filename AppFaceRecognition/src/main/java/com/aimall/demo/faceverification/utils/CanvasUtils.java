package com.aimall.demo.faceverification.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.aimall.easylib.utils.ToastUtils;
import com.aimall.multifacetrackerlib.bean.FaceInfo;
import com.cnbot.facelib.data.MatchResultBean;

import java.util.List;

/**
 * Created by zhangchao on 18-1-30.
 */

public class CanvasUtils {

    public static Bitmap drawFaceInfoOnBitmap(Bitmap bmp, List<FaceInfo> faceInfos) {
        Bitmap drawBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(drawBitmap);
        canvas.drawBitmap(bmp, 0, 0, null);

        Paint paint = new Paint();
        paint.setStrokeWidth(10);
        int topPading = 30;
        for (FaceInfo faceInfo : faceInfos) {
            paint.setColor(Color.GRAY);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(faceInfo.getRect(), paint);
            paint.setColor(Color.parseColor("#66ff0000"));
            paint.setStyle(Paint.Style.FILL);
            float left = faceInfo.getRect().left;
            float right = faceInfo.getRect().right;
            float top = faceInfo.getRect().top - 3 * topPading;
            float bottom = faceInfo.getRect().top;
            canvas.drawRect(left, top, right, bottom, paint);

            float[] points = faceInfo.getPoints();
            if(points != null) {
                paint.setColor(Color.parseColor("#ff0000"));
                for(int j = 0; j < points.length / 2; j++) {
                    canvas.drawCircle(points[j * 2], points[j * 2 + 1], 5, paint);
                }
            }

            paint.setColor(Color.WHITE);
            paint.setTextSize(30);
            Object tag = faceInfo.getTag();
            if(tag != null) {
                String          content         = "未知";
                MatchResultBean matchResultBean = (MatchResultBean)tag;
                if(matchResultBean.score > Constants.stardandScore) {
                    content = "名字：" + matchResultBean.photoBean.name;
                }
                ToastUtils.showShort("score:"+matchResultBean.score);
                canvas.drawText(content, left, faceInfo.getRect().top - topPading, paint);
            }
        }


        return drawBitmap;
    }

}
