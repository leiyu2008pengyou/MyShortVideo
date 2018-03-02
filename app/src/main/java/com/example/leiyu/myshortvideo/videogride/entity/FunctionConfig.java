package com.example.leiyu.myshortvideo.videogride.entity;

import com.example.leiyu.myshortvideo.R;

import java.io.Serializable;

/**
 * Created by leiyu on 2018/3/2.
 */

public class FunctionConfig implements Serializable {
    public static final String EXTRA_THIS_CONFIG = "function_config";
    //退出9宫格的action
    public static final String EXTRA_FINISH_ACTIVITY = "lf.app.ImageGridActivity.finish";
    //将视频的item全部置灰的action
    public static final String EXTRA_SET_VIDEO_DIS_ENABLE_ACTIVITY = "lf.app.ImageGridActivity.video.dis.enable";
    public static final String BUNDLE_CAMERA_PATH = "CameraPath";
    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_PREVIEW_MODE = "previewMode";

    public static final int READ_EXTERNAL_STORAGE = 0x01;
    public static final int CAMERA = 0x02;
    public final static int REQUEST_CAMERA = 99;


    // 预览
    public static int MODE_ALL_VALUE = 100;
    public static int MODE_SELECTED_VALUE = 101;

    public final static int MODE_MULTIPLE = 1;// 多选
    public final static int MODE_SINGLE = 2;// 单选
    private int type = 1; // 获取相册类型; 1 图片 2 视频
    public static final int SELECT_MAX_NUM = 9;

    private int maxSelectNum = SELECT_MAX_NUM; // 多选最大可选数量
    private int selectMode = MODE_MULTIPLE; // 单选 or 多选
    private boolean isShowCamera = true; // 是否显示相机
    private boolean enablePreview = true; // 是否预览图片
    private boolean enableCrop; // 是否裁剪图片，只针对单选图片有效
    private boolean isPreviewVideo; // 是否可预览视频(播放)
    private int imageSpanCount = 4; // 列表每行显示个数
    private int checkedBoxDrawable = R.drawable.lf_ugc_publish_checkbox_selector;// 图片选择默认样式

    private String publishGo;//发布成功后跳转到哪里
    private boolean isCheckNumMode = false;// 是否显示数字选择图片

    //default
    public static final long DEFAULT_SELECT_VIDEO_MAX_DURATION = 5 * 60 * 1000L;
    public static final long DEFAULT_SELECT_VIDEO_MIN_DURATION = 3 * 1000L;
    public static final long DEFAULT_CUT_VIDEO_MAX_DURATION = 60 * 1000L;
    public static final long DEFAULT_CUT_VIDEO_MIN_DURATION = 3 * 1000L;
    private long selectVideoMaxDuration = DEFAULT_SELECT_VIDEO_MAX_DURATION;//最长选择时间,相册选择里,ms
    private long selectVideoMinDuration = DEFAULT_SELECT_VIDEO_MIN_DURATION;//最短选择时间，相册选择里,ms
    private long cutVideoMaxDuration = DEFAULT_CUT_VIDEO_MAX_DURATION;//最长剪切时间,ms
    private long cutVideoMinDuration = DEFAULT_CUT_VIDEO_MIN_DURATION;//最短剪切时间,ms

    public boolean isPreviewVideo() {
        return isPreviewVideo;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public int getMaxSelectNum() {
        return maxSelectNum;
    }

    public void setMaxSelectNum(int maxSelectNum) {
        this.maxSelectNum = maxSelectNum;
    }

    public int getSelectMode() {
        return selectMode;
    }

    public void setSelectMode(int selectMode) {
        this.selectMode = selectMode;
    }

    public boolean isShowCamera() {
        return isShowCamera;
    }

    public void setShowCamera(boolean showCamera) {
        isShowCamera = showCamera;
    }

    public boolean isEnablePreview() {
        return enablePreview;
    }

    public void setEnablePreview(boolean enablePreview) {
        this.enablePreview = enablePreview;
    }


    public int getImageSpanCount() {
        return imageSpanCount;
    }

    public void setImageSpanCount(int imageSpanCount) {
        this.imageSpanCount = imageSpanCount;
    }

    public int getCheckedBoxDrawable() {
        return checkedBoxDrawable;
    }

    public void setCheckedBoxDrawable(int checkedBoxDrawable) {
        this.checkedBoxDrawable = checkedBoxDrawable;
    }

    public boolean isCheckNumMode() {
        return isCheckNumMode;
    }

    public void setCheckNumMode(boolean checkNumMode) {
        isCheckNumMode = checkNumMode;
    }

    public String getPublishGo() {
        return publishGo;
    }

    public void setPublishGo(String publishGo) {
        this.publishGo = publishGo;
    }

    public long getSelectVideoMaxDuration() {
        return selectVideoMaxDuration;
    }

    public void setSelectVideoMaxDuration(long selectVideoMaxDuration) {
        this.selectVideoMaxDuration = selectVideoMaxDuration;
    }

    public long getSelectVideoMinDuration() {
        return selectVideoMinDuration;
    }

    public void setSelectVideoMinDuration(long selectVideoMinDuration) {
        this.selectVideoMinDuration = selectVideoMinDuration;
    }

    public long getCutVideoMaxDuration() {
        return cutVideoMaxDuration;
    }

    public void setCutVideoMaxDuration(long cutVideoMaxDuration) {
        this.cutVideoMaxDuration = cutVideoMaxDuration;
    }

    public long getCutVideoMinDuration() {
        return cutVideoMinDuration;
    }

    public void setCutVideoMinDuration(long cutVideoMinDuration) {
        this.cutVideoMinDuration = cutVideoMinDuration;
    }
}
