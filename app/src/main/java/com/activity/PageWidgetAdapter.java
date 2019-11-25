package com.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.yinghuanhang.pdf.parser.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PageWidgetAdapter extends BaseAdapter {

    private Context mContext;
    private int count = -1;
    private LayoutInflater inflater;
    private ArrayList<String> data = new ArrayList<>();

    private TextView leftText = null;
    private TextView rightText = null;

    Toast toast;

    public PageWidgetAdapter(Context context, ArrayList<String> data) {
        mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data = data;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        count = data.size();
        if (count % 2 != 0) {//如果不是整数
            data.add("完");
        }
        return data.size() / 2;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewGroup layout;
        if (convertView == null) {
            layout = (ViewGroup) inflater.inflate(R.layout.item_layout, null);
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

        int p = (position + 1) * 2;
        if (p == (data.size())) {
            toast = Toast.makeText(mContext, "最后一页", Toast.LENGTH_SHORT);
            showMyToast(toast, 10 * 100);
        }
        {
            leftText = (TextView) group.findViewById(R.id.item_layout_leftText);
            leftText.setText(data.get(position));
            rightText = (TextView) group.findViewById(R.id.item_layout_rightText);
            rightText.setText(data.get(position + 1));
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
