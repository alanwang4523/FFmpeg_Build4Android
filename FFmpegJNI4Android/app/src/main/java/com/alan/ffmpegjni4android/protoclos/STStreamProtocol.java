package com.alan.ffmpegjni4android.protoclos;

import android.util.Log;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/3 17:34.
 * Mail: alanwang4523@gmail.com
 */
public class STStreamProtocol implements IStreamProtocol {
    private static final String TAG = STStreamProtocol.class.getSimpleName();

    private IStreamProtocol streamProtocol;

    @Override
    public void open(String uri) {
        Log.e(TAG, "open()-->>" + uri);
        streamProtocol = new STFileProtocol();
        streamProtocol.open(uri);
    }

    @Override
    public int read(byte[] buffer, int offset, int size) {
        Log.e(TAG, "read()-->>offset = " + offset + " size = " + size);
        return streamProtocol.read(buffer, offset, size);
    }

    @Override
    public int seek(long position) {
        Log.e(TAG, "seek()-->>" + position);
        return streamProtocol.seek(position);
    }

    @Override
    public void close() {
        Log.e(TAG, "close()-->>");
        streamProtocol.close();
    }
}
