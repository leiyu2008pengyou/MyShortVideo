package com.example.leiyu.myshortvideo.utils.configuration;

/**
 * Created by leiyu on 2018/3/1.
 */

public final class VideoConfiguration {
    public static final int DEFAULT_VIDEO_HEIGHT = 640;
    public static final int DEFAULT_VIDEO_WIDTH = 360;
    public static final int DEFAULT_VIDEO_FPS = 24;
    public static final int DEFAULT_VIDEO_BPS = 1300;
    public static final int DEFAULT_VIDEO_IFI = 1;

    public final int height;
    public final int width;
    public final int bps;
    public final int fps;
    public final int ifi;

    private VideoConfiguration(final Builder builder) {
        height = builder.height;
        width = builder.width;
        bps = builder.bps;
        fps = builder.fps;
        ifi = builder.ifi;
    }

    public static VideoConfiguration createDefault() {
        return new Builder().build();
    }

    public static class Builder {
        private int height = DEFAULT_VIDEO_HEIGHT;
        private int width = DEFAULT_VIDEO_WIDTH;
        private int bps = DEFAULT_VIDEO_BPS;
        private int fps = DEFAULT_VIDEO_FPS;
        private int ifi = DEFAULT_VIDEO_IFI;

        public Builder setSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder setBps(int bps) {
            this.bps = bps;
            return this;
        }

        public Builder setFps(int fps) {
            this.fps = fps;
            return this;
        }

        public Builder setIfi(int ifi) {
            this.ifi = ifi;
            return this;
        }

        public VideoConfiguration build() {
            return new VideoConfiguration(this);
        }
    }
}
