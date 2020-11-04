package com.alan.ffmpegjni4android.protoclos;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/3 17:34.
 * Mail: alanwang4523@gmail.com
 */
public class STStreamProtocol implements IStreamProtocol {

    private IStreamProtocol streamProtocol;

    @Override
    public void open(String uri) {
        streamProtocol = new STFileProtocol();
        streamProtocol.open(uri);
    }

    @Override
    public int read(byte[] buffer, int offset, int size) {
        return streamProtocol.read(buffer, offset, size);
    }

    @Override
    public int seek(long position) {
        return streamProtocol.seek(position);
    }

    @Override
    public void close() {
        streamProtocol.close();
    }
}
