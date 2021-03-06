package com.example.leiyu.myshortvideo.videoedit.entity;

import java.io.Serializable;

/**
 * Created by leiyu on 2018/3/2.
 */

public class VideoEditInfo implements Serializable {

    public String path; //图片的sd卡路径
    public long time;//图片所在视频的时间  毫秒

    public VideoEditInfo() {
    }


    @Override
    public String toString() {
        return "VideoEditInfo{" +
                "path='" + path + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
