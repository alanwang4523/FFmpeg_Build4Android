package com.alan.ffmpegjni4android.protocols;

import java.io.InputStream;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/5 19:46.
 * Mail: alanwang4523@gmail.com
 */
public abstract class STBaseStreamProtocol implements IStreamProtocol {

    private static final int SEEK_SET = 0;
    private static final int SEEK_CUR = 1;
    private static final int SEEK_END = 2;

    private InputStream mInputStream;
    private long mStreamSize = -1;
    private long mCurPosition = 0;

    protected abstract InputStream getInputStream(String uriString);

    @Override
    public int open(String uriString) {
        mInputStream = getInputStream(uriString);
        if (mInputStream == null) {
            return ERROR_OPEN;
        }
        try {
            mStreamSize = mInputStream.available();
            return SUCCESS;
        } catch (Exception ignored) {
        }
        return ERROR_OPEN;
    }

    @Override
    public long getSize() {
        return mStreamSize;
    }

    @Override
    public int read(byte[] buffer, int offset, int size) {
        if (mInputStream != null) {
            try {
                int readLen = mInputStream.read(buffer, offset, size);
                mCurPosition += readLen;
                return readLen;
            } catch (Exception ignored) {
            }
        }
        return ERROR_READ;
    }

    @Override
    public int seek(long position, int whence) {
        if (mInputStream != null) {
            try {
                mCurPosition = mInputStream.skip(getSeekPosition(position, whence));
                return SUCCESS;
            } catch (Exception ignored) {
            }
        }
        return ERROR_SEEK;
    }

    @Override
    public void close() {
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (Exception ignored) {
            }
            mInputStream = null;
        }
    }

    private long getSeekPosition(long position, int whence) {
        long realSeekPos;
        if (whence == SEEK_SET) {
            realSeekPos = position;
        } else if (whence == SEEK_CUR) {
            realSeekPos = mCurPosition + position;
        } else if (whence == SEEK_END) {
            realSeekPos = mStreamSize - position;
        } else {
            realSeekPos = position;
        }
        return realSeekPos;
    }
}
