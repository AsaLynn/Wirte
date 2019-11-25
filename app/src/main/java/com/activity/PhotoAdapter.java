package com.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.yinghuanhang.pdf.parser.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PhotoAdapter extends BaseAdapter {

	private Context mContext;
	private int count=-1;
	private LayoutInflater inflater;
	private List<Bitmap> dateimage=new ArrayList<Bitmap>();

	private ImageView leftText=null;
	private ImageView rightText=null;

	Toast toast;

	public PhotoAdapter(Context context,List<Bitmap> dateimage) {
		mContext = context;
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.dateimage=dateimage;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		count = dateimage.size();
		if (count%2!=0){//如果不是整数
			Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.end);
			dateimage.add(bitmap);
		}
		return dateimage.size()/2;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return dateimage.get(position);
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
		if(convertView == null) {
			layout = (ViewGroup) inflater.inflate(R.layout.item_photo, null);
		} else {
			layout = (ViewGroup) convertView;
		}

		if (dateimage!=null) {
			setViewContentImage(layout, position);

		}

		return layout;
	}


	private void setViewContentImage(ViewGroup group, int position) {
		int p = (position+1)*2;
		if (p==(dateimage.size())){
			toast = Toast.makeText(mContext,"最后一页",Toast.LENGTH_SHORT);
			showMyToast(toast,10*100);
		}{
			leftText = (ImageView) group.findViewById(R.id.item_layout_leftimage);
			leftText.setImageBitmap(dateimage.get(position * 2));
			rightText = (ImageView) group.findViewById(R.id.item_layout_rightimage);
			rightText.setImageBitmap(dateimage.get(position * 2 + 1));
		}
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
