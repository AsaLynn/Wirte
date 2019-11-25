package com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yinghuanhang.pdf.parser.R;

import java.io.File;
import java.util.List;

/**
 * @author Jenly <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * @since 2017/1/23
 */
public class GirlDetailAdapter extends BasePagerAdapter<File> {


    private LayoutInflater layoutInflater;

    public GirlDetailAdapter(Context context, List<File> listData) {
        super(context,listData);
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(ViewGroup container, final File girl, int position) {
        View view = layoutInflater.inflate(R.layout.list_girl_detail_item,container,false);
        final PhotoView photoView = (PhotoView)view.findViewById(R.id.photoView);
        photoView.enable();
        Glide.with(context).load(girl).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(photoView);

        return view;
    }


}
