package com.yinghuanhang.pdf.parser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.yinghuanhang.pdf.parser.R;

import java.util.ArrayList;

public class MyPageWidgetAdapter extends BaseAdapter {

    private Context mContext;
    private int count;
    private LayoutInflater inflater;
    private ArrayList<String> data;
    private TextView leftText;
    private TextView rightText;
    Toast toast;

    public MyPageWidgetAdapter(Context context, ArrayList<String> data) {
        mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data = data;
    }

    @Override
    public int getCount() {
        count = (data == null ? 0 : (int) (data.size() / 2.0));
        return count;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewGroup layout;
        if (convertView == null) {
            layout = (ViewGroup) inflater.inflate(R.layout.item_tv_page, null);
        } else {
            layout = (ViewGroup) convertView;
        }
        setViewContent(layout, position);
        return layout;
    }

    /**
     * @param group
     * @param position
     */
    public void setViewContent(ViewGroup group, int position) {

        leftText = (TextView) group.findViewById(R.id.item_layout_leftText);
        leftText.setText(data.get(position * 2));

        rightText = (TextView) group.findViewById(R.id.item_layout_rightText);
        rightText.setText(data.get(position * 2 + 1));

        /*int p = (position + 1) * 2;
        if (p == (data.size())) {
            toast = Toast.makeText(mContext, "最后一页", Toast.LENGTH_SHORT);
            showMyToast(toast, 10 * 100);
        }*/
    }


    //自定义Toast
//    public void showMyToast(final Toast toast, final int cnt) {
//        final Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                toast.show();
//            }
//        }, 0, 100);
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                toast.cancel();
//                timer.cancel();
//            }
//        }, cnt);
//    }

}
