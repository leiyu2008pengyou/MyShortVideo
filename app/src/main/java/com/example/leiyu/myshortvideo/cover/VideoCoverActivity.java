package com.example.leiyu.myshortvideo.cover;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.leiyu.myshortvideo.R;

/**
 * Created by leiyu on 2018/3/1.
 * 截图视频关键帧展示封面图片
 */

public class VideoCoverActivity extends Activity{
    private VideoCoverPicker mCoverPicker;
    private String mVideoPath;
    private VideoCoverShowView mCoverView;
    private long mTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_cover);
        Intent intent = getIntent();
        mVideoPath = intent.getStringExtra("path");
        initViews();
    }

    private void initViews() {
        mCoverPicker = (VideoCoverPicker) findViewById(R.id.coverPicker);
        mCoverPicker.setVideoPath(mVideoPath);
        mCoverPicker.setPickTimeListener(new VideoCoverPicker.PickTimeListener() {
            @Override
            public void onPickTime(long time) {
                mCoverView.seekTo(time);
                mTime = time;
            }
        });
        mCoverView = (VideoCoverShowView) findViewById(R.id.videoCoverView);
        mCoverView.setDataSource(mVideoPath);
    }

    private void handleCompleteClick() {
        Intent intent = new Intent();
        intent.putExtra("coverTime", mTime);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCoverPicker.release();
        mCoverView.release();
    }
}
