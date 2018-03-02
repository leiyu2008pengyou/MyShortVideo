package com.example.leiyu.myshortvideo.videogride.observable;

import com.example.leiyu.myshortvideo.videogride.entity.LocalMedia;
import com.example.leiyu.myshortvideo.entity.LocalMediaFolder;

import java.util.List;

/**
 * Created by leiyu on 2018/3/2.
 */

public interface ObserverListener {
    void observerUpFoldersData(List<LocalMediaFolder> folders);

    void observerUpSelectsData(List<LocalMedia> selectMedias);
}
