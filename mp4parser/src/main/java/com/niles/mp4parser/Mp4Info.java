package com.niles.mp4parser;

/**
 * Created by Niles
 * Date 2018/11/20 10:58
 * Email niulinguo@163.com
 */
public class Mp4Info {

    private final int mDuration;
    private final int mWidth;
    private final int mHeight;

    private Mp4Info(int duration, int width, int height) {
        mDuration = duration;
        mWidth = width;
        mHeight = height;
    }

    public int getDuration() {
        return mDuration;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public static final class Builder {
        private int mDuration;
        private int mWidth;
        private int mHeight;

        public int getDuration() {
            return mDuration;
        }

        public Builder setDuration(int duration) {
            mDuration = duration;
            return this;
        }

        public int getWidth() {
            return mWidth;
        }

        public Builder setWidth(int width) {
            mWidth = width;
            return this;
        }

        public int getHeight() {
            return mHeight;
        }

        public Builder setHeight(int height) {
            mHeight = height;
            return this;
        }

        public Mp4Info build() {
            return new Mp4Info(
                    mDuration,
                    mWidth,
                    mHeight
            );
        }
    }
}
