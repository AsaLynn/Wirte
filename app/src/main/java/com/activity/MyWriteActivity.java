package com.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bean.PagerItemInfo;
import com.yinghuanhang.pdf.parser.R;
import com.yinghuanhang.pdf.parser.view.MyPageWidget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyWriteActivity extends AppCompatActivity
        implements View.OnClickListener, Handler.Callback {

    public View mUndoView;
    public View mRedoView;
    private View mPenView;
    private View mEraserView;
    private View mClearView;
    private ProgressDialog mSaveProgressDlg;
    private static final int MSG_SAVE_SUCCESS = 1;
    private static final int MSG_SAVE_FAILED = 2;
    private static final int MSG_FINGER_NEXT_SUCCESS = 3;
    private static final int MSG_BTN_NEXT_SUCCESS = 4;
    private Handler mHandler;
    private MyPageWidget page = null;
    private MyWriteAdapter adapter;
    private String TAG = "WriteActivity";
    private ArrayList<PagerItemInfo> mItemInfos;
    private int lastPos;
    private int mCurrentPos;
    private int imgFileNum;
    private boolean hasPageNumChange;
    private boolean isBtnToNextPage;
    private boolean isBtnToLastPage;
    private int nextPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//设置横屏切换
        setContentView(R.layout.item_one);
        initData();
        initBeforeTurnListener();
        iniPageTurnListener();
        initButton();
    }

    private void initData() {
        getFilePagerItemInfos();
        mItemInfos = new ArrayList<>();
        File[] imgFiles = getImgFiles();
        imgFileNum = (imgFiles == null ? 0 : imgFiles.length);
        Log.i(TAG, "imgFileNum: ==>" + imgFileNum);
//        int itemNum = (imgFileNum == 0 ? 500 : imgFileNum);
        //读取已经保存的图片.
        int itemNum = 500;//500
        for (int i = 0; i < itemNum; i++) {
            PagerItemInfo info = new PagerItemInfo();
            if (null != imgFiles && imgFiles.length != 0) {
                if (i < imgFiles.length) {
                    String imgPath = imgFiles[i].getAbsolutePath();
                    info.setImgPath(imgPath);
                    info.setImgNo(i + 1);
                }
            }
            mItemInfos.add(info);
        }

        //不读取已经保存的图片.
       /* int itemNum = 5;
        imgFileNum = 0;
        for (int i = 0; i < itemNum; i++) {
            PagerItemInfo info = new PagerItemInfo();
            mItemInfos.add(info);
        }*/
        page = (MyPageWidget) findViewById(R.id.write_pageWidget);
        adapter = new MyWriteAdapter(this, mItemInfos, imgFileNum);
        page.setAdapter(adapter);
    }

    private void initButton() {
        mUndoView = findViewById(R.id.undo);
        mRedoView = findViewById(R.id.redo);
        mPenView = findViewById(R.id.pen);
        mPenView.setSelected(true);
        mEraserView = findViewById(R.id.eraser);
        mClearView = findViewById(R.id.clear);

        mUndoView.setOnClickListener(this);
        mRedoView.setOnClickListener(this);
        mPenView.setOnClickListener(this);
        mEraserView.setOnClickListener(this);
        mClearView.setOnClickListener(this);

        mUndoView.setEnabled(false);
        mRedoView.setEnabled(false);

        mHandler = new Handler(this);

        findViewById(R.id.btn_last).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
    }

    private void iniPageTurnListener() {
        page.setOnPageTurnListener(new MyPageWidget.OnPageTurnListener() {
            @Override
            public void onTurn(int count, int currentPosition) {
                Log.i(TAG, "onTurn: --->" + "翻页到" + currentPosition + "/" + count + "---ChildCount" + page.getChildCount());
                mCurrentPos = currentPosition;
                PaletteView lastPaletteView = (PaletteView) page.getChildAt(0).findViewById(R.id.palette);
                PagerItemInfo pagerItemInfo = mItemInfos.get(lastPos);
                //备份刚划过的页面数据
                pagerItemInfo.setDrawingInfos(lastPaletteView.backups());
                //清除刚划过的页面数据
                lastPaletteView.clear();
                lastPos = currentPosition;

                //获取当前页面的view,这只tag.
                PaletteView currentPaletteView = (PaletteView) page.getChildAt(0).findViewById(R.id.palette);

                //恢复当前页面数据
                if (null != mItemInfos.get(currentPosition).getDrawingInfos()
                        && 0 != mItemInfos.get(currentPosition).getDrawingInfos().size()) {
                    currentPaletteView.recover(mItemInfos.get(currentPosition).getDrawingInfos());
                    Log.i(TAG, "***recover: " + currentPosition);
                }

                //没有添加新页面的情况,翻页进行保存.
                //如果当前页面保存了,就不要保存了
                //下一页保存,上一页面就不要保存了.
                //------------------
                //点击下一页按钮,保存页面!

                if (!isBtnToNextPage) {
                    boolean isFingerSlidingFromRight = page.ismIsRTandLB();
                    //如果手指从右边滑向左边!,也就是翻下一页.
                    //并且是不是点击了上一页的按钮.
                    if (!isFingerSlidingFromRight && !isBtnToLastPage) {//手指翻下一页,保存页面!
                        //保存图片
                        /*String imgPath = mItemInfos.get(currentPosition).getImgPath();
                        if (TextUtils.isEmpty(imgPath)) {
                            saveFile(MSG_FINGER_NEXT_SUCCESS);
                        }*/
                        Log.i(TAG, "onTurn: --->手指滑动到下一页面" + currentPosition);
                    }
                }

                //标记归位!
                if (isBtnToLastPage) {
                    isBtnToLastPage = false;
                }

                if (isBtnToNextPage) {
                    isBtnToLastPage = true;
                }
            }
        });
    }

    private void initBeforeTurnListener() {
        //initBeforeTurnListener
        page.setOnOnPageBeforeTurnListener(new MyPageWidget.OnPageBeforeTurnListener() {
            @Override
            public void onBeforeTurn(boolean isFromL2R) {
                Log.i(TAG, "onBeforeTurn: ------>*");
                if (!isFromL2R) {
                    if (ContextCompat
                            .checkSelfPermission(MyWriteActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PermissionChecker.PERMISSION_GRANTED) {
                        //没有被授权//请求用户给权限
                        ActivityCompat.requestPermissions(MyWriteActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_SDCARD);
                    } else {
                        if (TextUtils.isEmpty(mItemInfos.get(mCurrentPos).getImgPath())) {
                            savedBitmapFile();
                        }
                    }
                }
            }
        });
    }

    private List<PagerItemInfo> getFilePagerItemInfos() {
        List<PagerItemInfo> items = new ArrayList<>();
        return items;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(MSG_SAVE_FAILED);
        mHandler.removeMessages(MSG_SAVE_SUCCESS);
    }

    private void initSaveProgressDlg() {
        if (null == mSaveProgressDlg) {
            mSaveProgressDlg = new ProgressDialog(this);
        }
        mSaveProgressDlg.setMessage("正在保存,请稍候...");
        mSaveProgressDlg.setCancelable(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SAVE_FAILED:
                mSaveProgressDlg.dismiss();
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
                break;
            case MSG_SAVE_SUCCESS:
                mSaveProgressDlg.dismiss();
                Toast.makeText(this, "画板已保存", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MyImagesActivity.class));
                break;
            case MSG_BTN_NEXT_SUCCESS:
                mSaveProgressDlg.dismiss();
                btnNext();
                break;
            case MSG_FINGER_NEXT_SUCCESS:
                mSaveProgressDlg.dismiss();
                fingerNext();
//                next();
                break;
        }
        return true;
    }

    private void fingerNext() {

    }

    private void btnNext() {
        this.page.setCurrentPosition(nextPosition);
    }

    private static void scanFile(Context context, String filePath) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(scanIntent);
    }

    private static String saveImage(Bitmap bmp, int quality) {
        if (bmp == null) {
            return null;
        }
        File appDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (appDir == null) {
            return null;
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static final int REQUEST_PERMISSION_WRITE_SDCARD = 1;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save://保存.
                saveFile(MSG_SAVE_SUCCESS);
        }
        return true;
    }

    private void saveFile(final int type) {
        if (ContextCompat
                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PermissionChecker.PERMISSION_GRANTED) {
            //没有被授权//请求用户给权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_SDCARD);
        } else {
            initSaveProgressDlg();

            mSaveProgressDlg.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    PaletteView currentPaletteView = (PaletteView) page.getChildAt(0).findViewById(R.id.palette);
                    Bitmap bm = currentPaletteView.buildBitmap();
                    String savedFile = saveImage(bm, 100);
                    if (savedFile != null) {
                        scanFile(MyWriteActivity.this, savedFile);
                        mHandler.obtainMessage(type).sendToTarget();
                    } else {
                        mHandler.obtainMessage(MSG_SAVE_FAILED).sendToTarget();
                    }
                }
            }).start();
        }
    }

    //获取到权限，执行代码逻辑
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_WRITE_SDCARD) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {

                //savedBitmapFile
                savedBitmapFile();
                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PaletteView currentPaletteView = (PaletteView) page.getChildAt(0).findViewById(R.id.palette);
                        Bitmap bm = currentPaletteView.buildBitmap();
                        String savedFile = saveImage(bm, 100);
                        if (savedFile != null) {
                            scanFile(MyWriteActivity.this, savedFile);
                            mHandler.obtainMessage(MSG_SAVE_SUCCESS).sendToTarget();
                        } else {
                            mHandler.obtainMessage(MSG_SAVE_FAILED).sendToTarget();
                        }
                    }
                }).start();*/
            } else {
                Toast.makeText(this, "无文件保存权限!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void savedBitmapFile() {
        if (mSaveProgressDlg == null) {
            initSaveProgressDlg();
        }
        mSaveProgressDlg.show();
        PaletteView currentPaletteView = (PaletteView) page.getChildAt(0).findViewById(R.id.palette);
        Bitmap bm = currentPaletteView.buildBitmap();
        String savedFile = saveImage(bm, 100);
        if (savedFile != null) {
            scanFile(MyWriteActivity.this, savedFile);
            mSaveProgressDlg.dismiss();
        } else {
            mSaveProgressDlg.dismiss();
            Toast.makeText(this, "文件保存失败!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.undo://回退,
                PaletteView currentPaletteView = (PaletteView) page.getChildAt(0).findViewById(R.id.palette);
                currentPaletteView.undo();
                break;
            case R.id.redo://前进
                PaletteView currentPaletteView1 = (PaletteView) page.getChildAt(0).findViewById(R.id.palette);
                currentPaletteView1.redo();
                break;
            case R.id.pen://画笔
                v.setSelected(true);
                mEraserView.setSelected(false);
                PaletteView currentPaletteView2 = (PaletteView) page.getChildAt(0).findViewById(R.id.palette);
                currentPaletteView2.setMode(PaletteView.Mode.DRAW);
                break;
            case R.id.eraser://橡皮
                v.setSelected(true);
                mPenView.setSelected(false);
                PaletteView currentPaletteView3 = (PaletteView) page.getChildAt(0).findViewById(R.id.palette);
                currentPaletteView3.setMode(PaletteView.Mode.ERASER);
                break;
            case R.id.clear://清除
                PaletteView currentPaletteView4 = (PaletteView) page.getChildAt(0).findViewById(R.id.palette);
                currentPaletteView4.clear();
                break;
            case R.id.btn_last:
                int lastPosition = mCurrentPos - 1;
                if (lastPosition < 0) {
                    Toast.makeText(this, "没有上一页拉!", Toast.LENGTH_SHORT).show();
                    return;
                }
                isBtnToLastPage = true;
                this.page.setCurrentPosition(lastPosition);
                break;
            case R.id.btn_next:
                nextPosition = mCurrentPos + 1;
                if (nextPosition > adapter.getCount() - 1) {
                    Toast.makeText(this, "没有下一页拉!", Toast.LENGTH_SHORT).show();
                    return;
                }
                isBtnToNextPage = true;
                String imgPath = mItemInfos.get(mCurrentPos).getImgPath();
                if (TextUtils.isEmpty(imgPath)) {
                    savedBitmapFile();
                }
                this.page.setCurrentPosition(nextPosition);

                //先保存当前页面
                //saveFile(MSG_FINGER_NEXT_SUCCESS);
                //滑动到下一页面.
//                next();
                break;
        }
    }

    private void next() {
        //最后一个页面索引.
        int lastIndex = adapter.getCount() - 1;
        //如果最后一页则添加一个新页面,在滑动到下一个页面.
        if (mCurrentPos == lastIndex) {
            PagerItemInfo info = new PagerItemInfo();
            mItemInfos.add(info);
            //adapter = new MyWriteAdapter(this, mItemInfos,imgFileNum);
            //标记页面总数量发生变化.
            hasPageNumChange = true;
            page.setAdapter(adapter);
            //adapter.notifyDataSetChanged();
            this.page.setCurrentPosition(adapter.getCount() - 1);
        } else {
            //标记页面总数量未发生变化.
            hasPageNumChange = false;
            //若不是最后一个页面则直接跳转下一页.
            int nextPosition = mCurrentPos + 1;
            this.page.setCurrentPosition(nextPosition);
        }

    }

    private File[] getImgFiles() {
        File fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (fileDir.isDirectory()) {
            if (fileDir.exists()) {
                File[] files = fileDir.listFiles();
                return files;
            }
        }
        return null;
    }
}
