package com.alan.ffmpegjni4android.protocols;

import android.util.Log;

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
    public int open(String uri) {
        try {
            mFile = new RandomAccessFile(uri, "r");
            Log.e("STFileProtocol", "open()--->length = " + mFile.length());
            return 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int size) {
        try {
            int readLen =  mFile.read(buffer, offset, size);
            Log.e("STFileProtocol", "read()--->readLen = " + readLen);
            return readLen;
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
