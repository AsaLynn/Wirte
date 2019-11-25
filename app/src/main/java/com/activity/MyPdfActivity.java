package com.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.artifex.mupdfdemo.MuPDFCore;
import com.yinghuanhang.pdf.parser.FileUtils;
import com.yinghuanhang.pdf.parser.R;
import com.yinghuanhang.pdf.parser.adapter.MyPdfAdapters;
import com.yinghuanhang.pdf.parser.application.MyApplication;
import com.yinghuanhang.pdf.parser.db.TxtInfoDao;
import com.yinghuanhang.pdf.parser.db.entity.TxtInfo;
import com.yinghuanhang.pdf.parser.view.MyPageWidget;
import com.zxn.library.blackboxprogress.BlackBoxProgress;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * create by liuliping on 2018/01/09
 */
public class MyPdfActivity extends AppCompatActivity {

    private MyPdfAdapters mAdapter;
    private SDCardUtils sdCardUtils = new SDCardUtils();
    private static List<String> dates;
    private MyPageWidget page;
    private int width = -1;
    private int hight = -1;
    private TxtInfoDao txtInfoDao;
    private TxtInfo mTxtInfo;
    private File file;
    private String TAG = this.getClass().getSimpleName();
    private int lastPosition;

    @Override
    protected void onCreate(@Nullable Bundle instanceState) {
        super.onCreate(instanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题
        setContentView(R.layout.activity_mupdf);

        page = (MyPageWidget) findViewById(R.id.pdfs_pageWidget);
        page.setOnPageTurnListener(new MyPageWidget.OnPageTurnListener() {
            @Override
            public void onTurn(int count, int currentPosition) {
                mTxtInfo.setPosition(currentPosition);
                txtInfoDao.update(mTxtInfo);
            }
        });
        dates = new ArrayList<>();
        sdCardUtils = new SDCardUtils();
        width = getWindowManager().getDefaultDisplay().getWidth(); // 屏幕宽（像素，如：720px）
        hight = getWindowManager().getDefaultDisplay().getHeight(); // 屏幕高（像素，如：1280px）

        EventBus.getDefault().register(this);

        initDb();

        //dialog();
    }

    private void read() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        onFindingPortablePathToParse(file.getPath());
    }

    private void dialog() {
        new AlertDialog
                .Builder(this)
                .setMessage("是否返回上次阅读的位置,继续阅读?")
                .setCancelable(false)
                .setNegativeButton("首页", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTxtInfo.setPosition(0);
                        txtInfoDao.update(mTxtInfo);
                        lastPosition = mTxtInfo.getPosition();
                        read();
                    }
                })
                .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lastPosition = mTxtInfo.getPosition();
                        read();
                    }
                })
                .create()
                .show();
        ;
    }

    private void initDb() {
        txtInfoDao = MyApplication
                .getDaoSession()
                .getTxtInfoDao();
        file = EventBus.getDefault().getStickyEvent(File.class);
        TxtInfo info = txtInfoDao
                .queryBuilder()
                .where(TxtInfoDao.Properties.Name.eq(file.getName()))
                .unique();
        if (null == info) {
            mTxtInfo = new TxtInfo() {
                {
                    setName(file.getName());
                    setPosition(0);
                    setPath(file.getPath());
                }
            };
            txtInfoDao
                    .insert(mTxtInfo);
            mTxtInfo.setPosition(0);
            txtInfoDao.update(mTxtInfo);
            read();
        } else {
            mTxtInfo = info;
            dialog();
        }
    }

    private void onFindingPortablePathToParse(String path) {
        if (!TextUtils.isEmpty(path)) {
            try {
                MuPDFCore core = new MuPDFCore(path);
                onParsingToStorage(core, path);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "打开文档失败", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void onParsingToStorage(MuPDFCore core, String document) {
        File file = new File(document);
        String name = file.getName().toLowerCase();
        new DocumentParser(core).execute(name.replace(".pdf", ""));
    }

    private class DocumentParser extends AsyncTask<String, Void, List<String>> {

        private MuPDFCore mParser;
        private BlackBoxProgress progressDialog;

        DocumentParser(MuPDFCore core) {
            mParser = core;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = BlackBoxProgress.create(MyPdfActivity.this)
                    .setStyle(BlackBoxProgress.Style.SPIN_INDETERMINATE)
                    .setLabel("文件正在读取中...")
                    .setCancellable(false)
                    .show();
        }

        @Override
        protected List<String> doInBackground(String... strings) {
            MuPDFCore.Cookie cookie = mParser.new Cookie();
            for (int index = 0; index < mParser.countPages(); index++) {
                String name = String.format(Locale.getDefault(), "%s(%d)", strings[0], (index + 1));
                String path = FileUtils.onBuildingCache("temporary/" + strings[0], name, "png");
                PointF size = mParser.getPageSize(index);
                Bitmap bitmap = Bitmap.createBitmap((int) size.x, (int) size.y, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(Color.BLACK);
                mParser.drawPage(bitmap, index, (int) size.x, (int) size.y, 0, 0, (int) size.x, (int) size.y, cookie);
                FileUtils.onSaveBitmapTo(bitmap, path);
                bitmap.recycle();
                dates.add(path);
            }
            cookie.destroy();
            return dates;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            //progressDialog.setLabel("读取成功!");
            progressDialog.dismiss();
            Log.i("onPostExecute", "onPostExecute,size: --->" + strings.size());
            mAdapter = new MyPdfAdapters(MyPdfActivity.this, strings);
            page.setAdapter(mAdapter);
            Log.i(TAG, "getPosition: --->" + mTxtInfo.getPosition());
            page.setCurrentPosition(lastPosition);

        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFileEvent(File file) {
    }
}
