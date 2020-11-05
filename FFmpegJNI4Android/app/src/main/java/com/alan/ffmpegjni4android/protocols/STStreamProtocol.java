package com.alan.ffmpegjni4android.protocols;

import android.util.Log;
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

    @Override
    public int open(String uriString) {
        Log.e(TAG, "open()-->>" + uriString);
        streamProtocol = STStreamProtocolFactory.create(uriString);
        if (streamProtocol == null) {
            return ERROR_OPEN;
        }
        return streamProtocol.open(uriString);
    }

    @Override
    public long getSize() {
        if (streamProtocol != null) {
            Log.e(TAG, "getSize()-->>" + streamProtocol.getSize());
            return streamProtocol.getSize();
        } else {
            return ERROR_GET_SIZE;
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int size) {
        Log.e(TAG, "read()-->>offset = " + offset + " size = " + size);
        if (streamProtocol != null) {
            return streamProtocol.read(buffer, offset, size);
        } else {
            return ERROR_READ;
        }
    }

    @Override
    public int seek(long position, int whence) {
        Log.e(TAG, "seek()-->>" + position + ", whence = " + whence);
        if (streamProtocol != null) {
            return streamProtocol.seek(position, whence);
        } else {
            return ERROR_SEEK;
        }
    }

    @Override
    public void close() {
        Log.e(TAG, "close()-->>");
        if (streamProtocol != null) {
            streamProtocol.close();
            streamProtocol = null;
        }
    }
}
