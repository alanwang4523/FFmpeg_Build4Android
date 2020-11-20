package com.alan.ffmpegjni4android.protocols;

import java.io.InputStream;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/5 19:46.
 * Mail: alanwang4523@gmail.com
 */
abstract class InputStreamProtocol implements IStreamProtocol {

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
            if (mInputStream.markSupported()) {
                mInputStream.mark((int) mStreamSize);
            }
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
                long posNeedSeekTo = getSeekPosition(position, whence);
                long needSkipLen = posNeedSeekTo - mCurPosition;
                long skipLen;
                if (needSkipLen < 0) {
                    // 往回跳转
                    if (mInputStream.markSupported()) {
                        mInputStream.reset();
                        mCurPosition = 0;
                        needSkipLen = posNeedSeekTo;
                    } else {
                        return ERROR_SEEK;
                    }
                }
                do {
                    skipLen = mInputStream.skip(needSkipLen);
                    mCurPosition += skipLen;
                    needSkipLen -= skipLen;
                } while (needSkipLen > 0);
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

    /**
     * 获取需要跳转到的绝对位置
     * @param position 需要跳转
     * @param whence 跳转方式
     * @return 需要调整到的绝对位置
     */
    private long getSeekPosition(long position, int whence) {
        long posNeedSeekTo;
        if (whence == SEEK_SET) {
            posNeedSeekTo = position;
        } else if (whence == SEEK_CUR) {
            posNeedSeekTo = mCurPosition + position;
        } else if (whence == SEEK_END) {
            posNeedSeekTo = mStreamSize - position;
        } else {
            posNeedSeekTo = position;
        }
        return posNeedSeekTo;
    }
}
