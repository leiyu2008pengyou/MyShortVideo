package com.example.leiyu.myshortvideo.entity;

import android.media.MediaMetadataRetriever;
import android.text.TextUtils;

import java.io.File;

/**
 * @Title: MediaMetadata
 * @Package com.laifeng.media.media.mp4
 * @Description:
 * @Author Jim
 * @Date 2017/3/7
 * @Time 下午1:20
 * @Version
 */

public class MediaMetadata {
    private MediaMetadataRetriever mMetadataRetriever;
    private int width, height, degree, bitrate;
    private long duration;

    public MediaMetadata(String path) {
        if(TextUtils.isEmpty(path)) {
            return;
        }
        File file = new File(path);
        if(!file.exists()) {
            return;
        }
        mMetadataRetriever = new MediaMetadataRetriever();
        mMetadataRetriever.setDataSource(file.getAbsolutePath());

        String widthString = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        if(!TextUtils.isEmpty(widthString)) {
            width = Integer.valueOf(widthString);
        }

        String heightString = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        if(!TextUtils.isEmpty(heightString)) {
            height = Integer.valueOf(heightString);
        }

        String durationString = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if(!TextUtils.isEmpty(durationString)) {
            duration = Long.valueOf(durationString);
        }

        String bitrateString = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
        if(!TextUtils.isEmpty(bitrateString)) {
            bitrate = Integer.valueOf(bitrateString);
        }

        String degreeStr = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if (!TextUtils.isEmpty(degreeStr)) {
            degree = Integer.valueOf(degreeStr);
        }

        mMetadataRetriever.release();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getDuration() {
        return duration;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getDegree() {
        return degree;
    }
}
