package com.example.myapplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.myapplication.R;
import com.example.myapplication.R.id;
import com.example.myapplication.R.layout;
import com.example.myapplication.util.StreamUtil;
import com.example.myapplication.util.ToastUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {
	
    protected static final String tag = "SplashActivity";

    //更新新版本的状态码
    private static final int UPDATE_VERSION = 100;
    //进入应用程序主界面的状态码
    private static final int ENTER_HOME = 101;
    //出错时的状态码
    private static final int URL_ERROR = 102;
    private static final int IO_ERROR = 103;
    private static final int JSON_ERROR = 104;

    private SharedPreferences mSP;

    private TextView tv_version_name;
    private int mLocalVersionCode;
    private RelativeLayout rl_root;

    private String mVersionDes;
    private String mDownloadUrl;

    private Handler mhandle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case UPDATE_VERSION:
                	//弹出对话框，提示用户更新
                	showUpdateDialog();
                    break;
                case ENTER_HOME:
                	//进入应用主界面
                    enterHome();
                    break;
                case URL_ERROR:
                	//项目上线时要删除
                	ToastUtil.show(SplashActivity.this, "url异常");
                	enterHome();
                    break;
                case IO_ERROR:
                	ToastUtil.show(SplashActivity.this, "读取异常");
                	enterHome();
                    break;
                case JSON_ERROR:
                	ToastUtil.show(SplashActivity.this, "json异常");
                	enterHome();
                	break;
            }
        }
    };



	@Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        
        //初始化UI
        initUI();
        //初始化数据
        initDate();
        //初始化数据库
        initDB();
    }
	 

    /**
     *  初始化数据库
     */
	private void initDB() {
		initVirusDB("antivirus.db");
		
	}

	/**
	 * 拷贝数据库值files文件夹下
	 * @param dbName 数据库名称
	 */
	private void initVirusDB(String dbName) {
		//1.在files文件夹下创建同名的dbName数据库文件过程
		File files = getFilesDir();
		File file = new File(files, dbName);
		if(file.exists()) {
			return;
		}
		InputStream stream = null;
		FileOutputStream fos = null;
		try {
			//2.输入流读取第三方资产目录下的文件
			stream = getAssets().open(dbName);
			//3.将读取的内容写入到指定文件夹的文件中去
			fos = new FileOutputStream(file);
			//4.每次的读取内容大小
			byte[] bs = new byte[1024];
			int temp = -1;
			while((temp = stream.read(bs))!= -1) {
				fos.write(bs,0,temp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(stream!=null && fos!=null) {
				try {
					stream.close();
					fos.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}


	/**
     * 进入应用程序主界面
     */
    private void enterHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        //在开启一个新的界面后，将导航界面关闭(导航界面只可见一次)
        finish();
    }

    /**
     * 弹出对话框，提示用户更新
     */
    protected void showUpdateDialog() {
		//对话框，是依赖于activity存在的
    	Builder builder = new AlertDialog.Builder(this);
    	
    	//设置左上角图标
    	builder.setIcon(R.drawable.ic_launcher);
    	builder.setTitle("版本更新");
    	
    	//设置描述内容
    	builder.setMessage(mVersionDes);
    	
    	//积极按钮，立即更新
    	builder.setPositiveButton("立即更新", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//下载apk,apk的链接地址,downloadUrl
				downloadAPK();
			}
		});
    	
    	//消极按钮，稍后再说
    	builder.setNegativeButton("稍后再说", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//取消对话框，进入主界面
				enterHome();
			}
		});
    	
    	//点击取消事件监听
    	builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				//即使用户点击取消，也需要让其进入应用程序主界面
				enterHome();
				dialog.dismiss();
			}
		});

    	builder.show();
    	
	}


    /**
     * 下载apk,apk的链接地址,downloadUrl
     */
    protected void downloadAPK() {
    	//apk下载链接地址，放置apk的所在路径
    	
    	//1.判断sd卡是否可用，是否挂在上
    	if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
    		//2.获取sd卡路径
    		String path = Environment.getExternalStorageDirectory().getAbsolutePath()
    				+ File.separator + "MyApplication.apk" ;
    		//3.发送请求，获取apk，并且放置到指定路径
    		HttpUtils httpUtils = new HttpUtils(); 
    		httpUtils.download(mDownloadUrl, path, new RequestCallBack<File>() {
    			//下载成功(下载过后的放置在sd卡中apk)
				@Override
				public void onSuccess(ResponseInfo<File> responseInfo) {
					Log.i(tag,"下载成功");
					File file = responseInfo.result;
					//提示用户安装
					installApk(file);
				}
				//下载失败
				@Override
				public void onFailure(HttpException arg0, String arg1) {
					Log.i(tag,"下载失败");
				}
				//刚刚开始下载方法
				@Override
				public void onStart() {
					Log.i(tag,"刚刚开始下载");
					super.onStart();
				}
				//下载过程中的方法(下载apk总大小，当前的下载位置，是否正在下载)
				@Override
				public void onLoading(long total, long current, boolean isUploading) {
					Log.i(tag,"下载中.....");
					Log.i(tag,"total = " + total);
					Log.i(tag,"current = " + current);
					super.onLoading(total, current, isUploading);
				}
			});
    	}
    	
	}

    /**
     * 安装对应的apk
     * @param file 安装文件
     */
	protected void installApk(File file) {
		//系统应用界面,源码,安装apk入口
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		//参数的文件作为数据源
//		intent.setData(Uri.fromFile(file));
		//设置安装的类型
//		intent.setType("application/vnd.android.package-archive");
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		//无返回的启动
//		startActivity(intent);
		//有返回结果的启动
		startActivityForResult(intent, 0);
		
	}
	
	/**
	 * 开启一个activity后，返回结果调用的方法
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		enterHome();//进入主界面
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
     * 获取数据方法
     */
    private void initDate() {
        //1.应用版本名称
        tv_version_name.setText("版本名称:"+getVersionName());
        //检查(本地版本号和服务器版本号对比)是否有更新，如果有更新，提示用户下载(member)
        //2.获取本地版本号
        mLocalVersionCode = getVersionCode();
        //3.获取服务器的版本号(客户端发请求，服务端响应,(json,xml))
        checkVersion();
    }

    /**
     * 检测版本号
     */
    private void checkVersion() {
        new Thread(){
            private String versionDes;

			public void run(){
                //发送请求获取数据，参数则为请求json的链接地址
                //http://10.85.4.17:8080/update.json

                //创建消息对象
                Message msg = Message.obtain();

                long startTime = System.currentTimeMillis();
                
                try {
                    //1.封装url地址
                    URL url = new URL("http://192.168.0.100:8080/update.json");
                    //2.开启一个链接
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    //3.设置常见请求参数(请求头)
                    //请求超时
                    connection.setConnectTimeout(2000);
                    //读取超时
                    connection.setReadTimeout(2000);
                    //默认就是get请求
                    //connection.setRequestMethod("POST");

                    //4.获取请求成功响应码
                    if (connection.getResponseCode() == 200){
                        //5.以流的形式，将数据获取下来(从链接那里获取了数据)
                        InputStream is = connection.getInputStream();
                        //6.将流转换成字符串(工具类封装)
                        String json = StreamUtil.streamToString(is);
                        Log.i(tag,json);
                        //7.json解析
                        JSONObject jsonObject = new JSONObject(json);

                        String versionName = jsonObject.getString("versionName");
                        mVersionDes = jsonObject.getString("versionDes");
                        String versionCode = jsonObject.getString("versionCode");
                        mDownloadUrl = jsonObject.getString("downloadUrl");

                        Log.i(tag,versionName);
                        Log.i(tag,mVersionDes);
                        Log.i(tag,versionCode);
                        Log.i(tag,mDownloadUrl);
                        
                        //8.对比版本号(服务器版本号>本地版本号，提醒用户更新)
                        if(mLocalVersionCode < Integer.parseInt(versionCode)){
                            //提示用户更新，弹出对话框(UI),消息机制
                            msg.what = UPDATE_VERSION;
                        }else{
                            //进入应用程序主界面
                            msg.what = ENTER_HOME;
                        }
                    }

                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                    msg.what = URL_ERROR;
                }
                catch (IOException e) {
                    e.printStackTrace();
                    msg.what = IO_ERROR;
                }
                catch (JSONException e){
                    e.printStackTrace();
                    msg.what = JSON_ERROR;
                }finally {
                	//指定睡眠时间，请求网络的时长超过4秒则不做处理
                	//请求网络的时长小于4秒，强制让其睡眠满4秒
                	long endTime = System.currentTimeMillis();
                	if(endTime-startTime < 4000) {
                		try {
							Thread.sleep(4000-(endTime-startTime));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
                	}
					mhandle.sendMessage(msg);
				}
            }
        }.start();
    }

    /**
     *  初始化UI方法
     */
    private void initUI() {
        tv_version_name = (TextView) findViewById(R.id.tv_version_name);
        rl_root = (RelativeLayout) findViewById(R.id.rl_root);
    }

    /**
     * 获取版本名称:清单文化中
     * @return
     */
    public String getVersionName() {
        //1.包管理者对象packageManager
        PackageManager pm = getPackageManager();
        try {
            //2.从包的管理者对象中，获取指定包名的基本信息(版本名称) 0代表获取基本信息
            PackageInfo packageInfo = pm.getPackageInfo(this.getPackageName(), 0);
            //3.获取版本名称
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 返回版本号 (非0代表获取成功)
     * @return
     */
    public int getVersionCode() {
        //1.包管理者对象packageManager
        PackageManager pm = getPackageManager();
        try {
            //2.从包的管理者对象中，获取指定包名的基本信息(版本名称) 0代表获取基本信息
            PackageInfo packageInfo = pm.getPackageInfo(this.getPackageName(), 0);
            //3.获取版本号
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}


