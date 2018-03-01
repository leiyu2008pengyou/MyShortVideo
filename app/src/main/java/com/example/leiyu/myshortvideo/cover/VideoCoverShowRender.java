package com.example.leiyu.myshortvideo.cover;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.Surface;

import com.example.leiyu.myshortvideo.gles.GlCoordUtil;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by leiyu on 2018/3/1.
 */

public class VideoCoverShowRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener{
    private VideoCoverShowScreen mRenderScreen;
    private GLSurfaceView mGLSurfaceView;
    private int mSurfaceTextureId = -1;
    private SurfaceTexture mSurfaceTexture;
    private boolean mUpdateSurface;
    private final float[] mTexMtx = GlCoordUtil.createIdentityMtx();
    private VideoCoverSurfaceListener mListener;
    private Lock mUpdateLock = new ReentrantLock();
    private Lock mAvailableLock = new ReentrantLock();
    private Condition mAvailableCondition = mAvailableLock.newCondition();
    private boolean mFrameAvailable;

    VideoCoverShowRender(GLSurfaceView view) {
        mGLSurfaceView = view;
    }

    void setListener(VideoCoverSurfaceListener listener) {
        mListener = listener;
    }

    void updateRender() {
        waitUpdateForAvailable();
        requestUpdate();
    }

    private void requestUpdate() {
        mUpdateLock.lock();
        mUpdateSurface = true;
        mUpdateLock.unlock();
        mGLSurfaceView.requestRender();
    }

    private void waitUpdateForAvailable() {
        mAvailableLock.lock();
        if(!mFrameAvailable) {
            try {
                mAvailableCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mAvailableLock.unlock();
    }

    private void wakeupForAvailable() {
        mAvailableLock.lock();
        mFrameAvailable = true;
        mAvailableCondition.signal();
        mAvailableLock.unlock();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        wakeupForAvailable();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        refreshAfterSurfaceChange();
        if(mSurfaceTextureId == -1) {
            initSurfaceTexture();
        }
        if(mRenderScreen == null) {
            initScreenTexture();
        }
        if(mRenderScreen != null) {
            mRenderScreen.setScreenSize(width, height);
        }
    }

    private void refreshAfterSurfaceChange() {
        if(mRenderScreen != null) {
            mGLSurfaceView.post(new Runnable() {
                @Override
                public void run() {
                    requestUpdate();;
                }
            });
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mUpdateLock.lock();
        if (mUpdateSurface && mRenderScreen != null) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mTexMtx);
            mRenderScreen.draw(mTexMtx);
            mUpdateSurface = false;
        }
        mUpdateLock.unlock();
    }

    private void initSurfaceTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mSurfaceTextureId = textures[0];
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mSurfaceTextureId);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        mSurfaceTexture = new SurfaceTexture(mSurfaceTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        Surface surface = new Surface(mSurfaceTexture);
        if(mListener != null) {
            mListener.OnSurfaceCreated(surface);
        }
    }

    private void initScreenTexture() {
        mRenderScreen = new VideoCoverShowScreen();
        mRenderScreen.setTextureId(mSurfaceTextureId);
    }
}
