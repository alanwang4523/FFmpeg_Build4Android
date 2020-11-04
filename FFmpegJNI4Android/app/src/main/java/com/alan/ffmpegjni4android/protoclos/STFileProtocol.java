package com.alan.ffmpegjni4android.protoclos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/3 19:20.
 * Mail: alanwang4523@gmail.com
 */
public class STFileProtocol implements IStreamProtocol {

    private RandomAccessFile mFile;

    @Override
    public void open(String uri) {
        try {
            mFile = new RandomAccessFile(uri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int size) {
        try {
            return mFile.read(buffer, offset, size);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int seek(long position) {
        try {
            mFile.seek(position);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void close() {
        try {
            mFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
