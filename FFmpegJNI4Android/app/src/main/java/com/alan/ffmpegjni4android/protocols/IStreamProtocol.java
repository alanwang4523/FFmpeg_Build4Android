package com.alan.ffmpegjni4android.protocols;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/3 17:36.
 * Mail: alanwang4523@gmail.com
 */
public interface IStreamProtocol {

    int open(String uri);

    long getSize();

    int read(byte[] buffer, int offset, int size);

    int seek(long position, int whence);

    void close();
}
