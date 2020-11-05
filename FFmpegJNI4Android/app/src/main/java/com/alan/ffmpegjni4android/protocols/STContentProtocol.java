package com.alan.ffmpegjni4android.protocols;

import android.content.Context;
import android.net.Uri;
import java.io.InputStream;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/5 19:35.
 * Mail: alanwang4523@gmail.com
 */
public class STContentProtocol extends STBaseStreamProtocol {

    private Context mContext;

    public STContentProtocol(Context context) {
        mContext = context;
    }

    @Override
    protected InputStream getInputStream(String uriString) {
        if (mContext == null) {
            return null;
        }
        InputStream inputStream = null;
        Uri uri = Uri.parse(uriString);
        try {
            inputStream = mContext.getContentResolver().openInputStream(uri);
            mContext = null;
        } catch (Exception ignored) {
        }
        return inputStream;
    }
}
