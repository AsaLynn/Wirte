package com.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.yinghuanhang.pdf.parser.R;

public class HomeActivity extends Activity implements View.OnClickListener{

    Button Bone,Btwo,Bthere,photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();
    }

    void init(){
        Bone = (Button) findViewById(R.id.btn_one);
        Bone.setOnClickListener(this);
        Btwo = (Button) findViewById(R.id.btn_two);
        Btwo.setOnClickListener(this);
        Bthere = (Button) findViewById(R.id.btn_there);
        Bthere.setOnClickListener(this);
        photo = (Button) findViewById(R.id.btn_photo);
        photo.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.btn_one:
                intent = new Intent(this,MainActivity.class);//txt
                startActivity(intent);
            break;
            case R.id.btn_two:
                intent = new Intent(this,BtonTwo.class);//pdf
                startActivity(intent);
                break;
            case R.id.btn_there:
                intent = new Intent(this,BtonThere.class);//word
                startActivity(intent);
                break;
            case R.id.btn_photo:
                intent = new Intent(this,PhotoActivity.class);//zip图片
                startActivity(intent);
                break;
        }
    }

}
