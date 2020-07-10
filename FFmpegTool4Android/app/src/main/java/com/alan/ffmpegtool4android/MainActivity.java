package com.alan.ffmpegtool4android;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.alan.ffmpegtool4android.utils.FFmpegTool;
import com.alan.ffmpegtool4android.utils.FileUtils;
import com.alan.ffmpegtool4android.utils.RuntimePermissionsManager;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // TODO getMediaInfo  OK
    // TODO getVideoCodec OK
    private static final String TEST_CMD_STR = " -i /sdcard/Alan/ffmpeg/test.mp4";

    // TODO 多视频合并 都 OK
    // -y -f concat -safe 0 -i /sdcard/Alan/ffmpeg/src/input_files.txt -c copy /sdcard/Alan/ffmpeg/output_concat.mp4
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

    // TODO 加水印 Ok [ VideoWaterMarkUtil.kt markVideo() #line 65 ]
    // -y -i /sdcard/Alan/ffmpeg/test.mp4 -i /sdcard/Alan/ffmpeg/video_watermark.png -b:v 5120k -acodec copy -filter_complex "[1]scale=200:-1[water];[0][water]overlay=main_w-overlay_w-20:main_h-overlay_h-20" -filter_complex_threads 4  -f mp4 -vcodec libopenh264 /sdcard/Alan/ffmpeg/out_watermark.mp4
//    private static final String TEST_CMD_STR = String.format(Locale.US, " -y -i %s -i %s -b:v %dk -acodec copy " +
//                    "-filter_complex \"[1]scale=%d:-1[water];[0][water]overlay=main_w-overlay_w-%d:main_h-overlay_h-%d\" " +
//                    "-filter_complex_threads 4  -f mp4 -vcodec libopenh264 %s",
//        "/sdcard/Alan/ffmpeg/test.mp4", "/sdcard/Alan/ffmpeg/video_watermark.png", 5 * 1024, 200, 20, 20, "/sdcard/Alan/ffmpeg/out_watermark.mp4");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RuntimePermissionsManager runtimePermissionsHelper = new RuntimePermissionsManager(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!runtimePermissionsHelper.isAllPermissionsGranted()) {
            runtimePermissionsHelper.makeRequest();
        }

        TextView tvTest = findViewById(R.id.tvTest);
        tvTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testFFmpeg();
//                testCreateInputFileList();
            }
        });
    }

    private void testFFmpeg() {
        FFmpegTool.get(MainActivity.this).execute(TEST_CMD_STR, new FFmpegTool.Listener() {
            @Override
            public void onComplete(boolean success) {
                Log.e("MainActivity", "execute onComplete!");
            }

            @Override
            public void onPrintInfo(boolean error, String line) {
                Log.e("MainActivity", "error = " + error + ", msg : " + line);
            }
        });
    }

    private void testCreateInputFileList() {
        List<String> pathList = new ArrayList<>();
        pathList.add("/sdcard/Alan/ffmpeg/src/st_test_01.mp4");
        pathList.add("/sdcard/Alan/ffmpeg/src/st_test_02.mp4");
        pathList.add("/sdcard/Alan/ffmpeg/src/st_test_03.mp4");

        FileUtils.createIntputFilesListFile("/sdcard/Alan/ffmpeg/src", pathList);
    }
}
