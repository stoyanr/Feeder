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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapCache {

    private static Map<Long, Bitmap> bitmaps = new HashMap<Long, Bitmap>();
    private static List<Bitmap> bitmapsToRecycle = new ArrayList<Bitmap>();

    private BitmapCache() {
    }

    public static synchronized Bitmap getBitmap(long id, byte[] image) {
        Bitmap bitmap = bitmaps.get(id);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            if (bitmap != null) {
                bitmaps.put(id, bitmap);
            }
        }
        return bitmap;
    }

    public static synchronized void clear() {
        for (long id : bitmaps.keySet()) {
            Bitmap bitmap = bitmaps.get(id);
            assert (bitmap != null);
            bitmapsToRecycle.add(bitmap);
        }
        bitmaps.clear();
    }

    public static synchronized void recycle() {
        for (Bitmap bitmap : bitmapsToRecycle) {
            bitmap.recycle();
        }
        bitmapsToRecycle.clear();
    }

}
