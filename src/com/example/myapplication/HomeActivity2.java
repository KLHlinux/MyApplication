package com.example.myapplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.example.myapplication.R;
import com.example.myapplication.engine.VirusDao;
import com.example.myapplication.mytoolbar.MyToolbar;
import com.example.myapplication.util.Md5Util;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class HomeActivity2 extends AppCompatActivity  {

	protected static final String tag = "HomeActivity";
	protected static final int SCANING = 100;
	protected static final int FINISH = 101;
	
	private MyToolbar myToolbar;
	private ImageView iv_scan;
	private LinearLayout ll_add_text;
	private TextView tv_lastTime;
	private Button bt_scan;
	private Button bt_process;
	private boolean flag;
	private boolean isStop;
	private RotateAnimation rotateAnimation;
	private SharedPreferences mSP;
	protected ArrayList<ScanInfo> mVirusScanInfoList;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SCANING:
				//ɨ�����
				ScanInfo info = (ScanInfo) msg.obj;
				//�����Բ��������һ������ɨ��Ӧ�õ�TextView
				TextView textView = new TextView(getApplicationContext());
				if(info.isVirus) {
					//�ǲ���
					textView.setTextColor(Color.RED);
					textView.setText("���ֲ�����" + info.name);
				}else {
					//���ǲ���
					textView.setTextColor(Color.BLACK);
					textView.setText("ɨ�谲ȫ��" + info.name);
				}
				ll_add_text.addView(textView, 0);
				break;
			case FINISH:
				//��ť�仯
				bt_process.setText("�� ��");
				bt_process.setBackgroundResource(R.drawable.green_border2);
				//ֹͣ����ִ�е���ת����//ͼƬ����
				iv_scan.setVisibility(View.INVISIBLE);
				iv_scan.clearAnimation();
				//��֪�û�ж�ذ����˲�����Ӧ��
				unInstallVirus();
				//����ɱ��ʱ��
				saveScanTime();
				flag=false;
				break;
			}
		}
		
		/**
		 * ����ɱ��ʱ��
		 */
		private void saveScanTime() {
			Editor edit = mSP.edit();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					Locale.getDefault());
			String currentTime = sdf.format(new Date());
			currentTime = "�ϴβ�ɱ��" + currentTime;
			edit.putString("lastVirusScan", currentTime);
			edit.commit();
		}
		
	};


	@Override
    protected void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        //״̬��͸��
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();            
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);            
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        } 
        
        mSP = getSharedPreferences("config", MODE_PRIVATE);
        
        //��ʼ��������
        initToolbar();
        //��ʼ��ͼ�����
        initUI();
        //��ʼ����������
        initAnimation();
        
    }
	
	@Override
	protected void onResume() {
		String string = mSP.getString("lastVirusScan", "����û�в�ɱ������");
		tv_lastTime.setText(string);
		super.onResume();
	}
	
	/**
	 * ��֪�û�ж�ذ����˲�����Ӧ��
	 */
	protected void unInstallVirus() {
		for(ScanInfo scanInfo : mVirusScanInfoList) {
			String packageName = scanInfo.packName;
			//Դ��
			//Intent intent = new Intent("android:intent.action.DELETE");
			//intent.addCategory("android.intent.category.DEFAULT");
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_DELETE);
			intent.setData(Uri.parse("package:"+packageName));
			startActivity(intent);
		}
	}

	/**
	 * �����߳�����鲡��
	 */
	private void checkVirus() {
		flag=true;
		isStop=false;
		new Thread() {
			public void run() {
				//��ȡ���ݿ������еĲ�����md5��
				List<String> virusList = VirusDao.getVirusList();
				//��ȡ�ֻ����������Ӧ�ó���ǩ���ļ���md5��
				//1.��ȡ�������߶���
				PackageManager pm = getPackageManager();
				//2.��ȡ����Ӧ�ó���ǩ���ļ�()
				List<PackageInfo> packageInfoList = pm.getInstalledPackages(
						PackageManager.GET_SIGNATURES + PackageManager.GET_UNINSTALLED_PACKAGES);
				//������¼�����ļ���
				mVirusScanInfoList = new ArrayList<ScanInfo>();
				//��¼����Ӧ�õļ���
				List<ScanInfo> scanInfoList = new ArrayList<ScanInfo>();
				//3.����Ӧ�ü���
				for(PackageInfo packageInfo : packageInfoList) {
					if(!flag) {
						isStop=true;
						return;
					}
					//����javaBean����(���ڼ�¼)
					ScanInfo scanInfo = new ScanInfo();
					//��ȡǩ���ļ�������
					Signature[] signatures = packageInfo.signatures;
					//��ȡǩ���ļ�����ĵ�һλ��Ȼ��ǩ���ļ�תmd5������md5�����ݿ��е�md5�ȶ�
					Signature signature = signatures[0];
					String string = signature.toCharsString();
					//32λ�ַ�����16�����ַ�(0-f)
					String encoder = Md5Util.encoder(string);
					
					//Log.i(tag, encoder);
					
					//4.�ȶ�Ӧ���Ƿ�Ϊ����
					if(virusList.contains(encoder)) {
						//5.��¼����
						scanInfo.isVirus = true;
						mVirusScanInfoList.add(scanInfo);//�ǲ���ʱ�����뼯����ȥ
					}else {
						scanInfo.isVirus = false;
					}
					//6.ά������İ������Լ�Ӧ������
					scanInfo.packName = packageInfo.packageName;
					scanInfo.name = packageInfo.applicationInfo.loadLabel(pm).toString();
					scanInfoList.add(scanInfo);
					
					//˯��һ��
					try {
						Thread.sleep(50+ new Random().nextInt(100));
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					
					//�����߳��з�����Ϣ����֪���̸߳���UI(ɨ������������Բ��������View)
					Message msg = Message.obtain();
					msg.what = SCANING;
					msg.obj = scanInfo;
					mHandler.sendMessage(msg);
				}
				//ɨ�����
				Message msg = Message.obtain();
				msg.what = FINISH;
				mHandler.sendMessage(msg);
			};
		}.start();
		
	}
	
	/**
	 * javaBean���ڴ洢�������������
	 *
	 */
	class ScanInfo{
		public boolean isVirus;
		public String packName;
		public String name;
	}
	
	/**
	 * ��ʼ������(����)
	 */
	private void initAnimation() {
		rotateAnimation = new RotateAnimation(
				0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f) ;
		rotateAnimation.setDuration(1000);
		//ָ������һֱ��ת
		rotateAnimation.setRepeatCount(RotateAnimation.INFINITE);
		//���ֶ���ִ�н������״̬
		rotateAnimation.setFillAfter(true);
		
	}

	/**
	 * ��ʼ����ͼ����
	 */
	private void initUI() {
		iv_scan = (ImageView) findViewById(R.id.iv_scan);
		ll_add_text = (LinearLayout) findViewById(R.id.ll_add_text);
		bt_scan = (Button) findViewById(R.id.bt_scan);
		bt_process = (Button) findViewById(R.id.bt_process);
		tv_lastTime = (TextView) findViewById(R.id.tv_lastTime);
	}

	/**
	 * ���ٲ�ɱ�İ�ť�¼�
	 */
	public void scan(View v){  
		//Toast.makeText(HomeActivity.this, "scanning", 0).show();
		//��ʾ����ͼƬ
		iv_scan.setVisibility(View.VISIBLE);
		//��鲡��
		checkVirus();
		//ִ�ж���
		iv_scan.startAnimation(rotateAnimation);
		//����仯
		tv_lastTime.setVisibility(View.INVISIBLE);
		bt_scan.setVisibility(View.INVISIBLE);
		bt_process.setVisibility(View.VISIBLE);
	} 
	
	/**
	 * ɱ�����̵İ�ť�¼�
	 * @param v
	 */
	public void finish(View v){  
		//����ɨ���
		if(flag==true) { 
			flag=false;
			//ֹͣ����ִ�е���ת����//ͼƬ����
			iv_scan.clearAnimation();
			iv_scan.setVisibility(View.INVISIBLE);
		}
		//ɨ����ɵ�
		else {
			
			onResume();
			bt_process.setText("ȡ �� ɨ ��");
			bt_process.setBackgroundResource(R.drawable.check_border1);
		}
		
		//����ı仯
		tv_lastTime.setVisibility(View.VISIBLE);
		bt_scan.setVisibility(View.VISIBLE);
		bt_process.setVisibility(View.GONE);
	} 
	
	/**
	 * ��ʼ��������
	 */
	private void initToolbar() {
		myToolbar = (MyToolbar) findViewById(R.id.toolbar_main);
		myToolbar.setLeftText("�����ֻ�ɱ��");
        myToolbar.setToolbarTitle("");
        myToolbar.setRightImg(R.drawable.shezhi1);
        myToolbar.setOnLeftTextClickListener(new MyToolbar.OnLeftTextClickListener() {
            @Override
            public void onLeftTextClick() {
            	//��Ӧ����ı�����¼�
                Toast.makeText(HomeActivity2.this, "Left", 0).show();
            }
        });

        myToolbar.setOnRightTextClickListener(new MyToolbar.OnRightTextClickListener() {
            @Override
            public void onRightTextClick() {
                //��Ӧ�Ҳ��ı�����¼�
            	//Toast.makeText(HomeActivity.this, "Right", 0).show();
            	Intent intent = new Intent(HomeActivity2.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        
        setSupportActionBar(myToolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
	}

    @Override
    protected void onDestroy() {
    	flag=false;
    	super.onDestroy();
    }

}
