package com.alan.ffmpegtool4android.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import dalvik.system.PathClassLoader;

/**
 * Author: AlanWang4523.
 * Date: 2020-07-09 20:56.
 * Mail: alanwang4523@gmail.com
 */
public class FFmpegTool {
    private final static String TAG = FFmpegTool.class.getSimpleName();

    public interface Listener {
        void onComplete(boolean success);
        void onPrintInfo(boolean error, String line);
    }


    private final static String FFMPEG_TOOL_NAME = "libffmpeg_tool.so"; // depend on libffmpeg.so

    private static FFmpegTool mInstance;
    private Context mContext;

    public static FFmpegTool get(Context context) {
        if (mInstance == null) {
            synchronized (FFmpegTool.class) {
                mInstance = new FFmpegTool(context);
            }
        }
        return mInstance;
    }

    private FFmpegTool(Context context) {
        this.mContext = context.getApplicationContext();
    }

    /**
     * 执行 ffmpeg 命令
     * @param cmd cmd
     * @param listener listener
     * @return error code
     */
    public int execute(String cmd, Listener listener) {
        if (TextUtils.isEmpty(cmd)) {
            return -1;
        }
        int ret = -1;
        Process process = null;
        try {
            process = exec(cmd);
            if (process == null) {
                if (listener != null) {
                    listener.onPrintInfo(true, "There is no " + FFMPEG_TOOL_NAME);
                    listener.onComplete(false);
                }
                return ret;
            }

            String line;
            BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = stderr.readLine()) != null) {
                if (listener != null) {
                    listener.onPrintInfo(true, line);
                }
            }

            BufferedReader std = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = std.readLine()) != null) {
                if (listener != null) {
                    listener.onPrintInfo(false, line);
                }
            }

            ret = process.waitFor();
            if (listener != null) {
                listener.onComplete(ret == 0);
            }
        } catch (Exception e) {
            Log.e(TAG, "exe error", e);
            if (listener != null) {
                listener.onPrintInfo(true, e.getMessage());
                listener.onComplete(false);
            }
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return ret;
    }

    private Process exec(String cmd) throws IOException {
        String filepath = getSoFilePath();
        if (!TextUtils.isEmpty(filepath)) {
            String nativeLibrariesPath = mContext.getApplicationContext().getApplicationInfo().nativeLibraryDir;
            Log.d(TAG , "nativeLibrariesPath : " + nativeLibrariesPath);
            String[] envp = {"LD_LIBRARY_PATH=" + nativeLibrariesPath};

            String _cmd = String.format("%s %s", filepath, cmd);
            Log.d("FFmpegUtils", "_cmd:" + _cmd);
            //If the -c option is present, then commands are read from string. If there are arguments after the string, they are assigned to the positional parameters, starting with $0.
            String[] commands = { "sh", "-c", _cmd };
            return Runtime.getRuntime().exec(commands, envp);
        } else {
            return null;
        }
    }

    private String getSoFilePath() {
        String filepath = "";
        String path = mContext.getApplicationContext().getApplicationInfo().nativeLibraryDir;
        File file = new File(path, FFMPEG_TOOL_NAME);
        if (file.exists()) {
            filepath = file.getAbsolutePath();
        } else {
            try {
                Object exSoFilePathList = getExSoFilePathList();
                if (exSoFilePathList instanceof List) {
                    ArrayList<File> array = (ArrayList<File>) exSoFilePathList;
                    if (array.size() > 0) {
                        for (int i= 0; i < array.size(); i++) {
                            File soFile = array.get(i);
                            if (soFile.exists() && soFile.isDirectory()) {
                                file = new File(soFile.getAbsoluteFile(), FFMPEG_TOOL_NAME);
                                if (file.exists()) {
                                    filepath = file.getAbsolutePath();
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filepath;
    }

    private Object getExSoFilePathList() {
        try {
            PathClassLoader pathClassLoader = (PathClassLoader) (this.getClass().getClassLoader());
            Object pathList = getPathList(pathClassLoader);
            Object nativeLibraryDirectories = pathList.getClass().getDeclaredField("nativeLibraryDirectories");
            ((Field) nativeLibraryDirectories).setAccessible(true);
            //获取 DEXPathList 中的属性值
            return ((Field) nativeLibraryDirectories).get(pathList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object getPathList(Object obj) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return getField(obj, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object getField(Object obj, Class cls, String str) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cls.getDeclaredField(str);
        declaredField.setAccessible(true);
        return declaredField.get(obj);
    }
}
