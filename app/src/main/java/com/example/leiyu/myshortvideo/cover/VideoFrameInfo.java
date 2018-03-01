package com.example.leiyu.myshortvideo.cover;

import java.io.Serializable;

/**
 * Created by leiyu on 2018/3/1.
 */

public class VideoFrameInfo implements Serializable {
    public String path; //图片的sd卡路径
    public long time;//图片所在视频的时间  毫秒

    public VideoFrameInfo() {
    }


    @Override
    public String toString() {
        return "VideoEditInfo{" +
                "path='" + path + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

}
