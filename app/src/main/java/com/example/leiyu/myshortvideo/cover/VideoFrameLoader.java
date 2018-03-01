package com.example.leiyu.myshortvideo.cover;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.example.leiyu.myshortvideo.gles.GlCommonUtil;
import com.example.leiyu.myshortvideo.gles.GlCoordUtil;
import com.example.leiyu.myshortvideo.entity.MediaMetadata;
import com.example.leiyu.myshortvideo.utils.MyConstant;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Created by leiyu on 2018/3/1.
 */
public class VideoFrameLoader extends Thread{

    private static final String TAG = "FrameLoader";

    private String mVideoPath;
    private int mFrameCount;
    private VideoFrameListener mListener;
    private volatile boolean mIsStop;
    private long mDuration;
    private int mWidth;

    public interface VideoFrameListener {
        void onFrame(Bitmap bitmap, long time, int index);
        void onFail();
    }

    public VideoFrameLoader(String videoPath, int count, int width, long duration, VideoFrameListener listener) {
        mVideoPath = videoPath;
        mFrameCount = count;
        mListener = listener;
        mDuration = duration;
        mWidth = width;
    }

    @Override
    public void run() {
        MediaCodec decoder = null;
        VideoFrameLoader.CodecOutputSurface outputSurface = null;
        MediaExtractor extractor = null;

        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(mVideoPath);
            int trackIndex = selectTrack(extractor);
            if (trackIndex < 0) {
                throw new RuntimeException("No video track found in " + mVideoPath);
            }
            extractor.selectTrack(trackIndex);
            MediaFormat format = extractor.getTrackFormat(trackIndex);

            MediaMetadata metadata = new MediaMetadata(mVideoPath);
            int degree = metadata.getDegree();
            int videoWidth, videoHeight;
            long duration;
            if(degree == 90 || degree == 270) {
                videoWidth = metadata.getHeight();
                videoHeight = metadata.getWidth();
            } else {
                videoWidth = metadata.getWidth();
                videoHeight = metadata.getHeight();
            }
            duration = format.getLong(MediaFormat.KEY_DURATION);
            int height = videoHeight * mWidth / videoWidth;
            // Could use width/height from the MediaFormat to get full-size frames.
            outputSurface = new VideoFrameLoader.CodecOutputSurface(mWidth, height);

            // Create a MediaCodec decoder, and configure it with the MediaFormat from the
            // extractor.  It's very important to use the format from the extractor because
            // it contains a copy of the CSD-0/CSD-1 codec-specific data chunks.
            String mime = format.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(format, outputSurface.getSurface(), null, 0);
            decoder.start();

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            ByteBuffer[] inputBuffers = decoder.getInputBuffers();
            long interval = (duration) / (mFrameCount - 1);

            for(int i=0;i<mFrameCount;i++) {
                if(mIsStop) {
                    break;
                }
                long time = interval * i;
                seekTo(extractor, decoder, time, inputBuffers, bufferInfo);
                outputSurface.awaitNewImage();
                outputSurface.drawImage();
                Bitmap bitmap = outputSurface.saveFrame();
                mListener.onFrame(bitmap, time, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mListener.onFail();
        } finally {
            // release everything we grabbed
            if (outputSurface != null) {
                outputSurface.release();
            }
            if (decoder != null) {
                decoder.stop();
                decoder.release();
            }
            if (extractor != null) {
                extractor.release();
            }
        }
    }

    public void stopLoad() {
        mIsStop = true;
    }

    /**
     * Selects the video track, if any.
     *
     * @return the track index, or -1 if no video track is found.
     */
    private static int selectTrack(MediaExtractor extractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                return i;
            }
        }
        return -1;
    }

    private static void seekTo(MediaExtractor extractor, MediaCodec decoder, long time, ByteBuffer[] inputBuffers, MediaCodec.BufferInfo bufferInfo) {
        extractor.seekTo(time, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        decoder.flush();
        while (true) {
            int inputBufferIndex = decoder.dequeueInputBuffer(12000);
            if(inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                int size = extractor.readSampleData(inputBuffer, 0);
                long presentationTime = extractor.getSampleTime();
                if(size >= 0) {
                    decoder.queueInputBuffer(inputBufferIndex, 0, size, presentationTime, extractor.getSampleFlags());
                }
                boolean inputFinish = !extractor.advance();
                if(inputFinish) {
                    decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    Log.d(TAG, "Input video finish.");
                }
            }
            int outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 12000);
            if(outputBufferIndex >= 0) {
                if((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                    boolean render = bufferInfo.size != 0;
                    long pts = bufferInfo.presentationTimeUs;
                    decoder.releaseOutputBuffer(outputBufferIndex, render);
                    if(render) {
                        if(pts >= time) {
                            break;
                        }
                    }
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0 ) {
                        break;
                    }
                } else {
                    decoder.releaseOutputBuffer(outputBufferIndex, false);
                }
            }
        }
    }

    /**
     * Holds state associated with a Surface used for MediaCodec decoder output.
     * <p>
     * The constructor for this class will prepare GL, create a SurfaceTexture,
     * and then create a Surface for that SurfaceTexture.  The Surface can be passed to
     * MediaCodec.configure() to receive decoder output.  When a frame arrives, we latch the
     * texture with updateTexImage(), then render the texture with GL to a pbuffer.
     * <p>
     * By default, the Surface will be using a BufferQueue in asynchronous mode, so we
     * can potentially drop frames.
     */
    private static class CodecOutputSurface
            implements SurfaceTexture.OnFrameAvailableListener {
        private VideoFrameLoader.STextureRender mTextureRender;
        private SurfaceTexture mSurfaceTexture;
        private Surface mSurface;
        private EGL10 mEgl;

        private EGLDisplay mEGLDisplay = EGL10.EGL_NO_DISPLAY;
        private EGLContext mEGLContext = EGL10.EGL_NO_CONTEXT;
        private EGLSurface mEGLSurface = EGL10.EGL_NO_SURFACE;
        int mWidth;
        int mHeight;

        private Object mFrameSyncObject = new Object();     // guards mFrameAvailable
        private boolean mFrameAvailable;

        private ByteBuffer mPixelBuf;                       // used by saveFrame()

        /**
         * Creates a CodecOutputSurface backed by a pbuffer with the specified dimensions.  The
         * new EGL context and surface will be made current.  Creates a Surface that can be passed
         * to MediaCodec.configure().
         */
        public CodecOutputSurface(int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException();
            }
            mEgl = (EGL10) EGLContext.getEGL();
            mWidth = width;
            mHeight = height;

            eglSetup();
            makeCurrent();
            setup();
        }

        /**
         * Creates interconnected instances of TextureRender, SurfaceTexture, and Surface.
         */
        private void setup() {
            mTextureRender = new VideoFrameLoader.STextureRender();
            mTextureRender.surfaceCreated();

            Log.d(TAG, "textureID=" + mTextureRender.getTextureId());
            mSurfaceTexture = new SurfaceTexture(mTextureRender.getTextureId());

            // This doesn't work if this object is created on the thread that CTS started for
            // these test cases.
            //
            // The CTS-created thread has a Looper, and the SurfaceTexture constructor will
            // create a Handler that uses it.  The "frame available" message is delivered
            // there, but since we're not a Looper-based thread we'll never see it.  For
            // this to do anything useful, CodecOutputSurface must be created on a thread without
            // a Looper, so that SurfaceTexture uses the main application Looper instead.
            //
            // Java language note: passing "this" out of a constructor is generally unwise,
            // but we should be able to get away with it here.
            mSurfaceTexture.setOnFrameAvailableListener(this);

            mSurface = new Surface(mSurfaceTexture);

            mPixelBuf = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
            mPixelBuf.order(ByteOrder.LITTLE_ENDIAN);
        }

        /**
         * Prepares EGL.  We want a GLES 2.0 context and a surface that supports pbuffer.
         */
        private void eglSetup() {
            final int EGL_OPENGL_ES2_BIT = 0x0004;
            final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

            mEGLDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (mEGLDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("unable to get EGL14 display");
            }
            int[] version = new int[2];
            if (!mEgl.eglInitialize(mEGLDisplay, version)) {
                mEGLDisplay = null;
                throw new RuntimeException("unable to initialize EGL14");
            }

            // Configure EGL for pbuffer and OpenGL ES 2.0, 24-bit RGB.
            int[] attribList = {
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_ALPHA_SIZE, 8,
                    EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                    EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,
                    EGL10.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            if (!mEgl.eglChooseConfig(mEGLDisplay, attribList, configs, configs.length,
                    numConfigs)) {
                throw new RuntimeException("unable to find RGB888+recordable ES2 EGL config");
            }

            // Configure context for OpenGL ES 2.0.
            int[] attrib_list = {
                    EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL10.EGL_NONE
            };
            mEGLContext = mEgl.eglCreateContext(mEGLDisplay, configs[0], EGL10.EGL_NO_CONTEXT,
                    attrib_list);
            checkEglError("eglCreateContext");
            if (mEGLContext == null) {
                throw new RuntimeException("null context");
            }

            // Create a pbuffer surface.
            int[] surfaceAttribs = {
                    EGL10.EGL_WIDTH, mWidth,
                    EGL10.EGL_HEIGHT, mHeight,
                    EGL10.EGL_NONE
            };
            mEGLSurface = mEgl.eglCreatePbufferSurface(mEGLDisplay, configs[0], surfaceAttribs);
            checkEglError("eglCreatePbufferSurface");
            if (mEGLSurface == null) {
                throw new RuntimeException("surface was null");
            }
        }

        /**
         * Discard all resources held by this class, notably the EGL context.
         */
        public void release() {
            if (mEGLDisplay != EGL10.EGL_NO_DISPLAY) {
                mEgl.eglDestroySurface(mEGLDisplay, mEGLSurface);
                mEgl.eglDestroyContext(mEGLDisplay, mEGLContext);
                //mEgl.eglReleaseThread();
                mEgl.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_CONTEXT);
                mEgl.eglTerminate(mEGLDisplay);
            }
            mEGLDisplay = EGL10.EGL_NO_DISPLAY;
            mEGLContext = EGL10.EGL_NO_CONTEXT;
            mEGLSurface = EGL10.EGL_NO_SURFACE;

            mSurface.release();

            // this causes a bunch of warnings that appear harmless but might confuse someone:
            //  W BufferQueue: [unnamed-3997-2] cancelBuffer: BufferQueue has been abandoned!
            //mSurfaceTexture.release();

            mTextureRender = null;
            mSurface = null;
            mSurfaceTexture = null;
        }

        /**
         * Makes our EGL context and surface current.
         */
        public void makeCurrent() {
            if (!mEgl.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
                throw new RuntimeException("eglMakeCurrent failed");
            }
        }

        /**
         * Returns the Surface.
         */
        public Surface getSurface() {
            return mSurface;
        }

        /**
         * Latches the next buffer into the texture.  Must be called from the thread that created
         * the CodecOutputSurface object.  (More specifically, it must be called on the thread
         * with the EGLContext that contains the GL texture object used by SurfaceTexture.)
         */
        public void awaitNewImage() {
            final int TIMEOUT_MS = 5000;

            synchronized (mFrameSyncObject) {
                while (!mFrameAvailable) {
                    try {
                        // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                        // stalling the test if it doesn't arrive.
                        mFrameSyncObject.wait(TIMEOUT_MS);
                        if (!mFrameAvailable) {
                            // TODO: if "spurious wakeup", continue while loop
                            throw new RuntimeException("frame wait timed out");
                        }
                    } catch (InterruptedException ie) {
                        // shouldn't happen
                        throw new RuntimeException(ie);
                    }
                }
                mFrameAvailable = false;
            }

            // Latch the data.
            mTextureRender.checkGlError("before updateTexImage");
            mSurfaceTexture.updateTexImage();
        }

        /**
         * Draws the data from SurfaceTexture onto the current EGL surface.
         */
        public void drawImage() {
            mTextureRender.drawFrame(mSurfaceTexture);
        }

        // SurfaceTexture callback
        @Override
        public void onFrameAvailable(SurfaceTexture st) {
            Log.d(TAG, "new frame available");
            synchronized (mFrameSyncObject) {
                if (mFrameAvailable) {
                    throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
                }
                mFrameAvailable = true;
                mFrameSyncObject.notifyAll();
            }
        }

        /**
         * Saves the current frame to disk as a PNG image.
         */
        public Bitmap saveFrame() throws IOException {
            // glReadPixels gives us a ByteBuffer filled with what is essentially big-endian RGBA
            // data (i.e. a byte of red, followed by a byte of green...).  To use the Bitmap
            // constructor that takes an int[] array with pixel data, we need an int[] filled
            // with little-endian ARGB data.
            //
            // If we implement this as a series of buf.get() calls, we can spend 2.5 seconds just
            // copying data around for a 720p frame.  It's better to do a bulk get() and then
            // rearrange the data in memory.  (For comparison, the PNG compress takes about 500ms
            // for a trivial frame.)
            //
            // So... we set the ByteBuffer to little-endian, which should turn the bulk IntBuffer
            // get() into a straight memcpy on most Android devices.  Our ints will hold ABGR data.
            // Swapping B and R gives us ARGB.  We need about 30ms for the bulk get(), and another
            // 270ms for the color swap.
            //
            // We can avoid the costly B/R swap here if we do it in the fragment shader (see
            // http://stackoverflow.com/questions/21634450/ ).
            //
            // Having said all that... it turns out that the Bitmap#copyPixelsFromBuffer()
            // method wants RGBA pixels, not ARGB, so if we create an empty bitmap and then
            // copy pixel data in we can avoid the swap issue entirely, and just copy straight
            // into the Bitmap from the ByteBuffer.
            //
            // Making this even more interesting is the upside-down nature of GL, which means
            // our output will look upside-down relative to what appears on screen if the
            // typical GL conventions are used.  (For ExtractMpegFrameTest, we avoid the issue
            // by inverting the frame when we render it.)
            //
            // Allocating large buffers is expensive, so we really want mPixelBuf to be
            // allocated ahead of time if possible.  We still get some allocations from the
            // Bitmap / PNG creation.

            mPixelBuf.rewind();
            GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                    mPixelBuf);
            Bitmap bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mPixelBuf.rewind();
            bmp.copyPixelsFromBuffer(mPixelBuf);
            return bmp;
        }

        /**
         * Checks for EGL errors.
         */
        private void checkEglError(String msg) {
            int error;
            if ((error = mEgl.eglGetError()) != EGL10.EGL_SUCCESS) {
                throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
            }
        }
    }


    /**
     * Code for rendering a texture onto a surface using OpenGL ES 2.0.
     */
    private static class STextureRender {

        private final FloatBuffer mNormalVtxBuf = GlCoordUtil.createVertexBuffer();
        private final FloatBuffer mNormalTexCoordBuf = GlCoordUtil.createTexCoordBuffer();
        private final float[] mPosMtx = GlCoordUtil.createIdentityMtx();
        private final float[] mTexMtx = GlCoordUtil.createIdentityMtx();

        private int mTextureID = -12345;

        private int mProgram         = -1;
        private int maPositionHandle = -1;
        private int maTexCoordHandle = -1;
        private int muSamplerHandle  = -1;
        private int muPosMtxHandle   = -1;
        private int muTexMtxHandle   = -1;

        public STextureRender() {
            Matrix.scaleM(mPosMtx, 0, 1, -1, 1);
        }

        public int getTextureId() {
            return mTextureID;
        }

        /**
         * Draws the external texture in SurfaceTexture onto the current EGL surface.
         */
        public void drawFrame(SurfaceTexture st) {
            checkGlError("onDrawFrame start");
            st.getTransformMatrix(mTexMtx);

            // (optional) clear to green so we can see if we're failing to set pixels
            GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(mProgram);
            checkGlError("glUseProgram");

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
                GLES20.glUniformMatrix4fv(muTexMtxHandle, 1, false, mTexMtx, 0);
            }
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        }

        /**
         * Initializes GL state.  Call this after the EGL surface has been created and made current.
         */
        public void surfaceCreated() {
            mProgram = createProgram(MyConstant.SHADER_NULL_VERTEX, MyConstant.SHADER_NULL_FRAGMENT);
            if (mProgram == 0) {
                throw new RuntimeException("failed creating program");
            }

            mProgram         = GlCommonUtil.createProgram(MyConstant.SHADER_NULL_VERTEX, MyConstant.SHADER_NULL_FRAGMENT);
            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
            maTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
            muSamplerHandle  = GLES20.glGetUniformLocation(mProgram, "uSampler");
            muPosMtxHandle   = GLES20.glGetUniformLocation(mProgram, "uPosMtx");
            muTexMtxHandle   = GLES20.glGetUniformLocation(mProgram, "uTexMtx");

            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            GLES20.glDisable(GLES20.GL_BLEND);


            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            mTextureID = textures[0];
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
            checkGlError("glBindTexture mTextureID");

            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
            checkGlError("glTexParameter");
        }


        private int loadShader(int shaderType, String source) {
            int shader = GLES20.glCreateShader(shaderType);
            checkGlError("glCreateShader type=" + shaderType);
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.d(TAG, "Could not compile shader " + shaderType + ":");
                Log.d(TAG, " " + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
            return shader;
        }

        private int createProgram(String vertexSource, String fragmentSource) {
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
            if (vertexShader == 0) {
                return 0;
            }
            int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
            if (pixelShader == 0) {
                return 0;
            }

            int program = GLES20.glCreateProgram();
            if (program == 0) {
                Log.d(TAG, "Could not create program");
            }
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.d(TAG, "Could not link program: ");
                Log.d(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
            return program;
        }

        public void checkGlError(String op) {
            int error;
            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                Log.d(TAG, op + ": glError " + error);
                throw new RuntimeException(op + ": glError " + error);
            }
        }

        public static void checkLocation(int location, String label) {
            if (location < 0) {
                throw new RuntimeException("Unable to locate '" + label + "' in program");
            }
        }
    }
}
