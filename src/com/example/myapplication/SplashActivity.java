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

    //�����°汾��״̬��
    private static final int UPDATE_VERSION = 100;
    //����Ӧ�ó����������״̬��
    private static final int ENTER_HOME = 101;
    //����ʱ��״̬��
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
                	//�����Ի�����ʾ�û�����
                	showUpdateDialog();
                    break;
                case ENTER_HOME:
                	//����Ӧ��������
                    enterHome();
                    break;
                case URL_ERROR:
                	//��Ŀ����ʱҪɾ��
                	ToastUtil.show(SplashActivity.this, "url�쳣");
                	enterHome();
                    break;
                case IO_ERROR:
                	ToastUtil.show(SplashActivity.this, "��ȡ�쳣");
                	enterHome();
                    break;
                case JSON_ERROR:
                	ToastUtil.show(SplashActivity.this, "json�쳣");
                	enterHome();
                	break;
            }
        }
    };



	@Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        
        //��ʼ��UI
        initUI();
        //��ʼ������
        initDate();
        //��ʼ�����ݿ�
        initDB();
    }
	 

    /**
     *  ��ʼ�����ݿ�
     */
	private void initDB() {
		initVirusDB("antivirus.db");
		
	}

	/**
	 * �������ݿ�ֵfiles�ļ�����
	 * @param dbName ���ݿ�����
	 */
	private void initVirusDB(String dbName) {
		//1.��files�ļ����´���ͬ����dbName���ݿ��ļ�����
		File files = getFilesDir();
		File file = new File(files, dbName);
		if(file.exists()) {
			return;
		}
		InputStream stream = null;
		FileOutputStream fos = null;
		try {
			//2.��������ȡ�������ʲ�Ŀ¼�µ��ļ�
			stream = getAssets().open(dbName);
			//3.����ȡ������д�뵽ָ���ļ��е��ļ���ȥ
			fos = new FileOutputStream(file);
			//4.ÿ�εĶ�ȡ���ݴ�С
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
     * ����Ӧ�ó���������
     */
    private void enterHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        //�ڿ���һ���µĽ���󣬽���������ر�(��������ֻ�ɼ�һ��)
        finish();
    }

    /**
     * �����Ի�����ʾ�û�����
     */
    protected void showUpdateDialog() {
		//�Ի�����������activity���ڵ�
    	Builder builder = new AlertDialog.Builder(this);
    	
    	//�������Ͻ�ͼ��
    	builder.setIcon(R.drawable.ic_launcher);
    	builder.setTitle("�汾����");
    	
    	//������������
    	builder.setMessage(mVersionDes);
    	
    	//������ť����������
    	builder.setPositiveButton("��������", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//����apk,apk�����ӵ�ַ,downloadUrl
				downloadAPK();
			}
		});
    	
    	//������ť���Ժ���˵
    	builder.setNegativeButton("�Ժ���˵", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//ȡ���Ի��򣬽���������
				enterHome();
			}
		});
    	
    	//���ȡ���¼�����
    	builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				//��ʹ�û����ȡ����Ҳ��Ҫ�������Ӧ�ó���������
				enterHome();
				dialog.dismiss();
			}
		});

    	builder.show();
    	
	}


    /**
     * ����apk,apk�����ӵ�ַ,downloadUrl
     */
    protected void downloadAPK() {
    	//apk�������ӵ�ַ������apk������·��
    	
    	//1.�ж�sd���Ƿ���ã��Ƿ������
    	if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
    		//2.��ȡsd��·��
    		String path = Environment.getExternalStorageDirectory().getAbsolutePath()
    				+ File.separator + "MyApplication.apk" ;
    		//3.�������󣬻�ȡapk�����ҷ��õ�ָ��·��
    		HttpUtils httpUtils = new HttpUtils(); 
    		httpUtils.download(mDownloadUrl, path, new RequestCallBack<File>() {
    			//���سɹ�(���ع���ķ�����sd����apk)
				@Override
				public void onSuccess(ResponseInfo<File> responseInfo) {
					Log.i(tag,"���سɹ�");
					File file = responseInfo.result;
					//��ʾ�û���װ
					installApk(file);
				}
				//����ʧ��
				@Override
				public void onFailure(HttpException arg0, String arg1) {
					Log.i(tag,"����ʧ��");
				}
				//�ոտ�ʼ���ط���
				@Override
				public void onStart() {
					Log.i(tag,"�ոտ�ʼ����");
					super.onStart();
				}
				//���ع����еķ���(����apk�ܴ�С����ǰ������λ�ã��Ƿ���������)
				@Override
				public void onLoading(long total, long current, boolean isUploading) {
					Log.i(tag,"������.....");
					Log.i(tag,"total = " + total);
					Log.i(tag,"current = " + current);
					super.onLoading(total, current, isUploading);
				}
			});
    	}
    	
	}

    /**
     * ��װ��Ӧ��apk
     * @param file ��װ�ļ�
     */
	protected void installApk(File file) {
		//ϵͳӦ�ý���,Դ��,��װapk���
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		//�������ļ���Ϊ����Դ
//		intent.setData(Uri.fromFile(file));
		//���ð�װ������
//		intent.setType("application/vnd.android.package-archive");
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		//�޷��ص�����
//		startActivity(intent);
		//�з��ؽ��������
		startActivityForResult(intent, 0);
		
	}
	
	/**
	 * ����һ��activity�󣬷��ؽ�����õķ���
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		enterHome();//����������
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
     * ��ȡ���ݷ���
     */
    private void initDate() {
        //1.Ӧ�ð汾����
        tv_version_name.setText("�汾����:"+getVersionName());
        //���(���ذ汾�źͷ������汾�ŶԱ�)�Ƿ��и��£�����и��£���ʾ�û�����(member)
        //2.��ȡ���ذ汾��
        mLocalVersionCode = getVersionCode();
        //3.��ȡ�������İ汾��(�ͻ��˷����󣬷������Ӧ,(json,xml))
        checkVersion();
    }

    /**
     * ���汾��
     */
    private void checkVersion() {
        new Thread(){
            private String versionDes;

			public void run(){
                //���������ȡ���ݣ�������Ϊ����json�����ӵ�ַ
                //http://10.85.4.17:8080/update.json

                //������Ϣ����
                Message msg = Message.obtain();

                long startTime = System.currentTimeMillis();
                
                try {
                    //1.��װurl��ַ
                    URL url = new URL("http://192.168.0.100:8080/update.json");
                    //2.����һ������
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    //3.���ó����������(����ͷ)
                    //����ʱ
                    connection.setConnectTimeout(2000);
                    //��ȡ��ʱ
                    connection.setReadTimeout(2000);
                    //Ĭ�Ͼ���get����
                    //connection.setRequestMethod("POST");

                    //4.��ȡ����ɹ���Ӧ��
                    if (connection.getResponseCode() == 200){
                        //5.��������ʽ�������ݻ�ȡ����(�����������ȡ������)
                        InputStream is = connection.getInputStream();
                        //6.����ת�����ַ���(�������װ)
                        String json = StreamUtil.streamToString(is);
                        Log.i(tag,json);
                        //7.json����
                        JSONObject jsonObject = new JSONObject(json);

                        String versionName = jsonObject.getString("versionName");
                        mVersionDes = jsonObject.getString("versionDes");
                        String versionCode = jsonObject.getString("versionCode");
                        mDownloadUrl = jsonObject.getString("downloadUrl");

                        Log.i(tag,versionName);
                        Log.i(tag,mVersionDes);
                        Log.i(tag,versionCode);
                        Log.i(tag,mDownloadUrl);
                        
                        //8.�ԱȰ汾��(�������汾��>���ذ汾�ţ������û�����)
                        if(mLocalVersionCode < Integer.parseInt(versionCode)){
                            //��ʾ�û����£������Ի���(UI),��Ϣ����
                            msg.what = UPDATE_VERSION;
                        }else{
                            //����Ӧ�ó���������
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
                	//ָ��˯��ʱ�䣬���������ʱ������4����������
                	//���������ʱ��С��4�룬ǿ������˯����4��
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
     *  ��ʼ��UI����
     */
    private void initUI() {
        tv_version_name = (TextView) findViewById(R.id.tv_version_name);
        rl_root = (RelativeLayout) findViewById(R.id.rl_root);
    }

    /**
     * ��ȡ�汾����:�嵥�Ļ���
     * @return
     */
    public String getVersionName() {
        //1.�������߶���packageManager
        PackageManager pm = getPackageManager();
        try {
            //2.�Ӱ��Ĺ����߶����У���ȡָ�������Ļ�����Ϣ(�汾����) 0�����ȡ������Ϣ
            PackageInfo packageInfo = pm.getPackageInfo(this.getPackageName(), 0);
            //3.��ȡ�汾����
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ���ذ汾�� (��0�����ȡ�ɹ�)
     * @return
     */
    public int getVersionCode() {
        //1.�������߶���packageManager
        PackageManager pm = getPackageManager();
        try {
            //2.�Ӱ��Ĺ����߶����У���ȡָ�������Ļ�����Ϣ(�汾����) 0�����ȡ������Ϣ
            PackageInfo packageInfo = pm.getPackageInfo(this.getPackageName(), 0);
            //3.��ȡ�汾��
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}


