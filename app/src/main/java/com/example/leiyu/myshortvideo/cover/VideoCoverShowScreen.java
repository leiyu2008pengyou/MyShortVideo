package com.example.leiyu.myshortvideo.cover;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.example.leiyu.myshortvideo.gles.GlCommonUtil;
import com.example.leiyu.myshortvideo.gles.GlCoordUtil;

import java.nio.FloatBuffer;

import static com.example.leiyu.myshortvideo.gles.GlCommonUtil.checkGlError;

/**
 * Created by leiyu on 2018/3/1.
 */

public class VideoCoverShowScreen {
    private static final String TAG = "RenderScreen";

    private final FloatBuffer mNormalVtxBuf = GlCoordUtil.createVertexBuffer();
    private final FloatBuffer mNormalTexCoordBuf = GlCoordUtil.createTexCoordBuffer();
    private final float[] mPosMtx = GlCoordUtil.createIdentityMtx();

    private int mProgram         = -1;
    private int maPositionHandle = -1;
    private int maTexCoordHandle = -1;
    private int muSamplerHandle  = -1;
    private int muPosMtxHandle   = -1;
    private int muTexMtxHandle   = -1;

    private int mWidth = -1;
    private int mHeight = -1;
    private int mTextureId = -1;

    VideoCoverShowScreen() {
        initGL();
    }

    void setScreenSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    void setTextureId(int textureId) {
        mTextureId = textureId;
    }

    void draw(float[] texMtx) {
        if(mWidth == -1 || mHeight == -1 || mTextureId == -1) {
            Log.e(TAG, "Not draw");
            return;
        }
        initProgram();
        drawFrame(texMtx);
    }

    private void drawFrame(float[] texMtx) {
        checkGlError("onDrawFrame start");
        mNormalVtxBuf.position(0);
        GLES20.glVertexAttribPointer(maPositionHandle,
                3, GLES20.GL_FLOAT, false, 4 * 3, mNormalVtxBuf);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        mNormalTexCoordBuf.position(0);
        GLES20.glVertexAttribPointer(maTexCoordHandle,
                2, GLES20.GL_FLOAT, false, 4 * 2, mNormalTexCoordBuf);
        GLES20.glEnableVertexAttribArray(maTexCoordHandle);
        GLES20.glUniform1i(muSamplerHandle, 0);
        if(muPosMtxHandle>= 0) {
            GLES20.glUniformMatrix4fv(muPosMtxHandle, 1, false, mPosMtx, 0);
        }
        if(muTexMtxHandle>= 0) {
            GLES20.glUniformMatrix4fv(muTexMtxHandle, 1, false, texMtx, 0);
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("onDrawFrame end");
    }

    private void initGL() {
        checkGlError("initGL_S");
        final String vertexShader =
                //
                "attribute vec4 position;\n" +
                        "attribute vec4 inputTextureCoordinate;\n" +
                        "varying   vec2 textureCoordinate;\n" +
                        "uniform   mat4 uPosMtx;\n" +
                        "uniform   mat4 uTexMtx;\n" +
                        "void main() {\n" +
                        "  gl_Position = uPosMtx * position;\n" +
                        "  textureCoordinate   = (uTexMtx * inputTextureCoordinate).xy;\n" +
                        "}\n";
        final String fragmentShader =
                //
                "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "uniform samplerExternalOES uSampler;\n" +
                        "varying vec2 textureCoordinate;\n" +
                        "void main() {\n" +
                        "  gl_FragColor = texture2D(uSampler, textureCoordinate);\n" +
                        "}\n";
        mProgram         = GlCommonUtil.createProgram(vertexShader, fragmentShader);
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
        maTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        muSamplerHandle  = GLES20.glGetUniformLocation(mProgram, "uSampler");
        muPosMtxHandle   = GLES20.glGetUniformLocation(mProgram, "uPosMtx");
        muTexMtxHandle   = GLES20.glGetUniformLocation(mProgram, "uTexMtx");

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_BLEND);

        checkGlError("initGL_E");
    }

    private void initProgram() {
        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
    }
}
