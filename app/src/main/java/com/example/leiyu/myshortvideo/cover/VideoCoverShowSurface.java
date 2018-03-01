package com.example.leiyu.myshortvideo.cover;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by leiyu on 2018/3/1.
 */

public class VideoCoverShowSurface extends GLSurfaceView {
    private VideoCoverShowRender mRenderer;

    VideoCoverShowSurface(Context context) {
        super(context);
        init();
    }

    VideoCoverShowSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mRenderer = new VideoCoverShowRender(this);
        setEGLContextClientVersion(2);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void setSurfaceListener(VideoCoverSurfaceListener listener) {
        mRenderer.setListener(listener);
    }

    public void updateRender() {
        mRenderer.updateRender();
    }
}
