package com.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.usbtest.uMainActivity;
import com.yinghuanhang.pdf.parser.R;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener{

    Button mRead,mWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题
        setContentView(R.layout.activity_welcome);
        init();
    }

    void init(){
        mRead = (Button) findViewById(R.id.btn_read);
        mWrite = (Button) findViewById(R.id.btn_write);
        mRead.setOnClickListener(this);
        mWrite.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.btn_read :
                intent = new Intent(this,uMainActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_write :
//                intent = new Intent(this,WriteActivity.class);
                intent = new Intent(this,MyWriteActivity.class);
                startActivity(intent);
                break;
        }
    }
}
