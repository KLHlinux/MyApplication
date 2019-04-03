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
     * ����ֻ��ʾ����
     */
    public void setOnlyTitle() {

        lefttext.setVisibility(INVISIBLE);
        righttext.setVisibility(INVISIBLE);
    }

    /**
     * �����Ҳ಻��ʾ
     */
    public void setNoRightText() {

        righttext.setVisibility(INVISIBLE);
    }


    /**
     * ���ñ���
     * @param text
     */
    public void setToolbarTitle(String text) {

        this.setTitle("");
        titletext.setVisibility(View.VISIBLE);
        titletext.setText(text);


    }

    /**
     * ��������ı�
     * @param text
     */
    public void setLeftText(String text) {

        lefttext.setVisibility(VISIBLE);
        lefttext.setText(text);

        //�����ı�����ʾͼƬ
        lefttext.setCompoundDrawables(null,null,null,null);

    }

    /**
     * �����ұ��ı�
     * @param text
     */
    public void setRightText(String text) {

        righttext.setVisibility(VISIBLE);
        righttext.setText(text);

        //�����ı�����ʾͼƬ
        righttext.setCompoundDrawables(null,null,null,null);

    }


    /**
     * �������ͼƬ
     * @param id
     */
    public void setLeftImg(int id) {

        Drawable drawable = getResources().getDrawable(id);

        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());

        //����ͼƬ����ʾ����
        lefttext.setText("");

        lefttext.setCompoundDrawables(drawable,null,null,null);


    }


    /**
     * �����Ҳ�ͼƬ
     * @param id
     */
    public void setRightImg(int id) {

        Drawable drawable = getResources().getDrawable(id);

        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());

        //����ͼƬ����ʾ����
        righttext.setText("");
        righttext.setCompoundDrawables(null,null,drawable,null);

    }

    //����ı��ص��ӿ�
    public interface OnLeftTextClickListener {
        void onLeftTextClick();
    }

    /**
     * ��������ı��ص�
     * @param listener
     */
    public void setOnLeftTextClickListener(OnLeftTextClickListener listener) {
        this.leftlistener = listener;
    }

    //�Ҳ��ı��ص��ӿ�
    public interface OnRightTextClickListener {
        void onRightTextClick();
    }

    /**
     * �����Ҳ��ı��ص�
     * @param litener
     */
    public void setOnRightTextClickListener(OnRightTextClickListener litener) {
        this.rightlistener = litener;
    }

    /**
     * ���÷���ͼƬ
     * @param id ͼƬ��id
     */
    public void setbackIcon(int id) {

        this.setNavigationIcon(id);

        lefttext.setVisibility(GONE);
        //����ı�������draw
        lefttext.setCompoundDrawables(null,null,null,null);

        this.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }
    

}

