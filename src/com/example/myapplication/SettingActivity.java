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
        
        //状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();            
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);            
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        } 
        
        //初始化数据
        mVersionName = getVersionName();
        
        //初始化工具栏
        initToolbar();
        
        //装配fragment对象
        getFragmentManager()
        	.beginTransaction()
        	.replace(R.id.content_frame, new SettingFragment()).commit();
        
        //初始化ui
        initUI();
    }

    //创建内部类来加载布局
    public static class SettingFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            
            //找到preferences.xml文件布局下的版本的视图对象
            Preference preference = findPreference("version");
            
            //设为该应用的当前版本
            preference.setSummary(mVersionName);
        }
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
     * 初始化UI
     */
    private void initUI() {
    	
	}


	/**
     * 初始化Toolbar
     */
    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_setting);
        mToolbar.setTitle("设置");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
        //此处的为自己定义的返回的一个小图标
        actionBar.setHomeAsUpIndicator(R.drawable.back6);
        actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * 选项菜单
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
