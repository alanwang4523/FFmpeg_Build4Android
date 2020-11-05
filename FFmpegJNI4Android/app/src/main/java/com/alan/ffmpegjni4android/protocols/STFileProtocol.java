package com.alan.ffmpegjni4android.protocols;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/3 19:20.
 * Mail: alanwang4523@gmail.com
 */
public class STFileProtocol extends STBaseStreamProtocol {

    @Override
    protected InputStream getInputStream(String uriString) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(uriString));
        } catch (Exception ignored) {
        }
        return inputStream;
    }
}
