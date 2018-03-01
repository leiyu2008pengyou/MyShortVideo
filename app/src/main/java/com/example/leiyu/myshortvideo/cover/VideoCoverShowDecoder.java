package com.example.leiyu.myshortvideo.cover;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.example.leiyu.myshortvideo.utils.MediaUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by leiyu on 2018/3/1.
 */
@TargetApi(18)
public class VideoCoverShowDecoder {
    public static final String TAG = "VideoCoverShowDecoder";
    private String mVideoPath;
    private Surface mSurface;
    private MediaExtractor mMediaExtractor;
    private MediaFormat mMediaFormat;
    private MediaCodec mMediaCodec;
    private boolean mHasError = true;
    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    private ByteBuffer[] inputBuffers;

    VideoCoverShowDecoder(String videoPath, Surface surface) {
        mVideoPath = videoPath;
        mSurface = surface;
        initExtractor();
        if(mMediaExtractor == null) {
            return;
        }
        initMediaCodec();
        if(mMediaCodec == null) {
            return;
        }
        mHasError = false;
        mMediaCodec.start();
        inputBuffers = mMediaCodec.getInputBuffers();
    }

    private void initExtractor() {
        try {
            mMediaExtractor = MediaUtil.createExtractor(mVideoPath);
            int videoTrack = MediaUtil.getAndSelectVideoTrackIndex(mMediaExtractor);
            mMediaFormat = mMediaExtractor.getTrackFormat(videoTrack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initMediaCodec() {
        String videoMime = MediaUtil.getMimeTypeFor(mMediaFormat);
        try {
            mMediaCodec = MediaCodec.createDecoderByType(videoMime);
            mMediaCodec.configure(mMediaFormat, mSurface, null, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seekTo(long time) {
        if(mHasError) {
            return;
        }
        mMediaExtractor.seekTo(time * 1000, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        mMediaCodec.flush();
        while (true) {
            int inputBufferIndex = mMediaCodec.dequeueInputBuffer(12000);
            if(inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                int size = mMediaExtractor.readSampleData(inputBuffer, 0);
                long presentationTime = mMediaExtractor.getSampleTime();
                if(size >= 0) {
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, size, presentationTime, mMediaExtractor.getSampleFlags());
                }
                boolean inputFinish = !mMediaExtractor.advance();
                if(inputFinish) {
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    Log.d(TAG, "Input video finish.");
                }
            }
            int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 12000);
            if(outputBufferIndex >= 0) {
                if((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                    boolean render = bufferInfo.size != 0;
                    long pts = bufferInfo.presentationTimeUs;
                    mMediaCodec.releaseOutputBuffer(outputBufferIndex, render);
                    if(render) {
                        if(pts >= time * 1000) {
                            break;
                        }
                    }
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0 ) {
                        break;
                    }
                } else {
                    mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                }
            }
        }
    }

    public void release() {
        if(mMediaExtractor != null) {
            mMediaExtractor.release();
            mMediaExtractor = null;
        }
        if(mMediaCodec != null) {
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }
}
