package com.example.leiyu.myshortvideo.gles;

import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @Title: GlCoordUtil
 * @Package com.laifeng.media.effect.video
 * @Description:
 * @Author Jim
 * @Date 2017/1/17
 * @Time 下午4:26
 * @Version
 */

public class GlCoordUtil {
    public static FloatBuffer createSquareVtx() {
        final float vtx[] = {
                // XYZ, UV
                -1f,  1f, 0f, 0f, 1f,
                -1f, -1f, 0f, 0f, 0f,
                1f,   1f, 0f, 1f, 1f,
                1f,  -1f, 0f, 1f, 0f,
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * vtx.length);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(vtx);
        fb.position(0);
        return fb;
    }

    public static FloatBuffer createVertexBuffer() {
        final float vtx[] = {
                // XYZ
                -1f,  1f, 0f,
                -1f, -1f, 0f,
                1f,   1f, 0f,
                1f,  -1f, 0f,
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * vtx.length);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(vtx);
        fb.position(0);
        return fb;
    }

    public static FloatBuffer createTexCoordBuffer() {
        final float vtx[] = {
                // UV
                0f, 1f,
                0f, 0f,
                1f, 1f,
                1f, 0f,
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * vtx.length);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(vtx);
        fb.position(0);
        return fb;
    }

    public static float[] createIdentityMtx() {
        float[] m = new float[16];
        Matrix.setIdentityM(m, 0);
        return m;
    }

    public static FloatBuffer getTextureCoordinate(int inputWidth, int inputHeight, int outputWidth, int outputHeight) {
        if(inputWidth <= 0 || inputHeight <= 0 || outputHeight <= 0 || outputWidth <= 0) {
            return null;
        }

        FloatBuffer textureBuffer;
        float hRatio = outputWidth / ((float)inputWidth);
        float vRatio = outputHeight / ((float)inputHeight);

        float ratio;
        if(hRatio > vRatio) {
            ratio = outputHeight / (inputHeight * hRatio);
            final float vtx[] = {
                    //UV
                    0f, 0.5f + ratio/2,
                    0f, 0.5f - ratio/2,
                    1f, 0.5f + ratio/2,
                    1f, 0.5f - ratio/2,
            };
            ByteBuffer bb = ByteBuffer.allocateDirect(4 * vtx.length);
            bb.order(ByteOrder.nativeOrder());
            textureBuffer = bb.asFloatBuffer();
            textureBuffer.put(vtx);
            textureBuffer.position(0);
        } else {
            ratio = outputWidth/ (inputWidth * vRatio);
            final float vtx[] = {
                    //UV
                    0.5f - ratio/2, 1f,
                    0.5f - ratio/2, 0f,
                    0.5f + ratio/2, 1f,
                    0.5f + ratio/2, 0f,
            };
            ByteBuffer bb = ByteBuffer.allocateDirect(4 * vtx.length);
            bb.order(ByteOrder.nativeOrder());
            textureBuffer = bb.asFloatBuffer();
            textureBuffer.put(vtx);
            textureBuffer.position(0);
        }
        return textureBuffer;
    }
}
