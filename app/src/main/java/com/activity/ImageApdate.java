package com.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.yinghuanhang.pdf.parser.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 26917 on 2017/12/25.
 */
public class ImageApdate extends BaseAdapter{
    private Context mContext;
    private List<Bitmap> dateimage=new ArrayList<Bitmap>();

    public ImageApdate(Context context, List<Bitmap> datename){
        this.mContext = context;
        this.dateimage = datename;
    }


    @Override
    public int getCount() {
        return dateimage.size();
    }

    @Override
    public Object getItem(int position) {
        return dateimage.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gridview_image, null);
        }
        ImageView imageView= (ImageView) convertView.findViewById(R.id.iv_image);
        imageView.setImageBitmap(dateimage.get(position));
        return convertView;
    }
}
