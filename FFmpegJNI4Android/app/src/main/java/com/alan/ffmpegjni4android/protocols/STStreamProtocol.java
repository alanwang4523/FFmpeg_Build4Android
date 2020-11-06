package com.alan.ffmpegjni4android.protocols;

import androidx.annotation.Keep;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/3 17:34.
 * Mail: alanwang4523@gmail.com
 */
@Keep
public class STStreamProtocol implements IStreamProtocol {
    private static final String TAG = STStreamProtocol.class.getSimpleName();

    private IStreamProtocol streamProtocol;

    @Keep
    @Override
    public int open(String uriString) {
        streamProtocol = STStreamProtocolFactory.create(uriString);
        if (streamProtocol == null) {
            return ERROR_OPEN;
        }
        return streamProtocol.open(uriString);
    }

    @Keep
    @Override
    public long getSize() {
        if (streamProtocol != null) {
            return streamProtocol.getSize();
        } else {
            return ERROR_GET_SIZE;
        }
    }

    @Keep
    @Override
    public int read(byte[] buffer, int offset, int size) {
        if (streamProtocol != null) {
            return streamProtocol.read(buffer, offset, size);
        } else {
            return ERROR_READ;
        }
    }

    @Keep
    @Override
    public int seek(long position, int whence) {
        if (streamProtocol != null) {
            return streamProtocol.seek(position, whence);
        } else {
            return ERROR_SEEK;
        }
    }

    @Keep
    @Override
    public void close() {
        if (streamProtocol != null) {
            streamProtocol.close();
            streamProtocol = null;
        }
    }
}
