package com.example.leiyu.myshortvideo.utils.configuration;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by leiyu on 2018/3/1.
 */

public final class AudioConfiguration {
    public static final int DEFAULT_AUDIO_FREQUENCY = 44100;
    public static final int DEFAULT_AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    public static final int DEFAULT_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    public static final int DEFAULT_AUDIO_BPS = 64;

    public final int frequency;
    public final int encoding;
    public final int channel;
    public final int source;
    public final int bps;

    private AudioConfiguration(final Builder builder) {
        frequency = builder.frequency;
        encoding = builder.encoding;
        channel = builder.channel;
        source = builder.source;
        bps = builder.bps;
    }

    public static AudioConfiguration createDefault() {
        return new Builder().build();
    }

    public static AudioConfiguration createAllSupport() {
        return new Builder().setFrequency(8000).
                setChannel(AudioFormat.CHANNEL_IN_MONO).build();
    }

    public static class Builder {
        private int frequency = DEFAULT_AUDIO_FREQUENCY;
        private int encoding = DEFAULT_AUDIO_ENCODING;
        private int channel = DEFAULT_AUDIO_CHANNEL;
        private int source = DEFAULT_AUDIO_SOURCE;
        private int bps = DEFAULT_AUDIO_BPS;

        @IntDef({
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.CHANNEL_IN_STEREO,
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface ChannelMode {}

        @IntDef({
                8000,
                11025,
                16000,
                44100,
                22050,
                48000,
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface FrequencyMode {}

        @IntDef({
                AudioFormat.ENCODING_PCM_16BIT,
                AudioFormat.ENCODING_PCM_8BIT,
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface EncodeMode {}

        @IntDef({
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                MediaRecorder.AudioSource.REMOTE_SUBMIX,
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface SourceMode {}

        public Builder setFrequency(@FrequencyMode int frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder setEncoding(@EncodeMode int encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder setChannel(@ChannelMode int channel) {
            this.channel = channel;
            return this;
        }

        public Builder setSource(@SourceMode int source) {
            this.source = source;
            return this;
        }

        public Builder setBps(int bps) {
            this.bps = bps;
            return this;
        }

        public AudioConfiguration build() {
            return new AudioConfiguration(this);
        }
    }
}
