package com.example.leiyu.myshortvideo.videogride.entity;

import java.io.Serializable;

/**
 * Created by leiyu on 2018/3/2.
 */

public class LocalMedia implements Serializable {

    private String sourcePath;//未经转码（特效）的地址
    private String destinationPath;//转码后的
    private String compressPath;
    private String cutPath;
    private long duration;//选取区间
    private long lastUpdateAt;
    private boolean isChecked;
    private boolean isCut;
    public int position;
    private int num;
    private int type;
    private String selectedMusicId;
    private String content;//短视频描述
    private String topicName;//短视频话题
    private String topicId;

    private long videoId;
    private String thumbnailPath;
    private int videoWidth;
    private int videoHeight;
    private long videoLength;//毫秒
    private long fileSize;
    private String effect;
    private String firstFrame;//首帧图片
    private String coverPath;//选取的封面图片
    private long coverTime;

    public long getCoverTime() {
        return coverTime;
    }

    public void setCoverTime(long coverTime) {
        this.coverTime = coverTime;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public void setFirstFrame(String firstFrame) {
        this.firstFrame = firstFrame;
    }

    public String getFirstFrame() {
        return firstFrame;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getEffect() {
        return effect;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setVideoId(long videoId) {
        this.videoId = videoId;
    }

    public long getVideoId() {
        return videoId;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public void setVideoLength(long videoLength) {
        this.videoLength = videoLength;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public long getVideoLength() {
        return videoLength;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getTopicName() {
        return topicName;
    }

    public LocalMedia(String sourcePath, long lastUpdateAt, long duration, int type) {
        this.sourcePath = sourcePath;
        this.duration = duration;
        this.lastUpdateAt = lastUpdateAt;
        this.type = type;
    }

    public LocalMedia(String sourcePath, String content, String topicId, String topicName, String selectedMusicId, long lastUpdateAt, long duration, int type) {
        this.sourcePath = sourcePath;
        this.duration = duration;
        this.lastUpdateAt = lastUpdateAt;
        this.type = type;
        this.selectedMusicId = selectedMusicId;
        this.topicName = topicName;
        this.content = content;
        this.topicId = topicId;
    }

    public LocalMedia(long videoId, String coverPath, long coverTime, int videoWidth, int videoHeight, long fileSize, String sourcePath, String content, String topicId, String topicName, String selectedMusicId, long lastUpdateAt, long duration, int type, String effect) {
        this.sourcePath = sourcePath;
        this.videoLength = duration;
        this.lastUpdateAt = lastUpdateAt;
        this.type = type;
        this.selectedMusicId = selectedMusicId;
        this.topicName = topicName;
        this.content = content;
        this.topicId = topicId;
        this.videoId = videoId;
        this.coverPath = coverPath;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.fileSize = fileSize;
        this.effect = effect;
        this.coverTime=coverTime;
    }

    public LocalMedia(String sourcePath, long duration, long lastUpdateAt, boolean isChecked, int position, int num, int type) {
        this.sourcePath = sourcePath;
        this.duration = duration;
        this.lastUpdateAt = lastUpdateAt;
        this.isChecked = isChecked;
        this.position = position;
        this.num = num;
        this.type = type;
    }

    public LocalMedia() {
    }

    public String getCutPath() {
        return cutPath;
    }

    public void setCutPath(String cutPath) {
        this.cutPath = cutPath;
    }

    public boolean isCut() {
        return isCut;
    }

    public void setCut(boolean cut) {
        isCut = cut;
    }


    public String getCompressPath() {
        return compressPath;
    }

    public void setCompressPath(String compressPath) {
        this.compressPath = compressPath;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getLastUpdateAt() {
        return lastUpdateAt;
    }

    public void setLastUpdateAt(long lastUpdateAt) {
        this.lastUpdateAt = lastUpdateAt;
    }

    public boolean getIsChecked() {
        return this.isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getSelectedMusicId() {
        return selectedMusicId == null ? "0" : selectedMusicId;
    }

    public void setSelectedMusicId(String selectedMusicId) {
        this.selectedMusicId = selectedMusicId;
    }

    @Override
    public String toString() {
        return "LocalMedia{" +
                "sourcePath='" + sourcePath + '\'' +
                ", compressPath='" + compressPath + '\'' +
                ", cutPath='" + cutPath + '\'' +
                ", duration=" + duration +
                ", lastUpdateAt=" + lastUpdateAt +
                ", isChecked=" + isChecked +
                ", isCut=" + isCut +
                ", position=" + position +
                ", num=" + num +
                ", type=" + type +
                ", selectedMusicId=" + selectedMusicId +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        LocalMedia other = (LocalMedia) obj;
        if (this == other) {
            return true;
        }
        if (sourcePath.equals(other.sourcePath)) {
            return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hashCode = System.identityHashCode(sourcePath);
        return hashCode;
    }
}
