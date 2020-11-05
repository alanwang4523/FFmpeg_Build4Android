package com.alan.ffmpegjni4android;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alan.ffmpegjni4android.protoclos.RuntimePermissionsHelper;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

//    private static final String TEST_CMD_STR = "ffmpeg -i /sdcard/Alan/ffmpeg/test.mp4";
    private static final String TEST_CMD_STR = "ffmpeg -i /sdcard/Alan/ffmpeg/test.mp4 -vcodec copy /sdcard/Alan/ffmpeg/test_out.mp4";

    // TODO 多视频合并 都 OK
//     -y -f concat -safe 0 -i /sdcard/Alan/ffmpeg/src/input_files.txt -c copy /sdcard/Alan/ffmpeg/output_concat.mp4
//    private static final String TEST_CMD_STR = " -y -f concat -safe 0 -i /sdcard/Alan/ffmpeg/src/input_files.txt " +
//            "-c copy /sdcard/Alan/ffmpeg/output_concat.mp4";

    // TODO 音频 + 图片 生成视频用于分享 OK
    // -y -loop 1 -r 0.1 -i /sdcard/Alan/ffmpeg/src/image_01.png -i /sdcard/Alan/ffmpeg/src/music.m4a -c:a copy -c:v libopenh264 -allow_skip_frames 1 -g 30 -r 15 -t 9000 -shortest /sdcard/Alan/ffmpeg/av_video.mp4
//    private static final String TEST_CMD_STR = " -y -loop 1 -r 0.1 -i /sdcard/Alan/ffmpeg/src/image_01.png -i /sdcard/Alan/ffmpeg/src/music.m4a " +
//            "-c:a copy -c:v libopenh264 -allow_skip_frames 1 -g 30 -r 15 -t 9000 -shortest /sdcard/Alan/ffmpeg/av_video.mp4";

    // TODO 音频 + 视频 Muxer OK
    // -y -i /sdcard/Alan/ffmpeg/src/st_test_01.mp4 -i /sdcard/Alan/ffmpeg/src/music.m4a -c copy /sdcard/Alan/ffmpeg/muxer_out.mp4
//    private static final String TEST_CMD_STR = String.format(" -y -i %s -i %s -c copy %s",
//            "/sdcard/Alan/ffmpeg/src/st_test_01.mp4", "/sdcard/Alan/ffmpeg/src/music.m4a", "/sdcard/Alan/ffmpeg/muxer_out.mp4");

    // TODO 音视频旋转 OK
    // -y -i /sdcard/Alan/ffmpeg/src/st_test_01.mp4 -c copy -metadata:s:v rotate=180 /sdcard/Alan/ffmpeg/out_rotate.mp4
//    private static final String TEST_CMD_STR = String.format(" -y -i %s -c copy -metadata:s:v rotate=%d %s",
//            "/sdcard/Alan/ffmpeg/src/st_test_01.mp4", 180, "/sdcard/Alan/ffmpeg/out_rotate.mp4");

    // TODO 音视频压缩 OK
    // -y -i /sdcard/Alan/ffmpeg/4.mp4 -threads 4 -b:v 1000k -vcodec libopenh264 -async 1 -vsync 1 -f mp4 -movflags faststart -vf scale=480:-1 -strict -2 -an /sdcard/Alan/ffmpeg/4_video.mp4
//    private static final String TEST_CMD_STR = String.format(Locale.US, " -y -i %s -threads 4 -b:v %dk -vcodec libopenh264 " +
//                    "-async 1 -vsync 1 -f mp4 -movflags faststart -vf scale=%d:-1 -strict -2 -an %s",
//            "/sdcard/Alan/ffmpeg/4.mp4", 1000, 480, "/sdcard/Alan/ffmpeg/4_video.mp4");

    // TODO 音视频分离 OK
    // -y -i /sdcard/Alan/ffmpeg/4.mp4 -threads 4 -vcodec copy -f mp4 -movflags faststart -strict -2 -an /sdcard/Alan/ffmpeg/4_video.mp4 -acodec aac -vn /sdcard/Alan/ffmpeg/4_audio.aac
//    private static final String TEST_CMD_STR = String.format(Locale.US, " -y -i %s -threads 4 " +
//                    "-vcodec copy -f mp4 -movflags faststart -strict -2 -an %s -acodec aac -vn %s",
//        "/sdcard/Alan/ffmpeg/4.mp4", "/sdcard/Alan/ffmpeg/4_video.mp4", "/sdcard/Alan/ffmpeg/4_audio.aac");

    // TODO 音视频裁剪 OK
    // -y -i /sdcard/Alan/ffmpeg/4.mp4 -threads 4 -b:v 200k -vcodec copy -acodec copy -ss 00:00:10 -t 00:00:30 -f mp4 -movflags faststart -strict -2 -an /sdcard/Alan/ffmpeg/out_trim.mp4
//    private static final String TEST_CMD_STR = String.format(Locale.US, " -y -i %s -threads 4 -b:v %dk " +
//                    "-vcodec copy -acodec copy -ss 00:00:10 -t 00:00:30 -f mp4 -movflags faststart -strict -2 -an %s",
//            "/sdcard/Alan/ffmpeg/4.mp4", 200, "/sdcard/Alan/ffmpeg/out_trim.mp4");

    // TODO 加水印 Ok
    // -y -i /sdcard/Alan/ffmpeg/test.mp4 -i /sdcard/Alan/ffmpeg/video_watermark.png -b:v 5120k -acodec copy -filter_complex "[1]scale=200:-1[water];[0][water]overlay=main_w-overlay_w-20:main_h-overlay_h-20" -filter_complex_threads 4  -f mp4 -vcodec libopenh264 /sdcard/Alan/ffmpeg/out_watermark.mp4
//    private static final String TEST_CMD_STR = String.format(Locale.US, " -y -i %s -i %s -b:v %dk -acodec copy " +
//                    "-filter_complex \"[1]scale=%d:-1[water];[0][water]overlay=main_w-overlay_w-%d:main_h-overlay_h-%d\" " +
//                    "-filter_complex_threads 4  -f mp4 -vcodec libopenh264 %s",
//        "/sdcard/Alan/ffmpeg/test.mp4", "/sdcard/Alan/ffmpeg/video_watermark.png", 5 * 1024, 200, 20, 20, "/sdcard/Alan/ffmpeg/out_watermark.mp4");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RuntimePermissionsHelper runtimePermissionsHelper = RuntimePermissionsHelper.create(this, null,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!runtimePermissionsHelper.allPermissionsGranted()) {
            runtimePermissionsHelper.makeRequest();
        }

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exeFFmpegCommand(TEST_CMD_STR.trim());
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native int exeFFmpegCommand(String cmdStr);
}
