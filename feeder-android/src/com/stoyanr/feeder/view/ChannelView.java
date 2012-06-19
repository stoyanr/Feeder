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
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stoyanr.feeder.R;
import com.stoyanr.feeder.content.ContentManager;
import com.stoyanr.feeder.util.BitmapCache;

public class ChannelView extends RelativeLayout {

    private static int OFFSET = 10000;

    private ContentManager cm;
    private ImageView imageView;
    private TextView titleTextView;
    private TextView countTextView;
    private ProgressBar progressBar;

    public ChannelView(Context context, ViewGroup parent) {
        super(context);
        cm = ContentManager.getInstance(getContext().getContentResolver());
        initControls(context, parent);
    }

    private void initControls(Context context, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.channel_view, parent, false);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        countTextView = (TextView) view.findViewById(R.id.countTextView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        addView(view);
    }

    public void bindView(Cursor cursor) {
        long channelId = ContentManager.getChannelId(cursor);

        byte[] image = ContentManager.getChannelImage(cursor);
        if (image != null && image.length == 0) {
            image = ContentManager.getChannelIcon(cursor);
            imageView.setScaleType(ScaleType.CENTER);
        }
        if (image != null) {
            Bitmap bmp = BitmapCache.getBitmap(channelId + OFFSET, image);
            imageView.setImageBitmap(bmp);
        }

        int count = cm.getUnreadItemsCount(channelId);

        if (count == 0) {
            titleTextView.setTypeface(Typeface.DEFAULT);
        }
        titleTextView.setText(ContentManager.getChannelTitle(cursor));

        countTextView.setText((count > 0) ? new Integer(count).toString() : "");
    }

    public void startRefresh() {
        countTextView.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);
    }

    public void finishRefresh() {
        progressBar.setVisibility(GONE);
        countTextView.setVisibility(VISIBLE);
    }
}
