package com.example.leiyu.myshortvideo.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by leiyu on 2018/3/1.
 */

public class FileUtil {
    public static final String filePath = "myshortvideo";
    public static final String fileName = "camera-test.mp4";
    public static final String IMAGE_PATH = File.separator + filePath + File.separator + "image" + File.separator;
    public static final String VIDEODIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + filePath + File.separator;
    public static final String videoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + filePath + File.separator + fileName;
    public static final String JPG_SUFFIX = ".jpg";

    public static File createMediaFile(Context context, String parentPath, String fileName) {
        String state = Environment.getExternalStorageState();
        File rootDir = state.equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory() : context.getCacheDir();

        File folderDir = new File(rootDir.getAbsolutePath() + File.separator + parentPath);
        if (!folderDir.exists() && folderDir.mkdirs()) {

        }
        File tmpFile = new File(folderDir, fileName);

        return tmpFile;
    }


}
