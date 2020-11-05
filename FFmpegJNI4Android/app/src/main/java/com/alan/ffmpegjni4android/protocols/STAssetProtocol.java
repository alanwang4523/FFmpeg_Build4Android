package com.alan.ffmpegjni4android.protocols;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/5 19:32.
 * Mail: alanwang4523@gmail.com
 */
public class STAssetProtocol extends STBaseStreamProtocol {

    private Context mContext;

    public STAssetProtocol(Context context) {
        mContext = context;
    }

    @Override
    protected InputStream getInputStream(String uri) {
        if (mContext == null) {
            return null;
        }
        InputStream inputStream = null;
        String fileName = uri.replace(STStreamProtocolFactory.SCHEME_ASSET, "");
        try {
            inputStream = mContext.getAssets().open(fileName);
            mContext = null;
        } catch (Exception ignored) {
        }
        return inputStream;
    }
}
