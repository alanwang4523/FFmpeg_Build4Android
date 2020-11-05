package com.alan.ffmpegjni4android.protocols;

import android.content.Context;
import android.text.TextUtils;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/5 19:10.
 * Mail: alanwang4523@gmail.com
 */
public class STStreamProtocolFactory {

    public final static String SCHEME_ASSET = "assets://";
    public final static String SCHEME_CONTENT = "content://";

    private static Context sAppContext;

    public static Context getAppContext() {
        return sAppContext;
    }

    public static void setAppContext(Context appContext) {
        STStreamProtocolFactory.sAppContext = appContext.getApplicationContext();
    }

    public static IStreamProtocol create(String uriString) {
        if (TextUtils.isEmpty(uriString)) {
            return null;
        }
        if (uriString.startsWith(SCHEME_ASSET)) {
            return new STAssetProtocol(sAppContext);
        } else if (uriString.startsWith(SCHEME_CONTENT)) {
            return new STContentProtocol(sAppContext);
        } else {
            return new STFileProtocol();
        }
    }
}
