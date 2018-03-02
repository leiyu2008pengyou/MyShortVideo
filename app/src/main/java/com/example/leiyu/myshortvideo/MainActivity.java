package com.example.leiyu.myshortvideo;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.leiyu.myshortvideo.Permission.PermissionChecker;
import com.example.leiyu.myshortvideo.cameracapture.CameraCaptureActivity;
import com.example.leiyu.myshortvideo.cover.VideoCoverActivity;
import com.example.leiyu.myshortvideo.videogride.activity.VideoGridActivity;
import com.example.leiyu.myshortvideo.videogride.entity.FunctionConfig;
import com.example.leiyu.myshortvideo.entity.LocalMediaLoader;
import com.example.leiyu.myshortvideo.utils.FileUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private FunctionConfig mConfig;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        setContentView(R.layout.activity_main);

        mConfig = new FunctionConfig();
        mConfig.setSelectVideoMaxDuration(FunctionConfig.DEFAULT_SELECT_VIDEO_MAX_DURATION);
        mConfig.setSelectVideoMinDuration(FunctionConfig.DEFAULT_SELECT_VIDEO_MIN_DURATION);
        mConfig.setCutVideoMaxDuration(FunctionConfig.DEFAULT_CUT_VIDEO_MAX_DURATION);
        mConfig.setCutVideoMinDuration(FunctionConfig.DEFAULT_CUT_VIDEO_MIN_DURATION);
    }

    public void checkPermission(View view){
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            Toast.makeText(this, "Some permissions is not approved !!!", Toast.LENGTH_SHORT).show();
        } else {
            Intent broadcastIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(new File(FileUtil.VIDEODIR));
            broadcastIntent.setData(uri);
            sendBroadcast(broadcastIntent);

            if(view.getId() == R.id.livingcamerabutton){
                Intent intent = new Intent(this, LiveCameraActivity.class);
                startActivity(intent);
            }else if(view.getId() == R.id.cameracapturebutton){
                Intent intent = new Intent(this, CameraCaptureActivity.class);
                startActivity(intent);
            }else if(view.getId() == R.id.vieocoverbutton){
                File file = new File(FileUtil.videoPath);
                if(file.exists()){
                    Intent intent = new Intent(this, VideoCoverActivity.class);
                    intent.putExtra("path", FileUtil.videoPath);
                    startActivity(intent);
                }else{
                    Toast.makeText(MainActivity.this, "视频文件不存在请先录制",Toast.LENGTH_LONG).show();
                }
            }else if(view.getId() == R.id.vieocoverbutton2){
                Intent intent = new Intent(this, VideoGridActivity.class);
                mConfig.setType(LocalMediaLoader.TYPE_VIDEO);
                mConfig.setSelectMode(FunctionConfig.MODE_SINGLE);
                mConfig.setShowCamera(false);
                mConfig.setEnablePreview(true);
                intent.putExtra(FunctionConfig.EXTRA_THIS_CONFIG, mConfig);
                startActivity(intent);
            }
        }
    }
}
