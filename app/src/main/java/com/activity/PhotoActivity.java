package com.activity;

import android.Manifest;
import android.content.Intent;
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

public class PhotoActivity extends AppCompatActivity {

    private SDCardUtils sdCardUtils=new SDCardUtils();
    private GridView mImage;
    private List<Bitmap> date;
    private PageWidget page=null;
    private PhotoAdapter imageApdate;
    private int width=-1;
    private int hight=-1;
    private ArrayList<String> mdata=new ArrayList<String>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//设置横屏切换
        setContentView(R.layout.activity_photo);
        page = (PageWidget) findViewById(R.id.fox_pageWidget);
        date = new ArrayList<Bitmap>();
        mdata=new ArrayList<>();
        sdCardUtils=new SDCardUtils();
        width = getWindowManager().getDefaultDisplay().getWidth(); // 屏幕宽（像素，如：720px）
        hight = getWindowManager().getDefaultDisplay().getHeight(); // 屏幕高（像素，如：1280px）

        try {
            //从文件中读取pdf(路径）
            Intent intent=getIntent();
            String uFile="";
            if (intent!=null){
                uFile=intent.getStringExtra("file");
            }
            upZipFile(sdCardUtils.getSdPath()+"/"+uFile,sdCardUtils.getSdPath()+"/zipOrd");
//            imageApdate = new ImageApdates(FoxActivity.this,date);
//            page.setAdapter(imageApdate);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
//                    ImageApdate imageApdate=new ImageApdate(Folw.this,date);
                    imageApdate=new PhotoAdapter(PhotoActivity.this,date);
                    page.setAdapter(imageApdate);
                    break;
            }
        }
    };

    /**
     * @param archive       待解压文件
     * @param decompressDir 解压后文件的存放目录
     * @throws IOException
     */
    public  void upZipFile(String archive, String decompressDir) throws IOException, FileNotFoundException, ZipException {
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
                Bitmap bitmap= BitmapFactory.decodeStream(zf.getInputStream(ze2));//把流转化为bitmp
                date.add(bitmap);
                bos.close();
            }
        }
        zf.close();
        Log.e("ssssssss","解压成功");
        Message message=new Message();
        message.what=0;
        mHandler.sendMessage(message);
    }

}
