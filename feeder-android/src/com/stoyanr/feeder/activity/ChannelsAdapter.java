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

package com.stoyanr.feeder.activity;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;

import com.stoyanr.feeder.content.ContentManager;
import com.stoyanr.feeder.view.ChannelView;

public class ChannelsAdapter extends CursorAdapter implements Filterable {

    private HashMap<Long, ChannelView> views;

    public ChannelsAdapter(Context context, Cursor c) {
        super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
        views = new HashMap<Long, ChannelView>();
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((ChannelView) view).bindView(cursor);
        putView(cursor, (ChannelView) view);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ChannelView view = new ChannelView(context, parent);
        view.bindView(cursor);
        putView(cursor, view);
        return view;
    }

    public ChannelView getView(long id) {
        return views.get(id);
    }

    private void putView(Cursor cursor, ChannelView view) {
        views.put(ContentManager.getChannelId(cursor), view);
    }
}