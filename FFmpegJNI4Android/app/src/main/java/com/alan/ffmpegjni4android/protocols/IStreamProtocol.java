package com.alan.ffmpegjni4android.protocols;

import androidx.annotation.Keep;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/3 17:36.
 * Mail: alanwang4523@gmail.com
 */
@Keep
public interface IStreamProtocol {

    int SUCCESS        = 0;
    int ERROR_OPEN     = -1;
    int ERROR_GET_SIZE = -2;
    int ERROR_READ     = -3;
    int ERROR_SEEK     = -4;

    @Keep
    int open(String uriString);

    @Keep
    long getSize();

    @Keep
    int read(byte[] buffer, int offset, int size);

    @Keep
    int seek(long position, int whence);

    @Keep
    void close();
}
