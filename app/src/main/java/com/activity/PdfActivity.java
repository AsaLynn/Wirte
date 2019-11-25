package com.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Toast;

import com.artifex.mupdfdemo.MuPDFCore;
import com.yinghuanhang.pdf.parser.FileUtils;
import com.yinghuanhang.pdf.parser.R;
import com.yinghuanhang.pdf.parser.adapter.MyPdfAdapters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * create by liuliping on 2018/01/09
 */
public class PdfActivity extends AppCompatActivity {

    //    private PdfAdapters mAdaptr ;
    private MyPdfAdapters mAdapter;

    private SDCardUtils sdCardUtils = new SDCardUtils();
    private static List<String> dates;
    private PageWidget page = null;
    private int width = -1;
    private int hight = -1;

    @Override
    protected void onCreate(@Nullable Bundle instanceState) {
        super.onCreate(instanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题
        setContentView(R.layout.activity_mupdf);
//        RecyclerView recycler = (RecyclerView) findViewById(R.id.parser_display);

        page = (PageWidget) findViewById(R.id.pdfs_pageWidget);
        dates = new ArrayList<String>();
        sdCardUtils = new SDCardUtils();
        width = getWindowManager().getDefaultDisplay().getWidth(); // 屏幕宽（像素，如：720px）
        hight = getWindowManager().getDefaultDisplay().getHeight(); // 屏幕高（像素，如：1280px）

//        GridLayoutManager manager = new GridLayoutManager(this, 2);

        try {
//            //从文件中读取pdf(路径）
            Intent intent = getIntent();
            String uFile = "";
            if (intent != null) {
                uFile = intent.getStringExtra("file");
            }
            onFindingPortablePathToParse(sdCardUtils.getSdPath() + "/" + uFile);

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } catch (Exception e) {
            e.printStackTrace();
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
        PdfActivity.DocumentParser parser = new PdfActivity.DocumentParser(core) {
            @Override
            protected void onProgressUpdate(String... values) {
                for (String val : values) {
                    dates.add(val);
                }
                mAdapter = new MyPdfAdapters(PdfActivity.this, dates);
                page.setAdapter(mAdapter);
            }
        };
        parser.execute(name.replace(".pdf", ""));
    }

    private static class DocumentParser extends AsyncTask<String, String, Boolean> {
        DocumentParser(MuPDFCore core) {
            mParser = core;
        }

        private MuPDFCore mParser;

        @Override
        protected Boolean doInBackground(String... params) {
            MuPDFCore.Cookie cookie = mParser.new Cookie();
            for (int index = 0; index < mParser.countPages(); index++) {
                String name = String.format(Locale.getDefault(), "%s(%d)", params[0], (index + 1));
                String path = FileUtils.onBuildingCache("temporary/" + params[0], name, "png");
                if (new File(path).exists()) {
                    publishProgress(path);
                    continue;
                }
                PointF size = mParser.getPageSize(index);
                Bitmap bitmap = Bitmap.createBitmap((int) size.x, (int) size.y, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(Color.BLACK);
                mParser.drawPage(bitmap, index, (int) size.x, (int) size.y, 0, 0, (int) size.x, (int) size.y, cookie);
                FileUtils.onSaveBitmapTo(bitmap, path);
                bitmap.recycle();
                publishProgress(path);
            }
            cookie.destroy();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }
}
