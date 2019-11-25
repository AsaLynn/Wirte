package com.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yinghuanhang.pdf.parser.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
//你这个坑货
/**
 * Created by BC021 on 2018/1/6.
 */

public class PdfAdapters extends BaseAdapter{

    private Context mContext;
    private int count=-1;
    private LayoutInflater inflater;

    private ImageView leftText=null;
    private ImageView rightText=null;

    private List<String> mImages = new ArrayList<>();


    Toast toast;

    public PdfAdapters(Context context,List<String> dateimage) {
        this.mContext=context;
        this.mImages=dateimage;
        inflater=LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        count = mImages.size();
//        if (count%2!=0){//如果不是整数
//            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.end);
//            dateimage.add(bitmap);
//        }
        return mImages.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mImages.get(position);


    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) { //这个方法都执行不了  你看下什么问题
        // TODO Auto-generated method stub
        View layout;
        if(convertView == null) {
            layout =inflater.inflate(R.layout.item_pdf, null);
        } else {
            layout =convertView;
        }

        if (mImages!=null) {
            setViewContentImage(layout, position);

            int currentPosition = position * 2;
            if (currentPosition >= mImages.size()){
                leftText.setImageResource(R.drawable.end);
            }
            else {
                File file = new File(mImages.get(position));
                Glide.with(mContext)
                        .load(file)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(leftText);
            }

            if (currentPosition + 1 >= mImages.size()) {
                rightText.setImageResource(R.drawable.end);
                toast = Toast.makeText(mContext,"最后一页",Toast.LENGTH_SHORT);
                showMyToast(toast,10*10);
            }
            else {
                File files = new File(mImages.get(position + 1));
                Glide.with(mContext)
                        .load(files)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(rightText);
            }
        }

        return layout;
    }


    private void setViewContentImage(View group, int position) {

        leftText = (ImageView) group.findViewById(R.id.item_layout_leftpdfimage);
        rightText = (ImageView) group.findViewById(R.id.item_layout_rightpdfimage);

    }



    //自定义Toast
    public void showMyToast(final Toast toast, final int cnt) {
        final Timer timer =new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        },0,100);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, cnt );
    }


}
