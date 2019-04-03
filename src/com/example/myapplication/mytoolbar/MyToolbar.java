package com.example.myapplication.mytoolbar;

import com.example.myapplication.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class MyToolbar extends Toolbar{
    private TextView lefttext;
    private TextView titletext;
    private TextView righttext;

    private Toolbar toolbar;

    OnLeftTextClickListener leftlistener;
    OnRightTextClickListener rightlistener;

    public MyToolbar(Context context) {
        this(context, null);
    }

    public MyToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.toolbar_layout,this);
    }
    
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        lefttext = (TextView) findViewById(R.id.toolbar_left);
        titletext = (TextView) findViewById(R.id.toolbar_title);
        righttext = (TextView) findViewById(R.id.toolbar_right);
        toolbar = (Toolbar) findViewById(R.id.toolbar_layout);

        lefttext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                leftlistener.onLeftTextClick();
            }
        });

        righttext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rightlistener.onRightTextClick();
            }
        });
        
    }


    public void setToolbarBackgroundColor(int color) {

        toolbar.setBackgroundResource(color);

    }

    /**
     * 设置只显示标题
     */
    public void setOnlyTitle() {

        lefttext.setVisibility(INVISIBLE);
        righttext.setVisibility(INVISIBLE);
    }

    /**
     * 设置右侧不显示
     */
    public void setNoRightText() {

        righttext.setVisibility(INVISIBLE);
    }


    /**
     * 设置标题
     * @param text
     */
    public void setToolbarTitle(String text) {

        this.setTitle("");
        titletext.setVisibility(View.VISIBLE);
        titletext.setText(text);


    }

    /**
     * 设置左侧文本
     * @param text
     */
    public void setLeftText(String text) {

        lefttext.setVisibility(VISIBLE);
        lefttext.setText(text);

        //设置文本则不显示图片
        lefttext.setCompoundDrawables(null,null,null,null);

    }

    /**
     * 设置右边文本
     * @param text
     */
    public void setRightText(String text) {

        righttext.setVisibility(VISIBLE);
        righttext.setText(text);

        //设置文本则不显示图片
        righttext.setCompoundDrawables(null,null,null,null);

    }


    /**
     * 设置左侧图片
     * @param id
     */
    public void setLeftImg(int id) {

        Drawable drawable = getResources().getDrawable(id);

        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());

        //设置图片则不显示文字
        lefttext.setText("");

        lefttext.setCompoundDrawables(drawable,null,null,null);


    }


    /**
     * 设置右侧图片
     * @param id
     */
    public void setRightImg(int id) {

        Drawable drawable = getResources().getDrawable(id);

        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());

        //设置图片则不显示文字
        righttext.setText("");
        righttext.setCompoundDrawables(null,null,drawable,null);

    }

    //左侧文本回调接口
    public interface OnLeftTextClickListener {
        void onLeftTextClick();
    }

    /**
     * 设置左侧文本回调
     * @param listener
     */
    public void setOnLeftTextClickListener(OnLeftTextClickListener listener) {
        this.leftlistener = listener;
    }

    //右侧文本回调接口
    public interface OnRightTextClickListener {
        void onRightTextClick();
    }

    /**
     * 设置右侧文本回调
     * @param litener
     */
    public void setOnRightTextClickListener(OnRightTextClickListener litener) {
        this.rightlistener = litener;
    }

    /**
     * 设置返回图片
     * @param id 图片的id
     */
    public void setbackIcon(int id) {

        this.setNavigationIcon(id);

        lefttext.setVisibility(GONE);
        //左侧文本不设置draw
        lefttext.setCompoundDrawables(null,null,null,null);

        this.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }
    

}

