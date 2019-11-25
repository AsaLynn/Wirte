package com.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.yinghuanhang.pdf.parser.R;

public class BtonThere extends Activity {

    private WebView mContent;
    private SDCardUtils sdCardUtils=new SDCardUtils();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bton_there);
        mContent = (WebView) findViewById(R.id.content);

        WebSettings settings = mContent.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);// 设置WebView可触摸放大缩小
        settings.setUseWideViewPort(true);

        //文件路径
        Intent intent=getIntent();
        String uFile="";
        if (intent!=null){
            uFile=intent.getStringExtra("file");
        }
        WordUtil wu = new WordUtil(sdCardUtils.getSdPath()+"/"+uFile);
        mContent.loadUrl("file:///" + wu.htmlPath);
    }
}
