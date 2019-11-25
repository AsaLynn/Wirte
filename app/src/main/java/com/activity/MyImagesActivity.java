package com.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.adapter.EsayGirlAdapter;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.decoration.SpaceDecoration;
import com.utils.Constants;
import com.utils.DensityUtil;
import com.yinghuanhang.pdf.parser.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.king.base.BaseInterface.KEY_FRAGMENT;


public class MyImagesActivity extends AppCompatActivity {

    /**
     * @param listData
     * @param position
     * @param source
     */
    private void startGirlDetail(ArrayList<File> listData, int position, View source) {
        Intent intent = new Intent(this, ContentActivity.class);
        intent.putExtra(KEY_FRAGMENT, ContentActivity.GIRL_DETAIL_FRAGMENT);
       /* intent.putParcelableArrayListExtra(Constants.LIST_GIRL, listData);*/
        intent.putExtra(Constants.CURRENT_POSTION, position);

        ActivityOptionsCompat activityOptionsCompat
                = ActivityOptionsCompat.makeScaleUpAnimation(source, source.getWidth() / 2, source.getHeight() / 2, 0, 0);
        ActivityCompat
                .startActivity(this, intent, activityOptionsCompat.toBundle());
    }

    private List<File> getGirls() {

        File fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (fileDir.isDirectory()) {
            if (fileDir.exists()) {
                File[] files = fileDir.listFiles();
                List<File> fileList = Arrays.asList(files);
                return fileList;
            }
        }
        return null;
    }


    private void initLayoutManager() {
        final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.getRecyclerView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //防止第一行到顶部有空白区域
                staggeredGridLayoutManager.invalidateSpanAssignments();
            }
        });
    }

    private void initView() {
        recyclerView = (EasyRecyclerView) findViewById(R.id.recyclerView);

        esayGirlAdapter
                = new EsayGirlAdapter(this, getGirls() == null ? new ArrayList<File>() : getGirls());

        initLayoutManager();

        SpaceDecoration spaceDecoration = new SpaceDecoration(DensityUtil.dp2px(this, 2));
        recyclerView.addItemDecoration(spaceDecoration);

        recyclerView.setAdapter(esayGirlAdapter);

        getGirls();
        esayGirlAdapter.setOnClickHolderItemListener(new EsayGirlAdapter.OnClickHolderItemListener() {
            @Override
            public void onItemClick(BaseViewHolder holder, int position) {
                startGirlDetail((ArrayList<File>) esayGirlAdapter.getAllData(),position,holder.itemView);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//设置横屏切换
        setContentView(R.layout.activity_my_images);
        initView();
    }

    private EasyRecyclerView recyclerView;

    private EsayGirlAdapter esayGirlAdapter;

}
