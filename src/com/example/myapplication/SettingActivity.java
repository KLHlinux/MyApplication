package com.example.myapplication;

import com.example.myapplication.R;
import com.example.myapplication.mytoolbar.MyToolbar;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class SettingActivity extends AppCompatActivity {
    
	private static String mVersionName;
	public Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        
        //״̬��͸��
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();            
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);            
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        } 
        
        //��ʼ������
        mVersionName = getVersionName();
        
        //��ʼ��������
        initToolbar();
        
        //װ��fragment����
        getFragmentManager()
        	.beginTransaction()
        	.replace(R.id.content_frame, new SettingFragment()).commit();
        
        //��ʼ��ui
        initUI();
    }

    //�����ڲ��������ز���
    public static class SettingFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            
            //�ҵ�preferences.xml�ļ������µİ汾����ͼ����
            Preference preference = findPreference("version");
            
            //��Ϊ��Ӧ�õĵ�ǰ�汾
            preference.setSummary(mVersionName);
        }
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
     * ��ʼ��UI
     */
    private void initUI() {
    	
	}


	/**
     * ��ʼ��Toolbar
     */
    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_setting);
        mToolbar.setTitle("����");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
        //�˴���Ϊ�Լ�����ķ��ص�һ��Сͼ��
        actionBar.setHomeAsUpIndicator(R.drawable.back6);
        actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * ѡ��˵�
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }

}
