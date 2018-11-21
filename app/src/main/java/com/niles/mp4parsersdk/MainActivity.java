package com.niles.mp4parsersdk;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.niles.mp4parser.Mp4Info;
import com.niles.mp4parser.Mp4Utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;
    private static final String VIDEO_PATH = "hobot/media/adas/video";
    private static final Pattern PATTERN = Pattern.compile("^ADAS_RAW_Nebula_([0-9]{8}-[0-9]{6}_[0-9]{3})\\.mp4$");
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss_SSS", Locale.getDefault());
    private ImageView mImageView;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bitmap bitmap = (Bitmap) msg.obj;
            mImageView.setImageBitmap(bitmap);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.iv_image);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        break;
                    }
                }
                break;
            }
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    public void onButtonClicked(View view) {
        File dir = new File(Environment.getExternalStorageDirectory(), VIDEO_PATH);
        if (!dir.exists()) {
            Toast.makeText(this, dir.getAbsolutePath() + "不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            Log.e("file", file.getAbsolutePath());
            Matcher matcher = PATTERN.matcher(file.getName());
            if (matcher.matches()) {
                String time = matcher.group(1);
                try {
                    Date date = DATE_FORMAT.parse(time);
                    Mp4Info mp4Info = Mp4Utils.getMp4Info(file.getAbsolutePath());
                    if (mp4Info != null) {
                        Log.e("crop_file", file.getAbsolutePath());
                        File outFile = new File(dir, file.getName() + "crop.mp4");
                        if (outFile.exists()) {
                            outFile.delete();
                        }
                        outFile.createNewFile();
//                        Mp4Utils.crop(file.getAbsolutePath(), 0, 10, outFile.getAbsolutePath());
                        for (int i = 0; i < 10; i++) {
                            Bitmap snap = Mp4Utils.snap(file.getAbsolutePath(), i);
                            if (snap != null) {
                                Message message = Message.obtain();
                                message.what = 0;
                                message.obj = snap;
                                mHandler.sendMessageDelayed(message, i * 1000);
                            }
                        }
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
