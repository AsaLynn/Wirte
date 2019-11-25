package com.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.widget.Toast;

import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnDrawListener;
import com.joanzapata.pdfview.listener.OnLoadCompleteListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;
import com.yinghuanhang.pdf.parser.R;

import java.io.File;

public class BtonTwo extends Activity implements OnPageChangeListener,OnLoadCompleteListener,OnDrawListener {
    private PDFView pdfView ;
    private SDCardUtils sdCardUtils=new SDCardUtils();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bton_two);
        pdfView = (PDFView) findViewById( R.id.pdfView );
        //从文件中读取pdf(路径）

        Intent intent=getIntent();
        String uFile="";
        if (intent!=null){
            uFile=intent.getStringExtra("file");
        }
        displayFromFile(new File(sdCardUtils.getSdPath()+"/"+uFile));
    }

    /**
     * 读取本地PDF文件方法
     * @param file 文件路径
     */
    private void displayFromFile( File file ) {
        pdfView.fromFile(file)   //设置pdf文件地址
                .defaultPage(1)         //设置默认显示第1页
                .onPageChange(this)     //设置翻页监听
                .onLoad(this)           //设置加载监听
                .onDraw(this)            //绘图监听
                .showMinimap(false)     //pdf放大的时候，是否在屏幕的右上角生成小地图
                .swipeVertical( false )  //pdf文档翻页是否是垂直翻页，默认是左右滑动翻页
                .enableSwipe(true)   //是否允许翻页，默认是允许翻
                .load();
    }
    /**
     * 翻页回调
     * @param page
     * @param pageCount
     */
    @Override
    public void onPageChanged(int page, int pageCount) {
//        Toast.makeText( BtonTwo.this , "page= " + page + " pageCount= " + pageCount , Toast.LENGTH_SHORT).show();
    }
    /**
     * 加载完成回调
     * @param nbPages  总共的页数
     */
    @Override
    public void loadComplete(int nbPages) {
        Toast.makeText( BtonTwo.this ,  "加载完成" + nbPages  , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
        // Toast.makeText( MainActivity.this ,  "pageWidth= " + pageWidth + "
        // pageHeight= " + pageHeight + " displayedPage="  + displayedPage , Toast.LENGTH_SHORT).show();
    }
}
