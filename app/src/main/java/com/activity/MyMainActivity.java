package com.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.yinghuanhang.pdf.parser.R;
import com.yinghuanhang.pdf.parser.adapter.MyPageWidgetAdapter;
import com.yinghuanhang.pdf.parser.application.MyApplication;
import com.yinghuanhang.pdf.parser.db.TxtInfoDao;
import com.yinghuanhang.pdf.parser.db.entity.TxtInfo;
import com.yinghuanhang.pdf.parser.view.MyPageWidget;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MyMainActivity extends Activity {

    private SDCardUtils sdCardUtils = null;
    private MyPageWidgetAdapter adapter = null;
    private ArrayList<String> mdata = new ArrayList<>();
    private MyPageWidget page;
    private int width = -1;
    private int hight = -1;

    String filepath;
    private String TAG = "MainActivity";
    private String TAG_STR = "***--->";
    private TxtInfoDao txtInfoDao;
    private TxtInfo txtInfo;
    private File file;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//设置横屏切换
        setContentView(R.layout.activity_main_my);

        EventBus.getDefault().register(this);
        //onFileEvent( EventBus.getDefault().getStickyEvent(File.class));
        //onFileEvent(file);

        sdCardUtils = new SDCardUtils();
        filepath = sdCardUtils.getSdPath();

        initView();

        initTxtDb();

//        dialog();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFileEvent(File file) {
        //Log.i(TAG, "onCreate: --->" + file.getName());
    }

    private void initView() {
        page = (MyPageWidget) findViewById(R.id.main_pageWidget);
        page.setOnPageTurnListener(new MyPageWidget.OnPageTurnListener() {
            @Override
            public void onTurn(int count, int currentPosition) {
                //lastPosition = currentPosition;
                txtInfo.setPosition(currentPosition);
                txtInfoDao.update(txtInfo);
            }
        });
        width = getWindowManager().getDefaultDisplay().getWidth(); // 屏幕宽（像素，如：720px）
        hight = getWindowManager().getDefaultDisplay().getHeight(); // 屏幕高（像素，如：1280px）


    }

    private void initTxtDb() {
        txtInfoDao = MyApplication
                .getDaoSession()
                .getTxtInfoDao();
        file = EventBus.getDefault().getStickyEvent(File.class);
        TxtInfo uniqueTxtInfo = txtInfoDao
                .queryBuilder()
                .where(TxtInfoDao.Properties.Name.eq(file.getName()))
                .unique();
        if (uniqueTxtInfo == null) {
            txtInfo = new TxtInfo();
            txtInfo.setName(file.getName());
            txtInfo.setPath(file.getPath());
            txtInfo.setPosition(0);
            long insertResult = txtInfoDao.insert(txtInfo);
            if (insertResult > 0) {
                Log.i(TAG, "insertResult: 成功--->" + insertResult);
            } else {
                Log.i(TAG, "insertResult: 失败--->" + insertResult);
            }
            txtInfo.setPosition(0);
            txtInfoDao.update(txtInfo);
            read(txtInfo.getPosition());
        } else {
            txtInfo = uniqueTxtInfo;
            dialog();
        }
    }

    private void dialog() {
        AlertDialog dialog
                = new AlertDialog
                .Builder(this)
                .setTitle("提示信息")
                .setCancelable(false)
                .setNegativeButton("打开首页", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //进入首页,从头阅读.
                        txtInfo.setPosition(0);
                        txtInfoDao.update(txtInfo);
                        read(txtInfo.getPosition());
                    }
                }).setPositiveButton("打开上一次浏览位置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //从上次阅读的位置,继续阅读!
                        read(txtInfo.getPosition());
                    }
                })
                .setMessage("返回上次浏览地方").create();
        dialog.show();
    }

    private void read(final int position) {
        if (sdCardUtils.isSdSafe()) {
            //权限申请.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                } else {
                    ActivityCompat
                            .requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                }
            } else {
                //权限开通可行,读取加载文本
                new Thread() {
                    @Override
                    public void run() {
                        loadFile(position);
                    }
                }.start();
            }
        } else {
            Toast.makeText(this, "当前sd卡不可用,请检查!", Toast.LENGTH_SHORT).show();
        }
    }

    //加载文件.
    private void loadFile(final int position) {
        BufferedReader reader;
        String str = "";
        try {
            //File file = new File(getIntent().getStringExtra("FileAbsolutePath"));
            FileInputStream is = new FileInputStream(file);
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
            tvInfo.delete(0, tvInfo.length());
            reader.close();
            in.close();
            is.close();

            MyMainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mdata.size() == 1) {
                        mdata.add("已经到了最后一页!");
                    }
                    adapter = new MyPageWidgetAdapter(MyMainActivity.this, mdata);
                    page.setAdapter(adapter);
                    page.setCurrentPosition(position);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //showText(filepath, 0);//txt
            } else {
                // Permission Denied
                Toast.makeText(MyMainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    static StringBuilder tvInfo = new StringBuilder();

    @Override
    protected void onDestroy() {
        EventBus
                .getDefault()
                .unregister(this);
        super.onDestroy();
    }

    //    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        if (adapter != null && position != 0) {
//            SharedPreferencedUtils.setInteger(this, "position", position);
//        }
//    }

    //    private void readFile(File file) {
//        try {
//            List<String> list = FileUtils.readLines(file, "UTF-8");
//            Log.i(TAG, "readFile: " + TAG_STR + list);
//            for (int i = 0; i < list.size(); i++) {
//                System.out.println("tag -n 12-27  ----> " + list.get(i));
//                Log.i(TAG, "readFile: " + TAG_STR + list.get(i));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


//    private void showText(String strpath, int position) {
//        File file;
//        if (position != 0 && !"".equals(files)) {
//            file = new File(files);//z
//            if (file.exists()) {
//                readFile(file);
//            }
//        } else {
//            Intent intent = getIntent();
//            String uFile = "";
//            if (intent != null) {
//                uFile = intent.getStringExtra("file");
//            }
//            file = new File(strpath + "/" + uFile);//z
//            if (file.exists()) {
//                readFile(file);
//            }
//            if (file != null) {
//                SharedPreferencedUtils.setString(this, "files", strpath + "/" + uFile);
//            }
//        }
//
//        BufferedReader reader;
//        String str = "";
//        try {
//            FileInputStream is = new FileInputStream(file);
//            BufferedInputStream in = new BufferedInputStream(is);
//            in.mark(4);
//            byte[] first3bytes = new byte[3];
//            in.read(first3bytes);//找到文档的前三个字节并自动判断文档类型。
//            in.reset();
//            if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB && first3bytes[2] == (byte) 0xBF) {
//                reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
//            } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFE) {
//                reader = new BufferedReader(new InputStreamReader(in, "unicode"));
//            } else if (first3bytes[0] == (byte) 0xFE && first3bytes[1] == (byte) 0xFF) {
//                reader = new BufferedReader(new InputStreamReader(in, "utf-16be"));
//            } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFF) {
//                reader = new BufferedReader(new InputStreamReader(in, "utf-16le"));
//            } else {
//                reader = new BufferedReader(new InputStreamReader(in, "GBK"));
//            }
//            while ((str = reader.readLine()) != null) {
//                tvInfo.append(str);
//                tvInfo.append("\n");
//            }
//            int abs = (width / 2) * (hight);
//            int tsb = (3 * 9) * (3 * 16);
//            int nb = abs / tsb;
//            int sss = 0;
//            if (tvInfo.length() > nb) {
//                for (int i = 0; i < tvInfo.length() / nb; i++) {
//                    sss = nb * i;
//                    String ss = tvInfo.substring(sss, sss + nb);
//                    mdata.add(ss);
//                }
//
//                mdata.add(tvInfo.substring(sss + nb));
//
//            } else {
//                mdata.add(tvInfo.toString());
//            }
//
//
//            adapter = new MyPageWidgetAdapter(this, mdata);
//            if (position != 0) {
//                adapter.setViewContent(page, position);
//            }
//            page.setAdapter(adapter);
//
//            tvInfo.delete(0, tvInfo.length());
//            reader.close();
//            in.close();
//            is.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


}
