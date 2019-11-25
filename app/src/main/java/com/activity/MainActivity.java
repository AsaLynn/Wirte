package com.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.yinghuanhang.pdf.parser.R;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private Button btn_read = null;
    private SDCardUtils sdCardUtils = null;
    private PageWidgetAdapter adapter = null;
    private ArrayList<String> mdata = new ArrayList<String>();
    private PageWidget page = null;
    private int width = -1;
    private int hight = -1;
    private int position = 0;
    private SharedPreferences sp;
    String filepath;
    private String files = "";//文件名
    private String TAG = "MainActivity";
    private String TAG_STR = "***--->";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//设置横屏切换
        setContentView(R.layout.activity_main);
        mdata = new ArrayList<>();
        sdCardUtils = new SDCardUtils();
        page = (PageWidget) findViewById(R.id.main_pageWidget);
        width = getWindowManager().getDefaultDisplay().getWidth(); // 屏幕宽（像素，如：720px）
        hight = getWindowManager().getDefaultDisplay().getHeight(); // 屏幕高（像素，如：1280px）

        position = SharedPreferencedUtils.getInteger(this, "position", 0);
        files = SharedPreferencedUtils.getString(this, "files", "");
        Log.i(TAG, "onCreate: " + TAG_STR + files);

        if (position != 0 && !"".equals(files)) {
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle("提示信息")
                    .setNegativeButton("打开首页", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //返回首页
                        }
                    }).setPositiveButton("打开上一次浏览位置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //设置上次浏览的位置
                            showText(files, position);
                        }
                    })
                    .setMessage("返回上次浏览地方").create();
            dialog.show();
        }
        page.setOnPageTurnListener(new PageWidget.OnPageTurnListener() {
            @Override
            public void onTurn(int count, int currentPosition) {
                //currentPosition 这个就是下标
                position = currentPosition;
                Log.i("test", "  ____ " + position);
            }
        });


        filepath = sdCardUtils.getSdPath();

        if (sdCardUtils.isSdSafe()) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            100);
                }
            } else {
                showText(filepath, 0);//txt
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showText(filepath, 0);//txt
            } else {
                // Permission Denied
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    static StringBuilder tvInfo = new StringBuilder();

    private void showText(String strpath, int position) {
        File file;
        if (position != 0 && !"".equals(files)) {
            file = new File(files);//z
            if (file.exists()) {
                readFile(file);
            }
        } else {
            Intent intent = getIntent();
            String uFile = "";
            if (intent != null) {
                uFile = intent.getStringExtra("file");
            }
            file = new File(strpath + "/" + uFile);//z
            if (file.exists()) {
                readFile(file);
            }
            if (file != null) {
                SharedPreferencedUtils.setString(this, "files", strpath + "/" + uFile);
            }
        }

        BufferedReader reader;
        String str = "";
        try {

            FileInputStream is = new FileInputStream(file);
//            InputStreamReader input = new InputStreamReader(is, "gb2312");
//            BufferedReader reader = new BufferedReader(input);
            BufferedInputStream in = new BufferedInputStream(is);
            in.mark(4);
            byte[] first3bytes = new byte[3];
            in.read(first3bytes);//找到文档的前三个字节并自动判断文档类型。
            in.reset();
            if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB && first3bytes[2] == (byte) 0xBF) {
                reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
            } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFE) {
                reader = new BufferedReader(new InputStreamReader(in, "unicode"));
            } else if (first3bytes[0] == (byte) 0xFE && first3bytes[1] == (byte) 0xFF) {
                reader = new BufferedReader(new InputStreamReader(in, "utf-16be"));
            } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFF) {
                reader = new BufferedReader(new InputStreamReader(in, "utf-16le"));
            } else {
                reader = new BufferedReader(new InputStreamReader(in, "GBK"));
            }
            while ((str = reader.readLine()) != null) {
                tvInfo.append(str);
                tvInfo.append("\n");
            }
            int abs = (width / 2) * (hight);
            int tsb = (3 * 9) * (3 * 16);
            int nb = abs / tsb;
            int sss = 0;
            if (tvInfo.length() > nb) {
                for (int i = 0; i < tvInfo.length() / nb; i++) {
                    sss = nb * i;
                    String ss = tvInfo.substring(sss, sss + nb);
                    mdata.add(ss);
                }

                mdata.add(tvInfo.substring(sss + nb));

            } else {
                mdata.add(tvInfo.toString());
            }


            adapter = new PageWidgetAdapter(this, mdata);
            if (position != 0) {
                adapter.setViewContent(page, position);
            }
            page.setAdapter(adapter);

            tvInfo.delete(0, tvInfo.length());
//            mdata.clear();
            reader.close();
//            input.close();
            in.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readFile(File file) {
        try {
            List<String> list = FileUtils.readLines(file, "UTF-8");
            Log.i(TAG, "readFile: " + TAG_STR + list);
            for (int i = 0; i < list.size(); i++) {
                System.out.println("tag -n 12-27  ----> " + list.get(i));
                Log.i(TAG, "readFile: " + TAG_STR + list.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (adapter != null && position != 0) {
            SharedPreferencedUtils.setInteger(this, "position", position);
        }
    }
}
