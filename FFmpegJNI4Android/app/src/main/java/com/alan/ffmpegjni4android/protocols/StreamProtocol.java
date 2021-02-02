package com.alan.ffmpegjni4android.protocols;

import android.util.Log;
import java.nio.ByteBuffer;
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
        Log.e(TAG, "open()---->>" + uriString);
        streamProtocol = StreamProtocolFactory.create(uriString);
        if (streamProtocol == null) {
            return ERROR_OPEN;
        }
        return streamProtocol.open(uriString);
    }

    @Keep
    @Override
    public long getSize() {
        long size;
        if (streamProtocol != null) {
            size = streamProtocol.getSize();
        } else {
            size = ERROR_GET_SIZE;
        }
        Log.e(TAG, "getSize()---->>" + size);
        return size;
    }

    @Keep
    @Override
    public int read(ByteBuffer buffer, int offset, int size) {
        int result;
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
        int result = 0;
        if (streamProtocol != null) {
            result = streamProtocol.seek(position, whence);
        } else {
            result = ERROR_SEEK;
        }
        Log.e(TAG, "seek()---->>position = " + position + ", whence = " + whence);
        return result;
    }

    @Keep
    @Override
    public void close() {
        Log.e(TAG, "close()---->>");
        if (streamProtocol != null) {
            streamProtocol.close();
            streamProtocol = null;
        }
    }
}
