package com.example.leiyu.myshortvideo.utils;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;

import com.example.leiyu.myshortvideo.entity.MediaData;
import com.example.leiyu.myshortvideo.entity.MediaMetadata;
import com.example.leiyu.myshortvideo.utils.configuration.AudioConfiguration;
import com.example.leiyu.myshortvideo.utils.configuration.VideoConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaExtractor.SEEK_TO_PREVIOUS_SYNC;

/**
 * Created by leiyu on 2018/3/1.
 */
@TargetApi(18)
public class MediaUtil {
    private static final String AUDIO_MIME = "audio/mp4a-latm";
    private static final String VIDEO_MIME = "video/avc";

    /**
     * Returns the first codec capable of encoding the specified MIME type, or null if no match was
     * found.
     */
    public static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    // We avoid the device-specific limitations on width and height by using values that
    // are multiples of 16, which all tested devices seem to be able to handle.
    public static int getVideoSize(int size) {
        int multiple = (int) Math.ceil(size/16.0);
        return multiple*16;
    }

    public static String getMimeTypeFor(MediaFormat format) {
        return format.getString(MediaFormat.KEY_MIME);
    }

    public static int getAndSelectVideoTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            if (isVideoFormat(extractor.getTrackFormat(index))) {
                extractor.selectTrack(index);
                return index;
            }
        }
        return -1;
    }

    public static int getAndSelectAudioTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            if (isAudioFormat(extractor.getTrackFormat(index))) {
                extractor.selectTrack(index);
                return index;
            }
        }
        return -1;
    }

    public static boolean isVideoFormat(MediaFormat format) {
        return getMimeTypeFor(format).startsWith("video/");
    }

    public static boolean isAudioFormat(MediaFormat format) {
        return getMimeTypeFor(format).startsWith("audio/");
    }

    public static MediaExtractor createExtractor(String path) throws IOException {
        MediaExtractor extractor;
        File inputFile = new File(path);   // must be an absolute path
        if (!inputFile.canRead()) {
            throw new FileNotFoundException("Unable to read " + inputFile);
        }
        extractor = new MediaExtractor();
        extractor.setDataSource(inputFile.toString());
        return extractor;
    }

    public static MediaFormat getVideoFormat(VideoConfiguration videoConfiguration) {
        int videoWidth = MediaUtil.getVideoSize(videoConfiguration.width);
        int videoHeight = MediaUtil.getVideoSize(videoConfiguration.height);
        MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME, videoWidth, videoHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoConfiguration.bps* 1000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, videoConfiguration.fps);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, videoConfiguration.ifi);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
            format.setInteger(MediaFormat.KEY_COMPLEXITY, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        }
        return format;
    }

    public static MediaFormat copyVideoFormat(MediaFormat mediaFormat, int bitrate) {
        String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
        int videoWidth = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
        int videoHeight = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);

        videoWidth = MediaUtil.getVideoSize(videoWidth);
        videoHeight = MediaUtil.getVideoSize(videoHeight);

        MediaFormat format = MediaFormat.createVideoFormat(mime, videoWidth, videoHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate * 1000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 24);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
            format.setInteger(MediaFormat.KEY_COMPLEXITY, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        }
        return format;
    }

    public static MediaFormat getAudioEncodeFormat(AudioConfiguration configuration) {
        int channelCount = (configuration.channel == AudioFormat.CHANNEL_IN_STEREO ? 2 : 1);
        MediaFormat format = MediaFormat.createAudioFormat(AUDIO_MIME, configuration.frequency, channelCount);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_BIT_RATE, configuration.bps * 1000);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, configuration.frequency);
        int maxInputSize = AudioUtils.getRecordBufferSize(configuration);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize * 2);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount);
        return format;
    }

    public static AudioConfiguration createAudioEncodeConfiguration(int frequency, int channelCount) {
        AudioConfiguration.Builder builder = new AudioConfiguration.Builder();
        int channel = AudioFormat.CHANNEL_IN_MONO;
        if(channelCount != 1) {
            channel = AudioFormat.CHANNEL_IN_STEREO;
        }
        builder.setFrequency(frequency).setChannel(channel).setBps(128);
        return builder.build();
    }

    public static VideoConfiguration createVideoConfiguration(MediaMetadata metadata, int width, int height, int bitrate) {
        VideoConfiguration.Builder builder = new VideoConfiguration.Builder();
        int degree = metadata.getDegree();
        int finalWidth, finalHeight;
        if(degree == 90 || degree == 270) {
            finalWidth = metadata.getHeight();
            finalHeight = metadata.getWidth();
        } else {
            finalWidth = metadata.getWidth();
            finalHeight = metadata.getHeight();
        }
        int videoMax = Math.max(finalWidth, finalHeight);
        int videoMin = Math.min(finalWidth, finalHeight);

        int limitMax = Math.max(width, height);
        int limitMin = Math.min(width, height);

        if(videoMax > limitMax || videoMin > limitMin) {
            float maxRatio = videoMax / (float) limitMax;
            float minRatio = videoMin / (float) limitMin;
            float ratio = maxRatio;
            if(maxRatio < minRatio) {
                ratio = minRatio;
            }
            videoMax = (int) (videoMax / ratio);
            videoMin = (int) (videoMin / ratio);
            if(finalWidth > finalHeight) {
                finalWidth = videoMax;
                finalHeight = videoMin;
            } else {
                finalWidth = videoMin;
                finalHeight = videoMax;
            }
        }
        builder.setSize(finalWidth, finalHeight).setBps(bitrate).setIfi(1);
        return builder.build();
    }

    public static MediaData clone(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        ByteBuffer clone = ByteBuffer.allocate(bi.size);
        bb.position(bi.offset);
        bb.limit(bi.offset + bi.size);
        clone.put(bb);
        clone.flip();

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        bufferInfo.set(0, bi.size, bi.presentationTimeUs, bi.flags);

        MediaData data = new MediaData();
        data.buffer = clone;
        data.bufferInfo = bufferInfo;
        return data;
    }

    public static void generateAdts(byte[] data, int sampleRate, int channelCount, int dataLength) {
        if(data.length != 7) {
            return;
        }
        int adtsLength = 7;
        int fullLength = adtsLength + dataLength;
        int profile = 2;
        int freqIdx = getAudioSimpleRateIndex(sampleRate);
        data[0] = (byte) 0xFF;
        data[1] = (byte) 0xF9;
        data[2] = (byte) (((profile-1)<<6) + (freqIdx<<2) +(channelCount>>2));
        data[3] = (byte) (((channelCount&3)<<6) + (fullLength>>11));
        data[4] = (byte) ((fullLength & 0x7FF) >> 3);
        data[5] = (byte) (((fullLength & 7)<<5) + 0x1F);
        data[6] = (byte) 0xFC;
    }

    public static long getKeyFramePts(String path, long ptsUs) throws IOException {
        MediaExtractor extractor = MediaUtil.createExtractor(path);
        MediaUtil.getAndSelectVideoTrackIndex(extractor);
        extractor.seekTo(ptsUs, SEEK_TO_PREVIOUS_SYNC);
        long result = extractor.getSampleTime();
        extractor.release();
        if(result < 0) {
            result = ptsUs;
        }
        return result;
    }

    public static byte[] getAudioSpecInfo(String path) {
        byte[] specInfo = {0x12, 0x10};
        try {
            MediaExtractor extractor = MediaUtil.createExtractor(path);
            int audioTrack = MediaUtil.getAndSelectAudioTrackIndex(extractor);
            MediaFormat decodeFormat = extractor.getTrackFormat(audioTrack);
            int frequency = decodeFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int channelCount = decodeFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

            int soundRateIndex = getAudioSimpleRateIndex(frequency);
            specInfo[0] = (byte) (0x10 | ((soundRateIndex>>1) & 0x7));
            specInfo[1] = (byte) (((soundRateIndex & 0x1)<<7) | ((channelCount & 0xF) << 3));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return specInfo;
    }

    /**
     * 根据传入的采样频率获取规定的采样频率的index
     * @param audioSampleRate
     * @return
     */
    public static int getAudioSimpleRateIndex(int audioSampleRate) {
        int simpleRateIndex;
        switch (audioSampleRate) {
            case 96000:
                simpleRateIndex = 0;
                break;
            case 88200:
                simpleRateIndex = 1;
                break;
            case 64000:
                simpleRateIndex = 2;
                break;
            case 48000:
                simpleRateIndex = 3;
                break;
            case 44100:
                simpleRateIndex = 4;
                break;
            case 32000:
                simpleRateIndex = 5;
                break;
            case 24000:
                simpleRateIndex = 6;
                break;
            case 22050:
                simpleRateIndex = 7;
                break;
            case 16000:
                simpleRateIndex = 8;
                break;
            case 12000:
                simpleRateIndex = 9;
                break;
            case 11025:
                simpleRateIndex = 10;
                break;
            case 8000:
                simpleRateIndex = 11;
                break;
            case 7350:
                simpleRateIndex = 12;
                break;
            default:
                simpleRateIndex = 15;
        }
        return simpleRateIndex;
    }
}
