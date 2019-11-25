package com.activity;

import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.Fragment;

import com.fragment.GirlDetailFragment;
import com.king.base.BaseActivity;
import com.utils.Constants;
import com.yinghuanhang.pdf.parser.R;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jenly <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * @since 2017/1/23
 */
public class ContentActivity extends BaseActivity {

    public static final int GIRL_DETAIL_FRAGMENT = 0X01;




    @Override
    public void initUI() {
        setContentView(R.layout.activity_content);

        Intent intent = getIntent();

        int fragmentId = intent.getIntExtra(KEY_FRAGMENT,0);

        switch (fragmentId){
            case GIRL_DETAIL_FRAGMENT:
                File fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                if (fileDir.isDirectory()) {
                    if (fileDir.exists()) {
                        File[] files = fileDir.listFiles();
                        List<File> fileList = Arrays.asList(files);
                        int position = intent.getIntExtra(Constants.CURRENT_POSTION,0);
                        replaceFragment(GirlDetailFragment.newInstance(fileList,position));
                    }
                }

                break;
            default:
//                LogUtils.w("Not found fragment.");
                break;
        }

    }

    public void replaceFragment(Fragment fragment){
        replaceFragment(R.id.fragment_content,fragment);
    }

    @Override
    public void addListeners() {

    }

    @Override
    public void initData() {

    }

//    @Override
//    public void onEventMessage(EventMessage em) {
//
//    }
}
