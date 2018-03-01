package com.example.leiyu.myshortvideo;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.leiyu.myshortvideo.Permission.PermissionChecker;
import com.example.leiyu.myshortvideo.cameracapture.CameraCaptureActivity;
import com.example.leiyu.myshortvideo.cover.VideoCoverActivity;
import com.example.leiyu.myshortvideo.utils.FileUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        setContentView(R.layout.activity_main);
    }

    public void checkPermission(View view){
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            Toast.makeText(this, "Some permissions is not approved !!!", Toast.LENGTH_SHORT).show();
        } else {
            if(view.getId() == R.id.livingcamerabutton){
                Intent intent = new Intent(this, LiveCameraActivity.class);
                startActivity(intent);
            }else if(view.getId() == R.id.cameracapturebutton){
                Intent intent = new Intent(this, CameraCaptureActivity.class);
                startActivity(intent);
            }else if(view.getId() == R.id.vieocoverbutton){
                Intent intent = new Intent(this, VideoCoverActivity.class);
                intent.putExtra("path", FileUtil.videoPath);
                startActivity(intent);
            }
        }
    }
}
