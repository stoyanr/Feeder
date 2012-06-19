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

import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

public class UrlUtils {
    private static final String TAG = "UrlUtils";

    private UrlUtils() {
    }

    public static URL getFaviconUrl(URL url) {
        URL result = null;
        try {
            result = new URL(url.getProtocol(), url.getHost(), url.getPort(),
                "/favicon.ico");
        } catch (MalformedURLException e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
        return result;
    }
}
