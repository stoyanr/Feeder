/*
 * $Id: $
 *
 * Copyright (C) 2012 Stoyan Rachev (stoyanr@gmail.com)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */

package com.stoyanr.feeder.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.util.Log;

public class IOUtils {
    private static final String TAG = "IOUtils";

    private IOUtils() {
    }

    public static byte[] getByteArray(URL url) {
        byte[] result = null;
        InputStream in = null;
        try {
            in = url.openStream();
            result = IOUtils.getByteArray(in);
        } catch (IOException e) {
            Log.d(TAG, Log.getStackTraceString(e));
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
            }
        }
        return result;
    }

    public static byte[] getByteArray(InputStream is) {
        byte[] result = null;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            result = buffer.toByteArray();
        } catch (IOException e) {
            Log.d(TAG, Log.getStackTraceString(e));
        } finally {
            try {
                buffer.close();
            } catch (IOException e) {
            }
        }
        return result;
    }

}
