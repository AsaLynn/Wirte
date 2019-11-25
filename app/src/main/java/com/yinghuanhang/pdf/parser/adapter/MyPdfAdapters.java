package com.yinghuanhang.pdf.parser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yinghuanhang.pdf.parser.R;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
//你这个坑货

/**
 * Created by BC021 on 2018/1/6.
 */

public class MyPdfAdapters extends BaseAdapter {

    private Context mContext;
    private LayoutInflater inflater;
    private ImageView leftText = null;
    private ImageView rightText = null;

    private List<String> mImages;


    Toast toast;

    public MyPdfAdapters(Context context, List<String> dateimage) {
        this.mContext = context;
        this.mImages = dateimage;
        inflater = LayoutInflater.from(context);
    }

    String TAG = this.getClass().getSimpleName();

    @Override
    public int getCount() {
        return mImages == null ? 0 : ((int) Math.ceil((mImages.size() / 2.0)));
    }

    @Override
    public Object getItem(int position) {
        return mImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) { //这个方法都执行不了  你看下什么问题
        ViewGroup layout;
        if (convertView == null) {
            layout = (ViewGroup) inflater.inflate(R.layout.item_pdf, null);
        } else {
            layout = (ViewGroup) convertView;
        }
        setViewContentImage(layout, position);
        return layout;
    }


    private void setViewContentImage(ViewGroup group, int position) {
        leftText = (ImageView) group.findViewById(R.id.item_layout_leftpdfimage);
        File file = new File(mImages.get(position * 2));
        Glide.with(mContext)
                .load(file)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(leftText);
        rightText = (ImageView) group.findViewById(R.id.item_layout_rightpdfimage);
        if ((position * 2 + 1) < mImages.size()) {
            File files = new File(mImages.get(position * 2 + 1));
            Glide.with(mContext)
                    .load(files)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(rightText);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.end)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(rightText);
        }
    }


    //自定义Toast
    public void showMyToast(final Toast toast, final int cnt) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        }, 0, 100);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, cnt);
    }


}
