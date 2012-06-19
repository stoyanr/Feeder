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
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stoyanr.feeder.util.BitmapCache;

public class ChannelHeader extends LinearLayout {
    private ImageView iconImageView;
    private TextView titleTextView;
    private TextView descTextView;

    private static final int PAD_TOP = 2;
    private static final int PAD_BOTTOM = 0;
    private static final int PAD_LEFT = 6;
    private static final int PAD_RIGHT = 0;

    private static final int ICON_SIZE = 32;

    public ChannelHeader(Context context) {
        super(context);
        createViews(context);
    }

    public ChannelHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        createViews(context);
    }

    private void createViews(Context context) {
        createIconView();
        LinearLayout layout = createLayout();
        createTitleView(layout);
        createDescView(layout);
    }

    private void createIconView() {
        iconImageView = new ImageView(getContext());
        iconImageView.setPadding(PAD_LEFT, PAD_TOP, PAD_RIGHT, PAD_BOTTOM);
        LayoutParams params = new LayoutParams(ICON_SIZE + PAD_LEFT, ICON_SIZE
            + PAD_TOP + PAD_BOTTOM);
        params.gravity = Gravity.CENTER_VERTICAL;
        addView(iconImageView, params);
    }

    private LinearLayout createLayout() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(VERTICAL);
        addView(layout, new LayoutParams(LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT));
        return layout;
    }

    private void createTitleView(LinearLayout layout) {
        titleTextView = new TextView(getContext());
        titleTextView.setTextAppearance(getContext(),
            android.R.style.TextAppearance_Medium);
        titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
        titleTextView.setPadding(PAD_LEFT, PAD_TOP, PAD_RIGHT, PAD_BOTTOM);
        titleTextView.setMaxLines(1);
        layout.addView(titleTextView, new LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    private void createDescView(LinearLayout layout) {
        descTextView = new TextView(getContext());
        descTextView.setTextAppearance(getContext(),
            android.R.style.TextAppearance_Small);
        descTextView.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
        descTextView.setPadding(PAD_LEFT, PAD_BOTTOM, PAD_RIGHT, PAD_TOP);
        descTextView.setGravity(Gravity.TOP);
        descTextView.setMaxLines(1);
        layout.addView(descTextView, new LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    public void setData(long id, String title, String description, byte[] icon) {
        titleTextView.setText(title);
        descTextView.setText(description);
        if (icon != null) {
            Bitmap bmp = BitmapCache.getBitmap(id, icon);
            iconImageView.setImageBitmap(bmp);
        }
    }
}
