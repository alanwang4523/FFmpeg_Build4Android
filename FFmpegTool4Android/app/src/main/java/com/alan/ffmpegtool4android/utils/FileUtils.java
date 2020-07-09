package com.alan.ffmpegtool4android.utils;

import android.text.TextUtils;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Author: AlanWang4523.
 * Date: 2020-07-06 18:10.
 * Mail: alanwang4523@gmail.com
 */
public class FileUtils {

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
}
