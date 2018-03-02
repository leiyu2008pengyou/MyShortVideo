package com.example.leiyu.myshortvideo.videogride.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.example.leiyu.myshortvideo.R;
import com.example.leiyu.myshortvideo.videoedit.activity.VideoEditActivity;
import com.example.leiyu.myshortvideo.videogride.adapter.PictureImageGridAdapter;
import com.example.leiyu.myshortvideo.videogride.dialog.SweetAlertDialog;
import com.example.leiyu.myshortvideo.videogride.entity.FunctionConfig;
import com.example.leiyu.myshortvideo.videogride.entity.LocalMedia;
import com.example.leiyu.myshortvideo.videogride.itemdecoration.GridSpacingItemDecoration;
import com.example.leiyu.myshortvideo.entity.LocalMediaFolder;
import com.example.leiyu.myshortvideo.entity.LocalMediaLoader;
import com.example.leiyu.myshortvideo.utils.DisplayUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by leiyu on 2018/3/2.
 * 先把手机里的所有媒体文件显示
 */

public class VideoGridActivity extends FragmentActivity implements View.OnClickListener{
    protected FunctionConfig config = new FunctionConfig();
    protected boolean showCamera = false;
    protected int type = 0;
    protected int maxSelectNum = 0;
    protected int spanCount = 4;
    protected int selectMode = FunctionConfig.MODE_MULTIPLE;
    protected boolean enablePreview = false;
    protected boolean enablePreviewVideo = true;
    protected boolean is_checked_num;
    protected int cb_drawable = 0;
    private RecyclerView mRecyclerView;
    private PictureImageGridAdapter mAdapter;
    private SweetAlertDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = (FunctionConfig) getIntent().getSerializableExtra(FunctionConfig.EXTRA_THIS_CONFIG);
        if (config != null) {
            type = config.getType();
            showCamera = config.isShowCamera();
            enablePreview = config.isEnablePreview();
            selectMode = config.getSelectMode();

            maxSelectNum = config.getMaxSelectNum();
            enablePreviewVideo = config.isPreviewVideo();
            spanCount = config.getImageSpanCount();
            cb_drawable = config.getCheckedBoxDrawable();
            is_checked_num = config.isCheckNumMode();
            // 如果是显示数据风格
            if (is_checked_num) {
                cb_drawable = R.drawable.lf_ugc_publish_checkbox_num_selector;
            }
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_grid);
        initView();
        initData();
    }


    private void initView(){
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    }

    private void initData(){
        readLocalMedia();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, DisplayUtil.dip2px(this, 4), false));
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mAdapter = new PictureImageGridAdapter(this, showCamera, maxSelectNum, selectMode, enablePreview, enablePreviewVideo, cb_drawable, is_checked_num);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnPhotoSelectChangedListener(mOnPhotoSelectChangedListener);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
    }

    private PictureImageGridAdapter.OnPhotoSelectChangedListener mOnPhotoSelectChangedListener = new PictureImageGridAdapter.OnPhotoSelectChangedListener() {
        @Override
        public void onTakePhoto() {
        }

        @Override
        public void onChange(List<LocalMedia> selectImages) {
        }

        @Override
        public void onPictureClick(LocalMedia media, int position) {
            startPreview(mAdapter.getImages(), position);
        }
    };

    private RecyclerView.OnScrollListener mOnScrollListener =new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            switch (newState){
                case RecyclerView.SCROLL_STATE_DRAGGING:
                    ImageLoader.getInstance().pause();
                    break;
                case RecyclerView.SCROLL_STATE_IDLE:
                    ImageLoader.getInstance().resume();
                    break;
                default: break;
            }
        }
    };

    public void startPreview(List<LocalMedia> previewImages, int position) {
        LocalMedia media = previewImages.get(position);
        int type = media.getType();
        switch (type) {
            case LocalMediaLoader.TYPE_VIDEO: // 视频
                if (media.getDuration() < config.getSelectVideoMinDuration()) {
                    Toast.makeText(this, "视频时长不足"+config.getSelectVideoMinDuration()/1000+"秒无法上传", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (media.getDuration() > config.getSelectVideoMaxDuration()) {
                    Toast.makeText(this, "最多支持上传时长"+(config.getSelectVideoMaxDuration()/60/1000)+"分钟的视频", Toast.LENGTH_SHORT).show();
                    return;
                }
                VideoEditActivity.launch(this, media.getSourcePath());
                break;
            default:
                break;
        }
    }

    protected void readLocalMedia() {
        /**
         * 根据type决定，查询本地图片或视频。
         */
        showDialog("请稍候...");
        new LocalMediaLoader(this, type).loadAllImage(new LocalMediaLoader.LocalMediaLoadListener() {

            @Override
            public void loadComplete(List<LocalMediaFolder> folders) {
                dismiss();
                if (folders.size() > 0) {
                    LocalMediaFolder folder = folders.get(0);
                    mAdapter.bindImagesData(folder.getImages());
                }
            }
        });
    }

    private void showDialog(String msg) {
        mDialog = new SweetAlertDialog(VideoGridActivity.this);
        mDialog.setTitleText(msg);
        mDialog.show();
    }


    private void dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.cancel();
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearData();
    }

    protected void clearData() {
        mAdapter = null;
        mRecyclerView = null;
    }
}
