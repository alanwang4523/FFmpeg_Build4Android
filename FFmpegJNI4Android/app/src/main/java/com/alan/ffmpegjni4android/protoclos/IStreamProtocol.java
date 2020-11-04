package com.alan.ffmpegjni4android.protoclos;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/3 17:36.
 * Mail: alanwang4523@gmail.com
 */
public interface IStreamProtocol {

    void open(String uri);

    int read(byte[] buffer, int offset, int size);

    int seek(long position);

    void close();
}
