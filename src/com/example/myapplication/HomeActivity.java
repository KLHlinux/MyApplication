package com.example.myapplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.example.myapplication.R;
import com.example.myapplication.adapter.ScanVirusAdapter;
import com.example.myapplication.adapter.ScanVirusAdapter.MyClickListener;
import com.example.myapplication.engine.VirusDao;
import com.example.myapplication.entity.ScanInfo;
import com.example.myapplication.util.Md5Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class HomeActivity extends AppCompatActivity  {

	protected static final String tag = "HomeActivity";
	protected static final int SCANING = 100;
	protected static final int FINISH = 101;
	
	public Toolbar mToolbar;
	private ImageView iv_scan;
//	private LinearLayout ll_add_text;
	private TextView tv_lastTime;
	private TextView tv_package;
	private Button bt_scan;
	private Button bt_process;
	private boolean flag;
	private boolean isStop;
	private int index = 0;
	private ProgressBar pb_bar;
	private RotateAnimation rotateAnimation;
	private SharedPreferences mSP;
	private ScanVirusAdapter adapter;
	private ListView mScanListView;
	private List<ScanInfo> mScanInfoList = new ArrayList<ScanInfo>();
	protected List<ScanInfo> mVirusScanInfoList = new ArrayList<ScanInfo>();;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SCANING:
				//ɨ�����
				ScanInfo info = (ScanInfo) msg.obj;
				//�ı�"����ɨ�����"
				tv_package.setText("����ɨ�裺" + info.name);
							
				//ɨ�����б�
				mScanInfoList.add(info);
				adapter.notifyDataSetChanged();
				mScanListView.setSelection(mScanInfoList.size());
				
				break;
				
			case FINISH:
				//��ť�仯
				bt_process.setText("�� ��");
				bt_process.setBackgroundResource(R.drawable.green_border2);
				
				//�ı�"����ɨ�����"
				if(!mVirusScanInfoList.isEmpty()) {
					tv_package.setText("�� �� �� ��");
				}else {
					tv_package.setText("�� �� �� �� ȫ");
				}
				
				//ֹͣ����ִ�е���ת����//ͼƬ����
				iv_scan.setVisibility(View.INVISIBLE);
				iv_scan.clearAnimation();
				
				pb_bar.setVisibility(View.INVISIBLE);
				
				//��֪�û�ж�ذ����˲�����Ӧ��
				//unInstallVirus();
				
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
			currentTime = "�ϴβ�ɱʱ�䣺" + currentTime;
			edit.putString("lastVirusScan", currentTime);
			edit.commit();
		}
		
	};

	/**
	 * ��������
	 */
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
	
	/**
	 * ��������
	 */
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
		//ɨ��ǰ���֮ǰ��ɨ���¼
		mScanInfoList.clear();
		mVirusScanInfoList.clear();
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
//				mVirusScanInfoList = new ArrayList<ScanInfo>();
//				//��¼����Ӧ�õļ���
//				List<ScanInfo> scanInfoList = new ArrayList<ScanInfo>();
				
				//���ý����������ֵ
				pb_bar.setMax(packageInfoList.size());
				
				//3.����Ӧ�ü���
				for(PackageInfo packageInfo : packageInfoList) {
					if(!flag) {
						isStop=true;
						return;
					}
//					//����javaBean����(���ڼ�¼)
//					ScanInfo scanInfo = new ScanInfo();
					//��ȡǩ���ļ�������
					Signature[] signatures = packageInfo.signatures;
					//��ȡǩ���ļ�����ĵ�һλ��Ȼ��ǩ���ļ�תmd5������md5�����ݿ��е�md5�ȶ�
					Signature signature = signatures[0];
					String string = signature.toCharsString();
					//32λ�ַ�����16�����ַ�(0-f)
					String encoder = Md5Util.encoder(string);
					
					//Ӧ�õ�md5��
//					Log.i(tag, encoder);
					
					//һ��Ӧ��
					ScanInfo scanInfo = new ScanInfo();
					
					//4.�ȶ�Ӧ���Ƿ�Ϊ����
					if(virusList.contains(encoder)) {
						//5.��¼����
						scanInfo.isVirus = true;
						mVirusScanInfoList.add(scanInfo);//�ǲ���ʱ�����뼯����ȥ
					}else {
						scanInfo.isVirus = false;
					}
					//6.ά������İ������Լ�Ӧ�����ƣ���Ӧ��ͼ��
					scanInfo.packName = packageInfo.packageName;
					scanInfo.name = packageInfo.applicationInfo.loadLabel(pm).toString();
					scanInfo.appicon = packageInfo.applicationInfo.loadIcon(pm);
//					scanInfoList.add(scanInfo);
					
					//7.��ɨ��Ĺ����У���Ҫ���½�����
					index++;
					pb_bar.setProgress(index);
					
					//˯��һ��
					try {
						Thread.sleep(50+ new Random().nextInt(100));
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					//8.�����߳��з�����Ϣ����֪���̸߳���UI(ɨ������������Բ��������View)
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
//		ll_add_text = (LinearLayout) findViewById(R.id.ll_add_text);
		bt_scan = (Button) findViewById(R.id.bt_scan);
		bt_process = (Button) findViewById(R.id.bt_process);
		tv_lastTime = (TextView) findViewById(R.id.tv_lastTime);
		tv_package = (TextView) findViewById(R.id.tv_package);
		pb_bar = (ProgressBar) findViewById(R.id.pb_bar);
		
		//ɨ���Ӧ���б�
		mScanListView = (ListView) findViewById(R.id.lv_scanapps);
		adapter = new ScanVirusAdapter(mScanInfoList, this,mListener);
		mScanListView.setAdapter(adapter);
		
	}
	
	/**
	 * ж�ذ�ť�¼�
	 */
	private MyClickListener mListener = new MyClickListener() {
		 public void myOnClick(int position, View v) {
//			 Toast.makeText( HomeActivity.this,
//					 	"listview���ڲ��İ�ť������ˣ���λ����-->" + position 
//			 		+ 	",������-->"+ mScanInfoList.get(position), 
//			 			Toast.LENGTH_SHORT).show();
		 	
			 String packageName = mScanInfoList.get(position).packName;
			 
			 //����ϵͳ��ж�ع���
			 Intent intent = new Intent();
			 intent.setAction(Intent.ACTION_DELETE);
			 intent.setData(Uri.parse("package:"+packageName));
//			 startActivity(intent);
			 startActivityForResult(intent, position);
		 }
	};
	
	/**
	 * ����һ��activity�󣬷��ؽ�����õķ���
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(!checkBrowser(mScanInfoList.get(requestCode).packName)) { //�������Ӧ���Ѿ��������ˣ�ɾ��listview�е�item
			mScanInfoList.remove(requestCode);
			adapter.notifyDataSetChanged();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * �ж�Ӧ���Ƿ����
	 */
    public boolean checkBrowser(String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(
                    packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
	
	/**
	 * ���ٲ�ɱ�İ�ť�¼�
	 */
	public void scan(View v){  
		//��ʾ����ͼƬ
		iv_scan.setVisibility(View.VISIBLE);
		//��鲡��
		checkVirus();
		//ִ�ж���
		iv_scan.startAnimation(rotateAnimation);
		//����仯
		tv_lastTime.setVisibility(View.GONE);
		bt_scan.setVisibility(View.GONE);
		tv_package.setVisibility(View.VISIBLE);
		bt_process.setVisibility(View.VISIBLE);
		pb_bar.setVisibility(View.VISIBLE);
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
		tv_package.setVisibility(View.GONE);
		bt_process.setVisibility(View.GONE);
		pb_bar.setVisibility(View.GONE);
		tv_lastTime.setVisibility(View.VISIBLE);
		bt_scan.setVisibility(View.VISIBLE);
	} 
	
	/**
	 * ��ʼ��������
	 */
	private void initToolbar() {
		mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        mToolbar.setTitle("�ֻ�ɱ�����");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //�˴��������õ�ͼ��
        	//actionBar.setDisplayHomeAsUpEnabled(true);
        }
	}

	/**
	 *  �˵���(���Ϸ�)��������ͼ��
	 */
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //дһ��menu����Դ�ļ�.Ȼ�󴴽�������.
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
	
	/**
	 * ����ͼƬ�İ�ť�¼�
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();   
		if (id == R.id.shezhi) {     
			//Toast.makeText(getApplicationContext(), "����", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
            startActivity(intent);
			return true;   
		}   
		return super.onOptionsItemSelected(item); 
	} 
	
	/**
	 * ��������
	 */
    @Override
    protected void onDestroy() {
    	flag=false;
    	super.onDestroy();
    }

}
