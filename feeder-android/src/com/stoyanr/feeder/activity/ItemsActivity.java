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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.stoyanr.feeder.R;
import com.stoyanr.feeder.content.ContentManager;
import com.stoyanr.feeder.model.Channel;
import com.stoyanr.feeder.model.Item;
import com.stoyanr.feeder.util.KeyUtils;
import com.stoyanr.feeder.util.MenuUtils;
import com.stoyanr.feeder.view.ChannelHeader;

public class ItemsActivity extends FragmentActivity {

    private static final String TAG = "ItemsActivity";

    public static final String EXTRA_CHANNEL_IDS = "channelIds";
    public static final String EXTRA_KEYWORDS = "keywords";
    public static final String EXTRA_FEATURED_ONLY = "featuredOnly";

    public static final String[] PROJECTION = new String[] { Item._ID,
        Item.CHANNEL_ID, Item.TITLE, Item.READ, Item.DATE, Item.IMAGE };

    private static final int ITEMS_LOADER_ID = 0x11;
    private static final int CHANNEL_LOADER_ID = 0x12;

    private ContentManager cm;
    private long[] channelIds;
    private ArrayList<String> keywords;
    private boolean featuredOnly;
    private long channelId = -1;
    private long prevChannelId = -1;
    private long nextChannelId = -1;
    private ItemsAdapter adapter;
    private GridView gridView;
    private ChannelHeader header;

    private final LoaderManager.LoaderCallbacks<Cursor> clc = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            assert (id == ITEMS_LOADER_ID);
            String where = (featuredOnly && keywords != null) ? getWhere(keywords)
                : null;
            String[] wha = (featuredOnly && keywords != null) ? getWhereArgs(keywords)
                : null;
            return new CursorLoader(ItemsActivity.this, getIntent().getData(),
                PROJECTION, where, wha, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            adapter.changeCursor(cursor);
            ActivityCompat.invalidateOptionsMenu(ItemsActivity.this);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapter.changeCursor(null);
            ActivityCompat.invalidateOptionsMenu(ItemsActivity.this);
        }
    };

    private final LoaderManager.LoaderCallbacks<Channel> chlc = new LoaderManager.LoaderCallbacks<Channel>() {

        @Override
        public Loader<Channel> onCreateLoader(int id, Bundle args) {
            assert (id == CHANNEL_LOADER_ID);
            return new ChannelLoader(ItemsActivity.this, cm, channelId);
        }

        @Override
        public void onLoadFinished(Loader<Channel> loader, Channel channel) {
            setData(channel);
        }

        @Override
        public void onLoaderReset(Loader<Channel> loader) {
        }
    };

    private static class ChannelLoader extends AsyncTaskLoader<Channel> {

        private final ContentManager cm;
        private final long channelId;

        public ChannelLoader(Context context, ContentManager cm, long channelId) {
            super(context);
            this.cm = cm;
            this.channelId = channelId;
        }

        @Override
        public Channel loadInBackground() {
            return cm.queryChannelById(channelId);
        }
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.items_activity);
        cm = ContentManager.getInstance(getContentResolver());
        channelIds = getIntent().getLongArrayExtra(EXTRA_CHANNEL_IDS);
        keywords = getIntent().getStringArrayListExtra(EXTRA_KEYWORDS);
        featuredOnly = getIntent().getBooleanExtra(EXTRA_FEATURED_ONLY, false);
        channelId = getChannelId();
        adapter = new ItemsAdapter(this, null);
        initLoaders();
        initSiblings();
        initControls();
    }

    @Override
    protected void onStart() {
        super.onStart();
        forceLoad();
        ActivityCompat.invalidateOptionsMenu(this);
    }

    private long getChannelId() {
        return Long.parseLong(getIntent().getData().getPathSegments().get(1));
    }

    private void initSiblings() {
        if (channelIds != null) {
            int i;
            for (i = 0; i < channelIds.length; i++) {
                if (channelIds[i] == channelId)
                    break;
                prevChannelId = channelIds[i];
            }
            if (i < channelIds.length - 1) {
                nextChannelId = channelIds[i + 1];
            }
        }
    }

    private void initControls() {
        header = (ChannelHeader) findViewById(R.id.channelHeader);
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                int position, long id) {
                onClick(id);
            }
        });
    }

    private void onClick(long id) {
        viewItem(id);
    }

    private void initLoaders() {
        getSupportLoaderManager().initLoader(ITEMS_LOADER_ID, null, clc);
        getSupportLoaderManager().initLoader(CHANNEL_LOADER_ID, null, chlc);
    }

    private void forceLoad() {
        getSupportLoaderManager().getLoader(ITEMS_LOADER_ID).forceLoad();
        getSupportLoaderManager().getLoader(CHANNEL_LOADER_ID).forceLoad();
    }

    private void setData(Channel channel) {
        header.setData(channel.getId(), channel.getTitle(),
            channel.getDescription(), channel.getIcon());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.items_menu, menu);
        MenuUtils.setShowAsActionAll(menu);
        MenuItem item = menu.findItem(R.id.view_featured);
        item.setChecked(featuredOnly);
        int iconRes = featuredOnly ? R.drawable.btn_check_on
            : R.drawable.btn_check_off;
        item.setIcon(iconRes);
        if (prevChannelId < 0) {
            menu.removeItem(R.id.prev_channel);
        }
        if (nextChannelId < 0) {
            menu.removeItem(R.id.next_channel);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.view_featured:
            featuredOnly = !featuredOnly;
            viewChannelItems(channelId);
            return true;
        case R.id.prev_channel:
            return prevChannel();
        case R.id.next_channel:
            return nextChannel();
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (KeyUtils.interpretDirection(keyCode)) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
            return prevChannel();
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            return nextChannel();
        default:
            return super.onKeyUp(keyCode, event);
        }
    }

    private void viewItem(long itemId) {
        Log.d(TAG, "Viewing item " + itemId);
        Uri uri = ContentManager.getItemUri(itemId);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(ViewItemActivity.EXTRA_POST_IDS, getItemIds());
        startActivity(intent);
    }

    private boolean prevChannel() {
        if (prevChannelId < 0)
            return false;
        viewChannelItems(prevChannelId);
        return true;
    }

    private boolean nextChannel() {
        if (nextChannelId < 0)
            return false;
        viewChannelItems(nextChannelId);
        return true;
    }

    private void viewChannelItems(long channelId) {
        Log.d(TAG, "Viewing items for channel " + channelId);
        Uri uri = ContentManager.getChannelItemsUri(channelId);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(EXTRA_CHANNEL_IDS, channelIds);
        intent.putExtra(EXTRA_KEYWORDS, keywords);
        intent.putExtra(EXTRA_FEATURED_ONLY, featuredOnly);
        startActivity(intent);
        finish();
    }

    private long[] getItemIds() {
        Cursor cursor = adapter.getCursor();
        assert (cursor != null);
        long[] result = new long[cursor.getCount()];
        int i = 0;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            result[i++] = ContentManager.getItemId(cursor);
        }
        return result;
    }

    private static String getWhere(ArrayList<String> keywords) {
        String where = "";
        for (int i = 0; i < keywords.size(); i++) {
            where += "(" + Item.TITLE + " like ?) or (" + Item.DESCRIPTION
                + " like ?)";
            if (i < keywords.size() - 1)
                where += " or ";
        }
        return where;
    }

    private static String[] getWhereArgs(ArrayList<String> keywords) {
        List<String> result = new ArrayList<String>();
        for (String keyword : keywords) {
            result.add("%" + keyword + "%");
            result.add("%" + keyword + "%");
        }
        return result.toArray(new String[] {});
    }
}
