package com.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.yinghuanhang.pdf.parser.R;

import java.io.File;
import java.util.List;

/**
 * @author Jenly <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * @since 2017/1/20
 *
 */
public class EsayGirlAdapter extends RecyclerArrayAdapter<File> {


    private OnClickHolderItemListener onClickHolderItemListener;
    private String TAG = this.getClass().getSimpleName();

    public EsayGirlAdapter(Context context, List<File> objects) {
        super(context, objects);
    }

    public void setListData(List<File> objects) {
        this.mObjects = objects;
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {

        GirlViewHolder girlViewHolder = new GirlViewHolder(parent, R.layout.list_girls_item);
        return girlViewHolder;
    }

    @Override
    public void OnBindViewHolder(final BaseViewHolder holder, final int position) {
        super.OnBindViewHolder(holder, position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickHolderItemListener != null) {
                    onClickHolderItemListener.onItemClick(holder, position);
                }
            }
        });
    }

    public interface OnClickHolderItemListener {
        public void onItemClick(BaseViewHolder holder, int position);
    }


    public void setOnClickHolderItemListener(OnClickHolderItemListener onClickHolderItemListener) {
        this.onClickHolderItemListener = onClickHolderItemListener;
    }

    public class GirlViewHolder extends BaseViewHolder<File> {

        private ImageView iv;


        public GirlViewHolder(ViewGroup parent, @LayoutRes int res) {
            super(parent, res);
            iv = $(R.id.ivImage);
        }

        @Override
        public void setData(File data) {
            super.setData(data);
            Log.i(TAG, "---->setData: ");
            Glide
                    .with(getContext())
                    .load(data)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(iv);
        }
    }
}
