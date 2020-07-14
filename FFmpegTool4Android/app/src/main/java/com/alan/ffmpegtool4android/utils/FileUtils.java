package com.alan.ffmpegtool4android.utils;

import android.text.TextUtils;
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Author: AlanWang4523.
 * Date: 2020-07-06 18:10.
 * Mail: alanwang4523@gmail.com
 */
public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();

    /*
     * 常见磁盘空间常量所对应的字节数
     */
    public static final long SIZE_BYTE = 1;
    public static final long SIZE_KB = 1024 * SIZE_BYTE;
    public static final long SIZE_MB = 1024 * SIZE_KB;
    public static final long SIZE_GB = 1024 * SIZE_MB;
    public static final long SIZE_TB = 1024 * SIZE_GB;
    public static final long SIZE_PB = 1024 * SIZE_TB;

    public static String createIntputFilesListFile(String tempFileDir,
                                            List<String> inputFiles) {

        if (inputFiles == null || inputFiles.size() == 0) {
            throw new IllegalArgumentException("output or input can not be null or empty");
        }

        File tempFile = new File(tempFileDir, "input_files.txt");
        if (tempFile.exists()) {
            tempFile.delete();
        }
        StringBuilder content = new StringBuilder();
        for (String fileName : inputFiles) {
            content.append("file '")
                    .append(fileName)
                    .append("'\n");
        }

        writeFileWithSync(tempFile.getAbsolutePath(), content.toString());

        return tempFile.getAbsolutePath();
    }

    public static boolean writeFileWithSync(String filePath, String content) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }
        FileOutputStream os = null;
        try {
            makeDirs(filePath);
            os = new FileOutputStream(new File(filePath));
            os.write(content.getBytes());
            os.getFD().sync();
            return true;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            close(os);
        }
    }

    public static boolean makeDirs(String filePath) {
        String folderName = getFolderName(filePath);
        if (TextUtils.isEmpty(folderName)) {
            return false;
        }

        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) || folder.mkdirs();
    }

    public static String getFolderName(String filePath) {

        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? "" : filePath.substring(0, filePosi);
    }

    /**
     * Close closable object and wrap {@link IOException} with {@link RuntimeException}
     * @param closeable closeable object
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new RuntimeException("IOException occurred. ", e);
            }
        }
    }

    /**
     * 复制文件（夹）
     *
     * @param src 源文件
     * @param dst 目标文件
     * @return true表示复制成功
     */
    public static boolean copy(@NonNull File src, @NonNull File dst) {
        Log.e(TAG, "Copy file from " + src + " to " + dst);
        try {
            doCopy(src, dst);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }


    /**
     * 使用新IO接口实现文件快速复制
     *
     * @param src 源文件
     * @param dst 目标文件
     * @throws IOException
     */
    protected static void doCopy(File src, File dst) throws IOException {
        if (src.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            boolean result = dst.mkdirs();
            File[] subs = src.listFiles();
            for (File subsrc : subs) {
                File subdst = new File(dst, subsrc.getName());
                doCopy(subsrc, subdst);
            }
        } else {
            //noinspection ResultOfMethodCallIgnored
            if (dst.exists()) {
                boolean delete = dst.delete();
            }
            boolean newFile = dst.createNewFile();
            FileInputStream fis = null;
            FileChannel fic = null;
            FileOutputStream fos = null;
            FileChannel foc = null;
            try {
                fis = new FileInputStream(src);
                fic = fis.getChannel();
                fos = new FileOutputStream(dst);
                foc = fos.getChannel();
                long size = fic.size();
                long position = 0;
                do {
                    position += foc.transferFrom(fic, position, SIZE_MB);
                } while (position < size);
            } finally {
                closeQuietly(foc);
                closeQuietly(fos);
                closeQuietly(fic);
                closeQuietly(fis);
            }
        }
    }

    /**
     * 静默关闭可关闭对象
     *
     * @param closeable 待关闭对象
     * @return 如果关闭成功返回true
     */
    public static boolean closeQuietly(@Nullable Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
                return true;
            }
        } catch (Exception ex) {
            // Empty
        }
        return false;
    }
}
