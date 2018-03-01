package com.example.leiyu.myshortvideo.cameracapture;

import android.app.Activity;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.leiyu.myshortvideo.R;
import com.example.leiyu.myshortvideo.camera.CameraConfiguration;
import com.example.leiyu.myshortvideo.camera.CameraHolder;
import com.example.leiyu.myshortvideo.camera.CameraListener;
import com.example.leiyu.myshortvideo.camera.CameraUtils;
import com.example.leiyu.myshortvideo.camera.exception.CameraDisabledException;
import com.example.leiyu.myshortvideo.camera.exception.CameraHardwareException;
import com.example.leiyu.myshortvideo.camera.exception.CameraNotSupportException;
import com.example.leiyu.myshortvideo.camera.exception.NoCameraException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by leiyu on 2018/2/28.
 */

public class CameraCaptureActivity extends Activity implements SurfaceTexture.OnFrameAvailableListener{

    private static final String TAG = "CameraCaptureActivity";
    private GLSurfaceView mGLView;
    private CameraSurfaceRenderer mRenderer;
    //private Camera mCamera;
    private CameraHandler mCameraHandler;
    private boolean mRecordingEnabled;
    private int mCameraPreviewWidth, mCameraPreviewHeight;

    // this is static so it survives activity restarts
    private static TextureMovieEncoder sVideoEncoder = new TextureMovieEncoder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_capture);

        File outputFile = createMediaFile(this, "myshortvideo", "camera-test.mp4");

        TextView fileText = (TextView) findViewById(R.id.cameraOutputFile_text);
        fileText.setText(outputFile.toString());
        mCameraHandler = new CameraHandler(this);
        mRecordingEnabled = sVideoEncoder.isRecording();

        mGLView = (GLSurfaceView) findViewById(R.id.cameraPreview_surfaceView);
        mGLView.setEGLContextClientVersion(2);     // select GLES 2.0
        mRenderer = new CameraSurfaceRenderer(mCameraHandler, sVideoEncoder, outputFile);
        mGLView.setRenderer(mRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        CameraConfiguration cameraConfiguration = new CameraConfiguration.Builder()
                .setFacing(CameraConfiguration.Facing.BACK)
                .setFocusMode(CameraConfiguration.FocusMode.AUTO)
                .build();
        mCameraPreviewWidth = CameraConfiguration.DEFAULT_WIDTH;
        mCameraPreviewHeight = CameraConfiguration.DEFAULT_HEIGHT;
        CameraHolder.instance().setConfiguration(cameraConfiguration);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume -- acquiring camera");
        super.onResume();
        updateControls();

        startPreview();
        mGLView.onResume();
        mGLView.queueEvent(new Runnable() {
            @Override public void run() {
                mRenderer.setCameraPreviewSize(mCameraPreviewWidth, mCameraPreviewHeight);
            }
        });
        Log.d(TAG, "onResume complete: " + this);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause -- releasing camera");
        super.onPause();
        CameraHolder.instance().releaseCamera();
        mGLView.queueEvent(new Runnable() {
            @Override public void run() {
                // Tell the renderer that it's about to be paused so it can clean up.
                mRenderer.notifyPausing();
            }
        });
        mGLView.onPause();
        Log.d(TAG, "onPause complete");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        CameraHolder.instance().stopPreview();
        CameraHolder.instance().releaseCamera();
    }

    /**
     * Connects the SurfaceTexture to the Camera preview output, and starts the preview.
     */
//    private void handleSetSurfaceTexture(SurfaceTexture st) {
//        st.setOnFrameAvailableListener(this);
//        try {
//            mCamera.setPreviewTexture(st);
//        } catch (IOException ioe) {
//            throw new RuntimeException(ioe);
//        }
//        mCamera.startPreview();
//    }

    private void updateControls() {
        Button toggleRelease = (Button) findViewById(R.id.toggleRecording_button);
        String id = mRecordingEnabled ?
                "Stop recording" : "Start recording";
        toggleRelease.setText(id);
    }

    private void startPreview() {
        int result = 0;
        try {
            CameraUtils.checkCameraService(this);
        } catch (CameraDisabledException e) {
            result = CameraListener.CAMERA_DISABLED;
            e.printStackTrace();
        } catch (NoCameraException e) {
            result = CameraListener.NO_CAMERA;
            e.printStackTrace();
        }
        if(result == 0) {
            CameraHolder.State state = CameraHolder.instance().getState();
            if (state != CameraHolder.State.PREVIEW) {
                try {
                    CameraHolder.instance().openCamera();
                } catch (CameraHardwareException e) {
                    result = CameraListener.CAMERA_OPEN_FAILED;
                    e.printStackTrace();
                } catch (CameraNotSupportException e) {
                    result = CameraListener.CAMERA_NOT_SUPPORT;
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * onClick handler for "record" button.
     */
    public void clickToggleRecording(@SuppressWarnings("unused") View unused) {
        mRecordingEnabled = !mRecordingEnabled;
        mGLView.queueEvent(new Runnable() {
            @Override public void run() {
                // notify the renderer that we want to change the encoder's state
                mRenderer.changeRecordingState(mRecordingEnabled);
            }
        });
        updateControls();
    }

    /**
     * Connects the SurfaceTexture to the Camera preview output, and starts the preview.
     */
    public void handleSetSurfaceTexture(SurfaceTexture st) {
        st.setOnFrameAvailableListener(this);
        CameraHolder.instance().setSurfaceTexture(st);
        CameraHolder.instance().startPreview();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mGLView.requestRender();
    }

    private static File createMediaFile(Context context, String parentPath, String fileName) {
        String state = Environment.getExternalStorageState();
        File rootDir = state.equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory() : context.getCacheDir();

        File folderDir = new File(rootDir.getAbsolutePath() + File.separator + parentPath);
        if (!folderDir.exists() && folderDir.mkdirs()) {

        }
        File tmpFile = new File(folderDir, fileName);

        return tmpFile;
    }

}
