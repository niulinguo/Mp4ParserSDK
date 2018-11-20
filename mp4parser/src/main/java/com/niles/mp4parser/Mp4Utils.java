package com.niles.mp4parser;

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

    public static Mp4Info getMp4Info(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);

        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        if (BuildConfig.DEBUG) {
            Log.e("info", String.format(Locale.getDefault(), "duration:%s, width:%s, height:%s", duration, width, height));
        }

        Mp4Info mp4Info = null;

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

    public static boolean crop(String path, long fromTime, long toTime, String outPath) {
        try {

            Movie movie = MovieCreator.build(path);
            Track videoTrack = null;
            for (Track track : movie.getTracks()) {
                if ("vide".equals(track.getHandler())) {
                    videoTrack = track;
                }
            }

            if (videoTrack == null) {
                return false;
            }

            long[] sampleDurations = videoTrack.getSampleDurations();
            double currTime = 0;
            int currSample = 0;
            int fromSample = 0;
            int toSample = 0;
            for (long duration : sampleDurations) {

                if (currTime <= fromTime) {
                    fromSample = currSample;
                }

                if (currTime <= toTime) {
                    toSample = currSample;
                }

                currTime += 1.0 * duration / videoTrack.getTrackMetaData().getTimescale();
                currSample += 1;
            }

            Movie resultMovie = new Movie();
            resultMovie.addTrack(new AppendTrack(new CroppedTrack(videoTrack, fromSample, toSample)));

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
