package com.example.leiyu.myshortvideo.cover;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.example.leiyu.myshortvideo.entity.MediaMetadata;

/**
 * Created by leiyu on 2018/3/1.
 */

public class VideoCoverShowView extends LinearLayout implements ViewTreeObserver.OnGlobalLayoutListener{
    private static final int MESSAGE_SEEK = 1;

    private Context mContext;
    private VideoCoverShowSurface mShowSurface;
    private Surface mSurface;

    private int mMaxWidth;
    private int mMaxHeight;
    private int mVideoWidth;
    private int mVideoHeight;

    private String mFilePath;
    private VideoCoverShowDecoder mDecoder;
    private HandlerThread mSeekThread;
    private Handler mSeekHandler;
    private long mTimeMs = -100;

    public VideoCoverShowView(Context context) {
        this(context, null);
    }

    public VideoCoverShowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoCoverShowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        mShowSurface = new VideoCoverShowSurface(mContext);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT);
        mShowSurface.setLayoutParams(layoutParams);
        mShowSurface.setSurfaceListener(mSurfaceListener);
        addView(mShowSurface);
        mSeekThread = new HandlerThread("SeekThread");
        mSeekThread.start();
        mSeekHandler = new Handler(mSeekThread.getLooper(), mHandlerCallback);
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    private Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == MESSAGE_SEEK) {
                long time = (long)msg.obj;
                seekToAsync(time);
            }
            return false;
        }
    };

    private VideoCoverSurfaceListener mSurfaceListener = new VideoCoverSurfaceListener() {
        @Override
        public void OnSurfaceCreated(Surface surface) {
            mSurface = surface;
            post(new Runnable() {
                @Override
                public void run() {
                    initDecoder();
                }
            });
        }
    };

    public void setDataSource(String filePath) {
        mFilePath = filePath;
        MediaMetadata metadata = new MediaMetadata(filePath);
        int degree = metadata.getDegree();
        if(degree == 90 || degree == 270) {
            mVideoWidth = metadata.getHeight();
            mVideoHeight = metadata.getWidth();
        } else {
            mVideoWidth = metadata.getWidth();
            mVideoHeight = metadata.getHeight();
        }
    }

    private void initDecoder() {
        mDecoder = new VideoCoverShowDecoder(mFilePath, mSurface);
        seekTo(0);
    }

    public void seekTo(long timeMs) {
        mSeekHandler.removeCallbacksAndMessages(null);
        Message msg = mSeekHandler.obtainMessage();
        msg.what = MESSAGE_SEEK;
        msg.obj = timeMs;
        mSeekHandler.sendMessage(msg);
    }

    private void seekToAsync(long timeMs) {
        if(Math.abs(timeMs - mTimeMs) < 10) {
            return;
        }
        mDecoder.seekTo(timeMs);
        mShowSurface.updateRender();
        mTimeMs = timeMs;
    }

    public long getTime() {
        return mTimeMs;
    }

    private void transWrapType() {
        LayoutParams layoutParams = (LayoutParams) mShowSurface.getLayoutParams();
        int leftMargin = 0, topMargin = 0;
        float widthRatio = (float) mMaxWidth / (float) mVideoWidth;
        float heightRatio = (float) mMaxHeight / (float) mVideoHeight;
        if (widthRatio == heightRatio) {
            layoutParams.width = mMaxWidth;
            layoutParams.height = mMaxHeight;
        } else if (widthRatio > heightRatio) {
            int finalWidth = (int) (mVideoWidth * heightRatio);
            layoutParams.width = finalWidth;
            layoutParams.height = mMaxHeight;
            leftMargin = (mMaxWidth - finalWidth) / 2;
        } else {
            int finalHeight = (int) (mVideoHeight * widthRatio);
            layoutParams.width = mMaxWidth;
            layoutParams.height = finalHeight;
            topMargin = (mMaxHeight - finalHeight) / 2;
        }
        layoutParams.leftMargin = leftMargin;
        layoutParams.topMargin = topMargin;
        mShowSurface.setLayoutParams(layoutParams);
    }

    public void release() {
        if(mDecoder != null) {
            mDecoder.release();
        }
        if(mSeekHandler != null) {
            mSeekHandler.removeCallbacksAndMessages(null);
        }
        if(mSeekThread != null) {
            mSeekThread.quit();
        }
    }

    @Override
    public void onGlobalLayout() {
        mMaxWidth = getWidth();
        mMaxHeight = getHeight();
        transWrapType();
    }
}
