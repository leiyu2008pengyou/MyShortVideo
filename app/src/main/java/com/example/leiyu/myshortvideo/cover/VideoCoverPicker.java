package com.example.leiyu.myshortvideo.cover;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.support.v7.widget.RecyclerView;
import com.example.leiyu.myshortvideo.R;
import com.example.leiyu.myshortvideo.utils.DisplayUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by leiyu on 2018/3/1.
 */

public class VideoCoverPicker extends RelativeLayout {
    private static final String EDIT_PATH = "/" + "SopCast" + "/EditVideo/";
    private static final int MSG_SAVE_SUCCESS = 0;

    private Context mContext;
    private RecyclerView mRecyclerView;
    private VideoFrameAdapter mAdapter;
    private FrameUpdateHandler mFrameUpdateHandler;
    private int mItemWidth;
    private int mItemHeight;
    private LinearLayout mImageCover;
    private LinearLayout mChooseBg;
    private ViewDragHelper mDragHelper;
    private PickTimeListener mListener;
    private String mFramesPath;
    private long mDuration;
    private VideoFrameLoader mFrameLoader;

    public interface PickTimeListener {
        void onPickTime(long time);
    }

    public VideoCoverPicker(Context context) {
        this(context, null);
    }

    public VideoCoverPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoCoverPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    public void setPickTimeListener(PickTimeListener listener) {
        mListener = listener;
    }

    private void init() {
        mItemWidth = (DisplayUtil.getScreenWidth(mContext) - DisplayUtil.dip2px(mContext, 76)) / 10;
        mItemHeight = mItemWidth * 16 / 9;
        View.inflate(mContext, R.layout.lf_layout_cover_picker, this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mImageCover = (LinearLayout) findViewById(R.id.imageCover);

        LinearLayout.LayoutParams coverParams = (LinearLayout.LayoutParams) mImageCover.getLayoutParams();
        coverParams.width = mItemWidth;
        coverParams.height = mItemHeight;
        mImageCover.setLayoutParams(coverParams);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new VideoFrameAdapter(mContext, mItemWidth, mItemHeight);
        mRecyclerView.setAdapter(mAdapter);
        mFrameUpdateHandler = new FrameUpdateHandler(this);

        mChooseBg = (LinearLayout) findViewById(R.id.chooseBg);
        mDragHelper = ViewDragHelper.create(this, 1, mDragCallback);
    }

    private ViewDragHelper.Callback mDragCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View view, int i) {
            if(view == mChooseBg) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx)
        {
            int leftBound = getPaddingLeft();
            int rightBound = getWidth() - mChooseBg.getWidth() - leftBound;
            return Math.min(Math.max(left, leftBound), rightBound);
        }

        @Override
        public int getViewHorizontalDragRange(View child)
        {
            return getMeasuredWidth()-child.getMeasuredWidth();
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            long pickTime = left * mDuration / (getWidth() - mItemWidth);
            if(mListener != null) {
                mListener.onPickTime(pickTime);
            }
        }
    };

    public void setVideoPath(String videoPath) {
        mFramesPath = getSaveEditThumbnailDir(mContext);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        retriever.release();
        mDuration = TextUtils.isEmpty(duration) ? 0 : Long.valueOf(duration);
        mFrameLoader = new VideoFrameLoader(videoPath, 10, mItemWidth, mDuration, mFrameListener);
        mFrameLoader.start();
    }

    private VideoFrameLoader.VideoFrameListener mFrameListener = new VideoFrameLoader.VideoFrameListener() {
        @Override
        public void onFrame(Bitmap bitmap, long time, int index) {
            String path = saveImageToSD(bitmap, mFramesPath, time);
            VideoFrameInfo frameInfo = new VideoFrameInfo();
            frameInfo.path = path;
            frameInfo.time = time;
            Message msg = mFrameUpdateHandler.obtainMessage(MSG_SAVE_SUCCESS);
            msg.obj = frameInfo;
            mFrameUpdateHandler.sendMessage(msg);
        }

        @Override
        public void onFail() {

        }
    };

    public static String saveImageToSD(Bitmap bmp, String dirPath, long time) {
        if (bmp == null) {
            return "";
        }
        File appDir = new File(dirPath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + "_" + time + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    private static String getSaveEditThumbnailDir(Context context) {
        String state = Environment.getExternalStorageState();
        File rootDir = state.equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory() : context.getCacheDir();
        File folderDir = new File(rootDir.getAbsolutePath() + EDIT_PATH);
        if (!folderDir.exists() && folderDir.mkdirs()) {

        }
        return folderDir.getAbsolutePath();
    }

    private static class FrameUpdateHandler extends Handler {
        private final WeakReference<VideoCoverPicker> mPicker;

        FrameUpdateHandler(VideoCoverPicker picker) {
            mPicker = new WeakReference<>(picker);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoCoverPicker picker = mPicker.get();
            if (picker != null) {
                if (msg.what == MSG_SAVE_SUCCESS) {
                    if (picker.mAdapter != null) {
                        VideoFrameInfo info = (VideoFrameInfo) msg.obj;
                        picker.mAdapter.addItemVideoInfo(info);
                    }
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event)
    {
        return mDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    public void release() {
        mFrameLoader.stopLoad();
    }
}
