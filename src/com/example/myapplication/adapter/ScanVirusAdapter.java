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
	private MyClickListener mListener;   //����listview��item������ͬһ��listener���󣡣���
	
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
		
		//Ԥ������
		ViewHolder holder;
		
		//��Դ�ĳ�ʼ��
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
		
		//���ڶ�Ӧ����ȡһ��Ӧ��
		ScanInfo scanInfo = mVirusScanInfoList.get(position);
		
		if(!scanInfo.isVirus) {
			holder.mNameTV.setText(scanInfo.name);
			holder.mNameTV.setTextColor(context.getResources().getColor(R.color.bright_gray));
			holder.mBut.setVisibility(View.INVISIBLE);
			
		} else {
			holder.mNameTV.setText(scanInfo.name);
			holder.mNameTV.setTextColor(context.getResources().getColor(R.color.bright_red));
			holder.mBut.setVisibility(View.VISIBLE);
			
			//����ж�ذ�ť���¼�(����)
			holder.mBut.setOnClickListener(mListener);
			holder.mBut.setTag(position);  //������ʹ����listview��item������ͬһ��listener��������Ϊÿ��item�����ø��Ե�listener������

		}
		//����Ӧ��ͼ��
		holder.mIconImgv.setImageDrawable(scanInfo.appicon);
		
		return convertView;
	}

	static class ViewHolder{
		ImageView mIconImgv;
		TextView mNameTV;
		Button mBut;
	}
	
	/**
	 * ���ڻص��ĳ�����
	 */
	public static abstract class MyClickListener implements OnClickListener {
		//�����onClick����
		public void onClick(View v) {
			myOnClick((Integer)v.getTag(), v);
		}
		public abstract void myOnClick(int position, View v);
	}
	
}

