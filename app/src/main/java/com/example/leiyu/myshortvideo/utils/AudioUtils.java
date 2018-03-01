package com.example.leiyu.myshortvideo.utils;

import android.media.AudioRecord;

import com.example.leiyu.myshortvideo.utils.configuration.AudioConfiguration;

/**
 * Created by leiyu on 2018/3/1.
 */

public class AudioUtils {
    public static boolean checkMicSupport(AudioConfiguration audioConfiguration) {
        boolean result = false;
        int recordBufferSize = getRecordBufferSize(audioConfiguration);
        if(recordBufferSize <= 0) {
            return false;
        }
        byte[] mRecordBuffer = new byte[recordBufferSize];
        AudioRecord audioRecord;
        try {
            audioRecord = getAudioRecord(audioConfiguration);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        try {
            audioRecord.startRecording();
            int readLen = audioRecord.read(mRecordBuffer, 0, recordBufferSize);
            result = readLen >= 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            audioRecord.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int getRecordBufferSize(AudioConfiguration audioConfiguration) {
        return AudioRecord.getMinBufferSize(audioConfiguration.frequency,
                audioConfiguration.channel, audioConfiguration.encoding);
    }

    public static AudioRecord getAudioRecord(AudioConfiguration audioConfiguration) {
        return new AudioRecord(audioConfiguration.source, audioConfiguration.frequency,
                audioConfiguration.channel, audioConfiguration.encoding, getRecordBufferSize(audioConfiguration));
    }
}
