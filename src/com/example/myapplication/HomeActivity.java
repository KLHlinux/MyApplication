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
				//扫描对象
				ScanInfo info = (ScanInfo) msg.obj;
				//改变"正在扫描程序："
				tv_package.setText("正在扫描：" + info.name);
							
				//扫描后的列表
				mScanInfoList.add(info);
				adapter.notifyDataSetChanged();
				mScanListView.setSelection(mScanInfoList.size());
				
				break;
				
			case FINISH:
				//按钮变化
				bt_process.setText("完 成");
				bt_process.setBackgroundResource(R.drawable.green_border2);
				
				//改变"正在扫描程序："
				if(!mVirusScanInfoList.isEmpty()) {
					tv_package.setText("发 现 病 毒");
				}else {
					tv_package.setText("手 机 很 安 全");
				}
				
				//停止正在执行的旋转动画//图片隐藏
				iv_scan.setVisibility(View.INVISIBLE);
				iv_scan.clearAnimation();
				
				pb_bar.setVisibility(View.INVISIBLE);
				
				//告知用户卸载包含了病毒的应用
				//unInstallVirus();
				
				//保存杀毒时间
				saveScanTime();
				flag=false;
				break;
			}
		}
		
		/**
		 * 保存杀毒时间
		 */
		private void saveScanTime() {
			Editor edit = mSP.edit();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					Locale.getDefault());
			String currentTime = sdf.format(new Date());
			currentTime = "上次查杀时间：" + currentTime;
			edit.putString("lastVirusScan", currentTime);
			edit.commit();
		}
		
	};

	/**
	 * 生命周期
	 */
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        //状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();            
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);            
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        } 
        
        mSP = getSharedPreferences("config", MODE_PRIVATE);
        
        //初始化工具栏
        initToolbar();
        //初始化图像对象
        initUI();
        //初始化动画动作
        initAnimation();
        
    }
	
	/**
	 * 生命周期
	 */
	@Override
	protected void onResume() {
		String string = mSP.getString("lastVirusScan", "您还没有查杀病毒！");
		tv_lastTime.setText(string);
		super.onResume();
	}
	
	/**
	 * 告知用户卸载包含了病毒的应用
	 */
	protected void unInstallVirus() {
		for(ScanInfo scanInfo : mVirusScanInfoList) {
			String packageName = scanInfo.packName;
			//源码
			//Intent intent = new Intent("android:intent.action.DELETE");
			//intent.addCategory("android.intent.category.DEFAULT");
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_DELETE);
			intent.setData(Uri.parse("package:"+packageName));
			startActivity(intent);
		}
	}
	
	/**
	 * 开启线程来检查病毒
	 */
	private void checkVirus() {
		flag=true;
		isStop=false;
		//扫描前清除之前的扫描记录
		mScanInfoList.clear();
		mVirusScanInfoList.clear();
		new Thread() {
			public void run() {
				//获取数据库中所有的病毒的md5码
				List<String> virusList = VirusDao.getVirusList();
				//获取手机上面的所有应用程序签名文件的md5码
				//1.获取包管理者对象
				PackageManager pm = getPackageManager();
				//2.获取所有应用程序签名文件()
				List<PackageInfo> packageInfoList = pm.getInstalledPackages(
						PackageManager.GET_SIGNATURES + PackageManager.GET_UNINSTALLED_PACKAGES);
				
				//创建记录病毒的集合
//				mVirusScanInfoList = new ArrayList<ScanInfo>();
//				//记录所有应用的集合
//				List<ScanInfo> scanInfoList = new ArrayList<ScanInfo>();
				
				//设置进度条的最大值
				pb_bar.setMax(packageInfoList.size());
				
				//3.遍历应用集合
				for(PackageInfo packageInfo : packageInfoList) {
					if(!flag) {
						isStop=true;
						return;
					}
//					//创建javaBean对象(用于记录)
//					ScanInfo scanInfo = new ScanInfo();
					//获取签名文件的数组
					Signature[] signatures = packageInfo.signatures;
					//获取签名文件数组的第一位，然后将签名文件转md5，将此md5和数据库中的md5比对
					Signature signature = signatures[0];
					String string = signature.toCharsString();
					//32位字符串，16进制字符(0-f)
					String encoder = Md5Util.encoder(string);
					
					//应用的md5码
//					Log.i(tag, encoder);
					
					//一个应用
					ScanInfo scanInfo = new ScanInfo();
					
					//4.比对应用是否为病毒
					if(virusList.contains(encoder)) {
						//5.记录病毒
						scanInfo.isVirus = true;
						mVirusScanInfoList.add(scanInfo);//是病毒时，加入集合中去
					}else {
						scanInfo.isVirus = false;
					}
					//6.维护对象的包名，以及应用名称，和应用图标
					scanInfo.packName = packageInfo.packageName;
					scanInfo.name = packageInfo.applicationInfo.loadLabel(pm).toString();
					scanInfo.appicon = packageInfo.applicationInfo.loadIcon(pm);
//					scanInfoList.add(scanInfo);
					
					//7.在扫描的过程中，需要更新进度条
					index++;
					pb_bar.setProgress(index);
					
					//睡眠一下
					try {
						Thread.sleep(50+ new Random().nextInt(100));
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					//8.在子线程中发送消息，告知主线程更新UI(扫描过程中往线性布局中添加View)
					Message msg = Message.obtain();
					msg.what = SCANING;
					msg.obj = scanInfo;
					mHandler.sendMessage(msg);
				}
				//扫描结束
				Message msg = Message.obtain();
				msg.what = FINISH;
				mHandler.sendMessage(msg);
			};
		}.start();
		
	}	
	
	/**
	 * 初始化动画(动作)
	 */
	private void initAnimation() {
		rotateAnimation = new RotateAnimation(
				0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f) ;
		rotateAnimation.setDuration(1000);
		//指定动画一直旋转
		rotateAnimation.setRepeatCount(RotateAnimation.INFINITE);
		//保持动画执行结束后的状态
		rotateAnimation.setFillAfter(true);
		
	}

	/**
	 * 初始化视图对象
	 */
	private void initUI() {
		iv_scan = (ImageView) findViewById(R.id.iv_scan);
//		ll_add_text = (LinearLayout) findViewById(R.id.ll_add_text);
		bt_scan = (Button) findViewById(R.id.bt_scan);
		bt_process = (Button) findViewById(R.id.bt_process);
		tv_lastTime = (TextView) findViewById(R.id.tv_lastTime);
		tv_package = (TextView) findViewById(R.id.tv_package);
		pb_bar = (ProgressBar) findViewById(R.id.pb_bar);
		
		//扫描的应用列表
		mScanListView = (ListView) findViewById(R.id.lv_scanapps);
		adapter = new ScanVirusAdapter(mScanInfoList, this,mListener);
		mScanListView.setAdapter(adapter);
		
	}
	
	/**
	 * 卸载按钮事件
	 */
	private MyClickListener mListener = new MyClickListener() {
		 public void myOnClick(int position, View v) {
//			 Toast.makeText( HomeActivity.this,
//					 	"listview的内部的按钮被点击了！，位置是-->" + position 
//			 		+ 	",内容是-->"+ mScanInfoList.get(position), 
//			 			Toast.LENGTH_SHORT).show();
		 	
			 String packageName = mScanInfoList.get(position).packName;
			 
			 //调用系统的卸载功能
			 Intent intent = new Intent();
			 intent.setAction(Intent.ACTION_DELETE);
			 intent.setData(Uri.parse("package:"+packageName));
//			 startActivity(intent);
			 startActivityForResult(intent, position);
		 }
	};
	
	/**
	 * 开启一个activity后，返回结果调用的方法
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(!checkBrowser(mScanInfoList.get(requestCode).packName)) { //如果病毒应用已经不存在了，删除listview中的item
			mScanInfoList.remove(requestCode);
			adapter.notifyDataSetChanged();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 判断应用是否存在
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
	 * 快速查杀的按钮事件
	 */
	public void scan(View v){  
		//显示扇形图片
		iv_scan.setVisibility(View.VISIBLE);
		//检查病毒
		checkVirus();
		//执行动画
		iv_scan.startAnimation(rotateAnimation);
		//界面变化
		tv_lastTime.setVisibility(View.GONE);
		bt_scan.setVisibility(View.GONE);
		tv_package.setVisibility(View.VISIBLE);
		bt_process.setVisibility(View.VISIBLE);
		pb_bar.setVisibility(View.VISIBLE);
	} 
	
	/**
	 * 杀毒进程的按钮事件
	 * @param v
	 */
	public void finish(View v){  
		//正在扫描的
		if(flag==true) { 
			flag=false;
			//停止正在执行的旋转动画//图片隐藏
			iv_scan.clearAnimation();
			iv_scan.setVisibility(View.INVISIBLE);
		}
		//扫描完成的
		else {
			onResume();
			bt_process.setText("取 消 扫 描");
			bt_process.setBackgroundResource(R.drawable.check_border1);
		}
		
		//界面的变化
		tv_package.setVisibility(View.GONE);
		bt_process.setVisibility(View.GONE);
		pb_bar.setVisibility(View.GONE);
		tv_lastTime.setVisibility(View.VISIBLE);
		bt_scan.setVisibility(View.VISIBLE);
	} 
	
	/**
	 * 初始化工具栏
	 */
	private void initToolbar() {
		mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        mToolbar.setTitle("手机杀毒软件");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //此处定义设置的图标
        	//actionBar.setDisplayHomeAsUpEnabled(true);
        }
	}

	/**
	 *  菜单栏(右上方)放上设置图标
	 */
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //写一个menu的资源文件.然后创建就行了.
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
	
	/**
	 * 设置图片的按钮事件
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();   
		if (id == R.id.shezhi) {     
			//Toast.makeText(getApplicationContext(), "设置", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
            startActivity(intent);
			return true;   
		}   
		return super.onOptionsItemSelected(item); 
	} 
	
	/**
	 * 生命周期
	 */
    @Override
    protected void onDestroy() {
    	flag=false;
    	super.onDestroy();
    }

}
