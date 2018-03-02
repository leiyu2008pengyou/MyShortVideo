package com.example.leiyu.myshortvideo.videogride.observable;

import com.example.leiyu.myshortvideo.videogride.entity.LocalMedia;
import com.example.leiyu.myshortvideo.entity.LocalMediaFolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by leiyu on 2018/3/2.
 */

public class ImagesObservable implements SubjectListener{

    //观察者接口集合
    private List<ObserverListener> observers = new ArrayList<>();

    private String ugcContent; //保存用户输入的内容
    private List<LocalMedia> medias;//所有图片视频
    private List<LocalMedia> selectedImages;//被选中的图片
    private List<LocalMedia> selectedVideos;//被选中的视频

    private static volatile ImagesObservable sObserver;

    private ImagesObservable() {
        medias = new ArrayList<>();
        selectedImages = new ArrayList<>();
        selectedVideos = new ArrayList<>();
    }

    public static ImagesObservable getInstance() {
        if (sObserver == null) {
            synchronized (ImagesObservable.class) {
                if (sObserver == null) {
                    sObserver = new ImagesObservable();
                }
            }
        }
        return sObserver;
    }


    /**
     * 存储图片
     *
     * @param list list
     */
    public void saveLocalMedia(List<LocalMedia> list) {
        medias = list;
    }


    /**
     * 读取图片
     */
    public List<LocalMedia> readLocalMedias() {
        return medias;
    }

    /**
     * 读取选中的图片
     */
    public List<LocalMedia> readSelectLocalMedias() {
        return selectedImages;
    }

    /***
     * 添加一个选中的图片
     *
     * @param image image
     */
    public void addASelectedImage(LocalMedia image) {
        for (LocalMedia media : selectedImages) {
            if (media.getSourcePath().equals(image.getSourcePath())) {
                return;
            }
        }
        selectedImages.add(image);
    }

    /***
     * 删除一个选中的图片
     *
     * @param image image
     */
    public void removeASelectedImage(LocalMedia image) {
        Iterator<LocalMedia> it = selectedImages.iterator();
        while (it.hasNext()) {
            LocalMedia l = it.next();
            if (l.getSourcePath().equals(image.getSourcePath())) {
                it.remove();
                break;
            }
        }
    }


    /**
     * 读取选中的视频
     */
    public List<LocalMedia> readSelectLocalMediaVideos() {
        return selectedVideos;
    }

    /***
     * 添加一个视频
     *
     * @param image image
     */
    public void addASelectedVideo(LocalMedia image) {
        selectedVideos.add(image);
    }

    /***
     * 删除一个视频
     *
     * @param image image
     */
    public void removeASelectedVideo(LocalMedia image) {
        selectedVideos.remove(image);
    }


    public void clearLocalMedia() {
        if (medias != null)
            medias.clear();
    }

    public void clearSelectedLocalMedia() {
        if (selectedImages != null)
            selectedImages.clear();
    }
    public void clearSelectedVideoLocalMedia() {
        if (selectedVideos != null)
            selectedVideos.clear();
    }

    @Override
    public void add(ObserverListener observerListener) {
        observers.add(observerListener);
    }

    /**
     * 相册所有列表文件夹
     *
     * @param folders
     */
    @Override
    public void notifyFolderObserver(List<LocalMediaFolder> folders) {
        for (ObserverListener observerListener : observers) {
            observerListener.observerUpFoldersData(folders);
        }
    }

    /**
     * 选中图片集合观察者
     *
     * @param selectMedias selectMedias
     */
    @Override
    public void notifySelectLocalMediaObserver(List<LocalMedia> selectMedias) {
        for (ObserverListener observerListener : observers) {
            observerListener.observerUpSelectsData(selectMedias);
        }
    }

    @Override
    public void remove(ObserverListener observerListener) {
        if (observers.contains(observerListener)) {
            observers.remove(observerListener);
        }
    }


    public String getUgcContent() {
        return ugcContent;
    }

    public void setUgcContent(String ugcContent) {
        this.ugcContent = ugcContent;
    }
}
