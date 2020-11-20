package com.alan.ffmpegjni4android.protocols;

import androidx.annotation.Keep;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/3 17:34.
 * Mail: alanwang4523@gmail.com
 */
@Keep
public class StreamProtocol implements IStreamProtocol {
    private static final String TAG = StreamProtocol.class.getSimpleName();

    private IStreamProtocol streamProtocol;

    @Keep
    @Override
    public int open(String uriString) {
        streamProtocol = StreamProtocolFactory.create(uriString);
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
        int result = ERROR_READ;
        if (streamProtocol != null) {
            result = streamProtocol.read(buffer, offset, size);
            if (result == -1) {
                result = 0;
            }
        } else {
            result = ERROR_READ;
        }
        return result;
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
