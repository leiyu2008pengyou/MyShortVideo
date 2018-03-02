package com.example.leiyu.myshortvideo.videogride.observable;

import com.example.leiyu.myshortvideo.videogride.entity.LocalMedia;
import com.example.leiyu.myshortvideo.entity.LocalMediaFolder;

import java.util.List;

/**
 * Created by leiyu on 2018/3/2.
 */

public interface SubjectListener {
    void add(ObserverListener observerListener);

    void notifyFolderObserver(List<LocalMediaFolder> folders);

    void notifySelectLocalMediaObserver(List<LocalMedia> medias);

    void remove(ObserverListener observerListener);
}
