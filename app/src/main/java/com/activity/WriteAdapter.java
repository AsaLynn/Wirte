package com.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bean.PagerItemInfo;
import com.yinghuanhang.pdf.parser.R;

import java.util.ArrayList;

public class WriteAdapter extends BaseAdapter {

    private Context mContext;
    //	private int count=-1;
    private LayoutInflater inflater;
    /* private ArrayList<String> data;*/
    private ArrayList<PagerItemInfo> data;

    PaletteView palt;

    /*public WriteAdapter(Context context, ArrayList<String> data) {
        mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data = data;
    }*/

    public WriteAdapter(Context context, ArrayList<PagerItemInfo> data) {
        mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /*@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewGroup layout;
        if (convertView == null) {
            layout = (ViewGroup) inflater.inflate(R.layout.activity_write, null);
        } else {
            layout = (ViewGroup) convertView;
        }

        setViewContent(layout, position);

        return layout;
    }*/

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        setViewContent(data.get(position).getView(), position);
        return data.get(position).getView();
    }

    /**
     * @param group
     * @param position
     */
    private void setViewContent(View group, int position) {
        palt = (PaletteView) group.findViewById(R.id.palette);
        palt.setPath(data.get(position).getPath());
        if (position % 3 == 0) {
            palt.setBackgroundResource(R.drawable.x1);
        } else if (position % 3 == 1) {
            palt.setBackgroundResource(R.drawable.x2);
        } else if (position % 3 == 2) {
            palt.setBackgroundResource(R.drawable.x3);
        }




    }

}
