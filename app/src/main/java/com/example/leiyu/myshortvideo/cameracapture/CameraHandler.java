package com.example.leiyu.myshortvideo.cameracapture;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by leiyu on 2018/2/28.
 * 如果想要开预览时由Activity发起，则要GLSurfaceView利用Handler将创建的SurfaceTexture传递给Activity
 */

public class CameraHandler extends Handler {
    private static final String TAG = "CameraHandler";
    public static final int MSG_SET_SURFACE_TEXTURE = 0;
    private Activity activity;

    public CameraHandler(Activity activity){
        this.activity = activity;
    }

    @Override  // runs on UI thread
    public void handleMessage(Message inputMessage) {
        int what = inputMessage.what;
        Log.d(TAG, "CameraHandler [" + this + "]: what=" + what);

        if (activity == null) {
            Log.w(TAG, "CameraHandler.handleMessage: activity is null");
            return;
        }

        switch (what) {
            case MSG_SET_SURFACE_TEXTURE:
                ((CameraCaptureActivity)activity).handleSetSurfaceTexture((SurfaceTexture) inputMessage.obj);
                break;
            default:
                throw new RuntimeException("unknown msg " + what);
        }
    }


}
