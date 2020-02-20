package com.aimall.demo.faceverification.module.manage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aimall.demo.faceverification.R;
import com.cnbot.facelib.data.PhotoBean;

import java.util.List;

/**
 * Created by Administrator on 2017/1/22.
 */

public class PhotoManageAdapter extends BaseAdapter {
    private Context context;

    public void setList(List<PhotoBean> list) {
        this.list = list;
    }

    private List<PhotoBean> list;

    public PhotoManageAdapter(Context context, List<PhotoBean> list) {
        this.context = context;
        this.list = list;

    }

    @Override
    public int getCount() {
        if (list != null)
            return list.size();
        else
            return 0;
    }

    @Override
    public Object getItem(int position) {
        if (list != null)
            return list.get(position);
        else return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_photo_manage, null);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.photo);
        TextView name_text = (TextView) convertView.findViewById(R.id.name);

        if (list != null && list.size() > 0) {
            PhotoBean bean = list.get(position);
            if (bean!=null){
                Bitmap bitmap = null;
                if(bean.bitmap == null){
                    bitmap = BitmapFactory.decodeFile(bean.path);
                }else {
                    bitmap = bean.bitmap.bitmap;
                }
                imageView.setImageBitmap(bitmap);
                name_text.setText(bean.name);
            }
           /* if (bean != null && bean.bitmap != null && bean.bitmap.bitmap != null && bean.name != null) {
                imageView.setImageBitmap(bean.bitmap.bitmap);
                name_text.setText(bean.name);

            }*/
        }

        return convertView;


    }


}
