package com.example.myapplication.adapter;

import java.util.List;

import com.example.myapplication.entity.ScanInfo;
import com.example.myapplication.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ScanVirusAdapter extends BaseAdapter {

	private List<ScanInfo> mVirusScanInfoList;
	private Context context;
	private MyClickListener mListener;   //所有listview的item都共用同一个listener对象！！！
	
	public ScanVirusAdapter(List<ScanInfo> virusScanInfoList,Context context, MyClickListener listener) {
		super();
		mVirusScanInfoList = virusScanInfoList;
		this.context = context;
		mListener = listener;

	}
	
	@Override
	public int getCount() {
		return mVirusScanInfoList.size();
	}

	@Override
	public Object getItem(int position) {
		return mVirusScanInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		//预留对象
		ViewHolder holder;
		
		//资源的初始化
		if(convertView == null) {
			
			convertView = View.inflate(context,R.layout.item_list_applock, null);
			
			holder = new ViewHolder();
			holder.mIconImgv = (ImageView) convertView.findViewById(R.id.imgv_appicon);
			holder.mNameTV = (TextView) convertView.findViewById(R.id.tv_appname);
			holder.mBut = (Button) convertView.findViewById(R.id.but_appbut);
			
			convertView.setTag(holder);
		
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		//从众多应用中取一个应用
		ScanInfo scanInfo = mVirusScanInfoList.get(position);
		
		if(!scanInfo.isVirus) {
			holder.mNameTV.setText(scanInfo.name);
			holder.mNameTV.setTextColor(context.getResources().getColor(R.color.bright_gray));
			holder.mBut.setVisibility(View.INVISIBLE);
			
		} else {
			holder.mNameTV.setText(scanInfo.name);
			holder.mNameTV.setTextColor(context.getResources().getColor(R.color.bright_red));
			holder.mBut.setVisibility(View.VISIBLE);
			
			//设置卸载按钮的事件(病毒)
			holder.mBut.setOnClickListener(mListener);
			holder.mBut.setTag(position);  //这样能使所有listview的item都共用同一个listener，而不用为每个item都设置各自的listener！！！

		}
		//设置应用图标
		holder.mIconImgv.setImageDrawable(scanInfo.appicon);
		
		return convertView;
	}

	static class ViewHolder{
		ImageView mIconImgv;
		TextView mNameTV;
		Button mBut;
	}
	
	/**
	 * 用于回调的抽象类
	 */
	public static abstract class MyClickListener implements OnClickListener {
		//基类的onClick方法
		public void onClick(View v) {
			myOnClick((Integer)v.getTag(), v);
		}
		public abstract void myOnClick(int position, View v);
	}
	
}

