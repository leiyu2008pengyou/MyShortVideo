package com.example.leiyu.myshortvideo.entity;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.example.leiyu.myshortvideo.videogride.entity.LocalMedia;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by leiyu on 2018/3/2.
 */

public class LocalMediaLoader {

    // load type
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_IMAGE_VIDEO = 3;

    private final static String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media._ID
    };

    private final static String[] VIDEO_PROJECTION = {
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DURATION,
    };
    private int type = TYPE_IMAGE;
    private FragmentActivity activity;

    private ArrayList<LocalMediaFolder> allImageFoldersForAll = new ArrayList<>();

    public LocalMediaLoader(FragmentActivity activity, int type) {
        this.activity = activity;
        this.type = type;
    }

    public void loadAllImage(final LocalMediaLoadListener imageLoadListener) {
        if (type == TYPE_IMAGE || type == TYPE_VIDEO) {
            loadImageOrVideo(imageLoadListener);
        } else if (type == TYPE_IMAGE_VIDEO) {
            loadAllImageVideo(imageLoadListener);
        }
    }

    private void loadImageOrVideo(final LocalMediaLoadListener imageLoadListener) {
        activity.getSupportLoaderManager().initLoader(type, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            CursorLoader cursorLoader = null;

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                if (id == TYPE_IMAGE) {
                    cursorLoader = new CursorLoader(
                            activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            IMAGE_PROJECTION, MediaStore.Images.Media.MIME_TYPE + "=? or "
                            + MediaStore.Images.Media.MIME_TYPE + "=?" + " or "
                            + MediaStore.Images.Media.MIME_TYPE + "=?",
                            new String[]{"image/jpeg", "image/png", "image/gif"}, IMAGE_PROJECTION[2] + " DESC");
                } else if (id == TYPE_VIDEO) {
                    cursorLoader = new CursorLoader(
                            activity, MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            VIDEO_PROJECTION, MediaStore.Images.Media.MIME_TYPE + "=?", new String[]{"video/mp4"}, VIDEO_PROJECTION[2] + " DESC");
                }
                return cursorLoader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                try {
                    ArrayList<LocalMediaFolder> allImageFolders = new ArrayList<>();
                    if (cursor != null) {
                        int count = cursor.getCount();
                        if (count > 0) {
                            cursor.moveToFirst();
                            do {
                                String path = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                                // 如原图路径不存在或者路径存在但文件不存在,就结束当前循环
                                if (TextUtils.isEmpty(path) || !new File(path).exists()) {
                                    continue;
                                }
                                long dateTime = cursor.getLong(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                                int duration = (type == TYPE_VIDEO ? cursor.getInt(cursor.getColumnIndexOrThrow(VIDEO_PROJECTION[4])) : 0);
                                LocalMedia image = new LocalMedia(path, dateTime, duration, type);
                                LocalMediaFolder folder = getImageFolder(path, allImageFolders);
                                folder.getImages().add(image);
                                folder.setType(type);
                                folder.setImageNum(folder.getImageNum() + 1);
                            } while (cursor.moveToNext());

                            if (allImageFolders.size() > 0) {
                                LocalMediaFolder folder = new LocalMediaFolder();
                                if (type == TYPE_IMAGE) {
                                    folder.setName("全部图片");
                                    folder.setType(TYPE_IMAGE);
                                } else if (type == TYPE_VIDEO) {
                                    folder.setName("全部视频");
                                    folder.setType(TYPE_VIDEO);
                                }
                                for (int i = 0; i < allImageFolders.size(); i++) {
                                    folder.getImages().addAll(allImageFolders.get(i).getImages());
                                }
                                folder.setImageNum(folder.getImages().size());
                                folder.setFirstImagePath(folder.getImages().get(0).getSourcePath());
                                File imageFile = new File(folder.getFirstImagePath());
                                File folderFile = imageFile.getParentFile();
                                folder.setPath(folderFile.getAbsolutePath());
                                allImageFolders.add(0, folder);
                            }

                            imageLoadListener.loadComplete(allImageFolders);
                            cursor.close();
                        } else {
                            // 如果没有相册
                            imageLoadListener.loadComplete(allImageFolders);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });
    }

    private void loadAllImageVideo(final LocalMediaLoadListener imageLoadListener) {
        activity.getSupportLoaderManager().initLoader(TYPE_IMAGE, null, new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                CursorLoader cursorLoader;
                cursorLoader = new CursorLoader(
                        activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        IMAGE_PROJECTION, MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?" + " or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png", "image/gif"}, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                try {
                    if (cursor != null) {
                        int count = cursor.getCount();
                        if (count > 0) {
                            cursor.moveToFirst();
                            do {
                                String path = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                                // 如原图路径不存在或者路径存在但文件不存在,就结束当前循环
                                if (TextUtils.isEmpty(path) || !new File(path).exists()) {
                                    continue;
                                }
                                long dateTime = cursor.getLong(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                                LocalMedia image = new LocalMedia(path, dateTime, 0, TYPE_IMAGE);
                                LocalMediaFolder folder = getImageFolder(path, allImageFoldersForAll);
                                folder.getImages().add(image);
                                folder.setType(TYPE_IMAGE_VIDEO);
                                folder.setImageNum(folder.getImageNum() + 1);
                            } while (cursor.moveToNext());
                            cursor.close();
                            loadAllVideo(imageLoadListener);
                        } else {
                            // 如果没有相册
                            loadAllVideo(imageLoadListener);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });
    }

    private void loadAllVideo(final LocalMediaLoadListener imageLoadListener) {
        activity.getSupportLoaderManager().initLoader(TYPE_VIDEO, null, new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                CursorLoader cursorLoader;
                cursorLoader = new CursorLoader(
                        activity, MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        VIDEO_PROJECTION, MediaStore.Images.Media.MIME_TYPE + "=?", new String[]{"video/mp4"}, VIDEO_PROJECTION[2] + " DESC");
                return cursorLoader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                try {
                    if (cursor != null) {
                        int count = cursor.getCount();
                        if (count > 0) {
                            cursor.moveToFirst();
                            do {
                                String path = cursor.getString(cursor.getColumnIndexOrThrow(VIDEO_PROJECTION[0]));
                                // 如原图路径不存在或者路径存在但文件不存在,就结束当前循环
                                if (TextUtils.isEmpty(path) || !new File(path).exists()) {
                                    continue;
                                }
                                long dateTime = cursor.getLong(cursor.getColumnIndexOrThrow(VIDEO_PROJECTION[2]));
                                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(VIDEO_PROJECTION[4]));
                                LocalMedia image = new LocalMedia(path, dateTime, duration, TYPE_VIDEO);
                                LocalMediaFolder folder = getImageFolder(path, allImageFoldersForAll);
                                folder.getImages().add(image);
                                folder.setType(TYPE_IMAGE_VIDEO);
                                folder.setImageNum(folder.getImageNum() + 1);
                            } while (cursor.moveToNext());
                            if (allImageFoldersForAll.size() > 0) {
                                LocalMediaFolder folder = new LocalMediaFolder();
                                folder.setName("全部");
                                for (int i = 0; i < allImageFoldersForAll.size(); i++) {
                                    folder.getImages().addAll(allImageFoldersForAll.get(i).getImages());
                                }
                                //排序
                                Collections.sort(folder.getImages(), new Comparator<LocalMedia>() {
                                    @Override
                                    public int compare(LocalMedia o1, LocalMedia o2) {
                                        return (int) (o2.getLastUpdateAt()-o1.getLastUpdateAt());
                                    }
                                });

                                folder.setType(TYPE_IMAGE_VIDEO);
                                folder.setImageNum(folder.getImages().size());
                                folder.setFirstImagePath(folder.getImages().get(0).getSourcePath());
                                File imageFile = new File(folder.getFirstImagePath());
                                File folderFile = imageFile.getParentFile();
                                folder.setPath(folderFile.getAbsolutePath());
                                allImageFoldersForAll.add(0, folder);
                            }
                            cursor.close();
                            imageLoadListener.loadComplete(allImageFoldersForAll);
                        } else {
                            // 如果没有相册
                            if (allImageFoldersForAll.size() > 0) {
                                LocalMediaFolder folder = new LocalMediaFolder();
                                folder.setName("全部");
                                for (int i = 0; i < allImageFoldersForAll.size(); i++) {
                                    folder.getImages().addAll(allImageFoldersForAll.get(i).getImages());
                                }
                                folder.setType(TYPE_IMAGE_VIDEO);
                                folder.setImageNum(folder.getImages().size());
                                folder.setFirstImagePath(folder.getImages().get(0).getSourcePath());
                                File imageFile = new File(folder.getFirstImagePath());
                                File folderFile = imageFile.getParentFile();
                                folder.setPath(folderFile.getAbsolutePath());
                                allImageFoldersForAll.add(0, folder);
                            }
                            imageLoadListener.loadComplete(allImageFoldersForAll);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });
    }


    public static LocalMediaFolder getImageFolder(String path, List<LocalMediaFolder> imageFolders) {
        File imageFile = new File(path);
        File folderFile = imageFile.getParentFile();

        for (LocalMediaFolder folder : imageFolders) {
            if (folder.getName().equals(folderFile.getName())) {
                return folder;
            }
        }
        LocalMediaFolder newFolder = new LocalMediaFolder();
        newFolder.setName(folderFile.getName());
        newFolder.setPath(folderFile.getAbsolutePath());
        newFolder.setFirstImagePath(path);
        imageFolders.add(newFolder);
        return newFolder;
    }

    public interface LocalMediaLoadListener {
        void loadComplete(List<LocalMediaFolder> folders);
    }
}
