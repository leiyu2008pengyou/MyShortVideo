/*
 * Copyright 2013 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.leiyu.myshortvideo;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.leiyu.myshortvideo.camera.CameraConfiguration;
import com.example.leiyu.myshortvideo.camera.CameraHolder;
import com.example.leiyu.myshortvideo.camera.CameraListener;
import com.example.leiyu.myshortvideo.camera.CameraUtils;
import com.example.leiyu.myshortvideo.camera.exception.CameraDisabledException;
import com.example.leiyu.myshortvideo.camera.exception.CameraHardwareException;
import com.example.leiyu.myshortvideo.camera.exception.CameraNotSupportException;
import com.example.leiyu.myshortvideo.camera.exception.NoCameraException;

import java.io.IOException;

/**
 * More or less straight out of TextureView's doc.
 * <p>
 * TODO: add options for different display sizes, frame rates, camera selection, etc.
 */
public class LiveCameraActivity extends Activity implements TextureView.SurfaceTextureListener {
    private static final String TAG = "LiveCameraActivity";

    private Camera mCamera;
    private SurfaceTexture mSurfaceTexture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextureView textureView = new TextureView(this);
        textureView.setSurfaceTextureListener(this);

        setContentView(textureView);
        CameraConfiguration cameraConfiguration = new CameraConfiguration.Builder()
                .setFacing(CameraConfiguration.Facing.BACK)
                .setFocusMode(CameraConfiguration.FocusMode.AUTO)
                .build();
        CameraHolder.instance().setConfiguration(cameraConfiguration);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        mSurfaceTexture = surface;
        startPreview();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//        mCamera.stopPreview();
//        mCamera.release();
        CameraHolder.instance().stopPreview();
        CameraHolder.instance().releaseCamera();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Invoked every time there's a new Camera preview frame
        //Log.d(TAG, "updated, ts=" + surface.getTimestamp());
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
            CameraHolder.instance().setSurfaceTexture(mSurfaceTexture);
            if (state != CameraHolder.State.PREVIEW) {
                try {
                    CameraHolder.instance().openCamera();
                    CameraHolder.instance().startPreview();
                } catch (CameraHardwareException e) {
                    result = CameraListener.CAMERA_OPEN_FAILED;
                    e.printStackTrace();
                } catch (CameraNotSupportException e) {
                    result = CameraListener.CAMERA_NOT_SUPPORT;
                    e.printStackTrace();
                }
            }
        }
//        if(result == 0) {
//            mRenderCameraView.changeFocusModeUI();
//            if(mCameraOpenListener != null) {
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mCameraOpenListener.onOpenSuccess();
//                    }
//                });
//            }
//            isCameraOpen = true;
//        } else {
//            if(mCameraOpenListener != null) {
//                final int outResult = result;
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mCameraOpenListener.onOpenFail(outResult);
//                    }
//                });
//            }
//        }

//        mCamera = Camera.open();
//        if (mCamera == null) {
//            // Seeing this on Nexus 7 2012 -- I guess it wants a rear-facing camera, but
//            // there isn't one.  TODO: fix
//            throw new RuntimeException("Default camera not available");
//        }
//
//        try {
//            mCamera.setPreviewTexture(mSurfaceTexture);
//            Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
//
//            if(display.getRotation() == Surface.ROTATION_0) {
//                mCamera.setDisplayOrientation(90);
//            }
//            if(display.getRotation() == Surface.ROTATION_270) {
//                mCamera.setDisplayOrientation(180);
//            }
//            mCamera.startPreview();
//        } catch (IOException ioe) {
//            // Something bad happened
//            Log.e(TAG,"Exception starting preview", ioe);
//        }
    }
}
