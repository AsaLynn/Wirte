package com.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.GridView;

import com.file.zip.ZipEntry;
import com.file.zip.ZipFile;
import com.yinghuanhang.pdf.parser.R;
import com.yinghuanhang.pdf.parser.application.MyApplication;
import com.yinghuanhang.pdf.parser.db.TxtInfoDao;
import com.yinghuanhang.pdf.parser.db.entity.TxtInfo;
import com.yinghuanhang.pdf.parser.view.MyPageWidget;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipException;

public class MyPhotoActivity extends AppCompatActivity {

    private SDCardUtils sdCardUtils = new SDCardUtils();
    private GridView mImage;
    private List<Bitmap> date;
    private MyPageWidget page;
    private PhotoAdapter imageApdate;
    private int width = -1;
    private int hight = -1;
    private ArrayList<String> mdata;
    private TxtInfoDao txtInfoDao;
    private File eventFile;
    private TxtInfo mTxtInfo;
    private String TAG = this.getClass().getSimpleName();
    private int lastPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//设置横屏切换
        setContentView(R.layout.activity_photo);

        page = (MyPageWidget) findViewById(R.id.fox_pageWidget);
        page.setOnPageTurnListener(new MyPageWidget.OnPageTurnListener() {
            @Override
            public void onTurn(int count, int currentPosition) {
                mTxtInfo.setPosition(currentPosition);
                txtInfoDao.update(mTxtInfo);
            }
        });
        date = new ArrayList<>();
        mdata = new ArrayList<>();
        sdCardUtils = new SDCardUtils();
        width = getWindowManager().getDefaultDisplay().getWidth(); // 屏幕宽（像素，如：720px）
        hight = getWindowManager().getDefaultDisplay().getHeight(); // 屏幕高（像素，如：1280px）

        initDb();

        dialog();

        //readImage();
    }

    private void readImage() {
        try {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            upZipFile(eventFile.getPath(), sdCardUtils.getSdPath() + "/zipOrd");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dialog() {
        new AlertDialog.Builder(this)
                .setTitle("请选择是否继续?")
                .setMessage("回到上次位置,继续阅读?")
                .setNegativeButton("首页", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTxtInfo.setPosition(0);
                        txtInfoDao.update(mTxtInfo);
                        lastPosition = mTxtInfo.getPosition();
                        readImage();
                    }
                })
                .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lastPosition = mTxtInfo.getPosition();
                        readImage();
                    }
                })
                .create()
                .show();
    }

    private void initDb() {
        txtInfoDao = MyApplication
                .getDaoSession()
                .getTxtInfoDao();
        eventFile = EventBus.getDefault().getStickyEvent(File.class);
        TxtInfo uniqueInfo = txtInfoDao.
                queryBuilder()
                .where(TxtInfoDao.Properties.Name.eq(eventFile.getName()))
                .unique();
        if (null == uniqueInfo) {
            mTxtInfo = new TxtInfo() {
                {
                    this.setPosition(0);
                    this.setPath(eventFile.getPath());
                    this.setName(eventFile.getName());
                }
            };
            long insertResult = txtInfoDao.insert(mTxtInfo);
            if (insertResult > 0) {
                Log.i(TAG, "initDb: sucess" + insertResult);
            } else {
                Log.i(TAG, "initDb: fail" + insertResult);
            }
        } else {
            mTxtInfo = uniqueInfo;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
//                    ImageApdate imageApdate=new ImageApdate(Folw.this,date);
                    imageApdate = new PhotoAdapter(MyPhotoActivity.this, date);
                    page.setAdapter(imageApdate);
                    page.setCurrentPosition(lastPosition);
                    break;
            }
        }
    };

    /**
     * @param archive       待解压文件
     * @param decompressDir 解压后文件的存放目录
     * @throws IOException
     */
    public void upZipFile(String archive, String decompressDir) throws IOException, FileNotFoundException, ZipException {
        /**
         * BufferedInputStream：读取数据
         */
        BufferedInputStream bi;
        ZipFile zf = new ZipFile(archive, "GBK");
        Enumeration e = zf.getEntries();
        while (e.hasMoreElements()) {
            ZipEntry ze2 = (ZipEntry) e.nextElement();
            String entryName = ze2.getName();
            String path = decompressDir + "/" + entryName;
            if (ze2.isDirectory()) {
                Log.e("TAG", "upZipFile:正在创建解压目录  " + entryName);
                File decompressDirFile = new File(path);
                if (!decompressDirFile.exists()) {
                    decompressDirFile.mkdirs();
                }
            } else {
                Log.e("TAG", "upZipFile: 正在创建解压文件  " + entryName);
                String fileDir = path.substring(0, path.lastIndexOf("/"));
                Log.e("TAG", "upZipFile: " + fileDir);
                File fileDirFile = new File(fileDir);
                if (!fileDirFile.exists()) {
                    fileDirFile.mkdirs();
                }
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(decompressDir + "/" + entryName));
                bi = new BufferedInputStream(zf.getInputStream(ze2));
                byte[] readContent = new byte[1024];
                int readCount = bi.read(readContent);
                while (readCount != -1) {
                    bos.write(readContent, 0, readCount);
                    readCount = bi.read(readContent);
                }
                Bitmap bitmap = BitmapFactory.decodeStream(zf.getInputStream(ze2));//把流转化为bitmp
                date.add(bitmap);
                bos.close();
            }
        }
        zf.close();
        Log.e("ssssssss", "解压成功");
        Message message = new Message();
        message.what = 0;
        mHandler.sendMessage(message);
    }

}
