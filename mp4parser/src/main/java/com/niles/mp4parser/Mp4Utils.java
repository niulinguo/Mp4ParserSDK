package com.niles.mp4parser;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

/**
 * Created by Niles
 * Date 2018/11/20 10:18
 * Email niulinguo@163.com
 */
public class Mp4Utils {

    /**
     * 获取 MP4 文件信息
     */
    public static Mp4Info getMp4Info(String path) {
        // 解析文件
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);

        // 获取参数
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        if (BuildConfig.DEBUG) {
            Log.e("info", String.format(Locale.getDefault(), "duration:%s, width:%s, height:%s", duration, width, height));
        }

        Mp4Info mp4Info = null;

        // 如果 mp4 文件不完整，将获取不到信息
        if (!TextUtils.isEmpty(duration)
                && !"Null".equalsIgnoreCase(duration)) {
            mp4Info = new Mp4Info.Builder()
                    .setDuration(Integer.parseInt(duration))
                    .setWidth(Integer.parseInt(width))
                    .setHeight(Integer.parseInt(height))
                    .build();
        }

        retriever.release();

        return mp4Info;
    }

    public static Bitmap snap(String path, int time) {
        return snap(path, time, MediaMetadataRetriever.OPTION_CLOSEST);
    }

    /**
     * 获取视频的某一帧图片
     *
     * @param path   mp4文件路径
     * @param time   截图时间，单位秒
     * @param option 关键帧选项{@see MediaMetadataRetriever.OPTION_PREVIOUS_SYNC}
     *               {@see MediaMetadataRetriever.OPTION_NEXT_SYNC}
     *               {@see MediaMetadataRetriever.OPTION_CLOSEST_SYNC}
     *               {@see MediaMetadataRetriever.OPTION_CLOSEST}
     * @return 图片
     */
    public static Bitmap snap(String path, int time, int option) {
        // 解析文件
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);

        Bitmap frame = retriever.getFrameAtTime(time * 1000 * 1000, option);

        retriever.release();

        return frame;
    }

    /**
     * 裁剪一段视频
     *
     * @param path     mp4文件路径
     * @param fromTime 开始剪裁时间，单位秒
     * @param toTime   结束剪裁时间，单位秒
     * @param outPath  裁剪后mp4文件
     * @return 是否剪裁成功
     */
    public static boolean crop(String path, int fromTime, int toTime, String outPath) {
        try {

            // 获取 videoTrack
            Movie movie = MovieCreator.build(path);
            Track videoTrack = null;
            for (Track track : movie.getTracks()) {
                if ("vide".equals(track.getHandler())) {
                    videoTrack = track;
                }
            }

            // 没有 videoTrack，则返回失败
            if (videoTrack == null) {
                return false;
            }

            /*
            循环遍历，计算需要的视频是从哪一帧到哪一帧
            currTime：当前的时间，单位秒
            fromSample：起始帧
            toSample：结束帧
             */
            long[] sampleDurations = videoTrack.getSampleDurations();
            double currTime = 0;
            int currSample = 0;
            int fromSample = 0;
            int toSample = 0;
            for (long duration : sampleDurations) {

                // 定位起始帧
                if (currTime <= fromTime) {
                    fromSample = currSample;
                }

                // 定位结束帧
                if (currTime <= toTime) {
                    toSample = currSample;
                }

                // 累加当前时间和当前帧
                currTime += 1.0 * duration / videoTrack.getTrackMetaData().getTimescale();
                currSample += 1;
            }

            // 裁剪视频
            Movie resultMovie = new Movie();
            resultMovie.addTrack(new AppendTrack(new CroppedTrack(videoTrack, fromSample, toSample)));

            // 保存
            Container container = new DefaultMp4Builder().build(resultMovie);
            FileOutputStream outputStream = new FileOutputStream(new File(outPath));
            container.writeContainer(outputStream.getChannel());

            outputStream.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
