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

package com.stoyanr.feeder.view;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stoyanr.feeder.R;
import com.stoyanr.feeder.content.ContentManager;
import com.stoyanr.feeder.util.BitmapCache;

public class ItemView extends RelativeLayout {

    private static int OFFSET = 100000;

    private ImageView imageView;
    private TextView titleTextView;

    public ItemView(Context context, ViewGroup parent) {
        super(context);
        initControls(context, parent);
    }

    private void initControls(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_view, parent, false);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        addView(view);
    }

    public void bindView(Cursor cursor) {
        long itemId = ContentManager.getChannelId(cursor);
        byte[] image = ContentManager.getItemImage(cursor);
        if (image != null) {
            Bitmap bmp = BitmapCache.getBitmap(itemId + OFFSET, image);
            imageView.setImageBitmap(bmp);
        }

        if (ContentManager.isItemRead(cursor)) {
            titleTextView.setTypeface(Typeface.DEFAULT);
        }

        titleTextView.setText(ContentManager.getItemTitle(cursor));
    }
}
