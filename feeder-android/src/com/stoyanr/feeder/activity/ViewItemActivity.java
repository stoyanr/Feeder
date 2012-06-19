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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import com.stoyanr.feeder.R;
import com.stoyanr.feeder.content.ContentManager;
import com.stoyanr.feeder.model.Channel;
import com.stoyanr.feeder.model.Item;
import com.stoyanr.feeder.util.KeyUtils;
import com.stoyanr.feeder.util.MenuUtils;
import com.stoyanr.feeder.view.ChannelHeader;

public class ViewItemActivity extends FragmentActivity {

    private static final String TAG = "ViewItemActivity";

    public static final String EXTRA_POST_IDS = "itemIds";

    public static final String[] PROJECTION = new String[] { Item._ID,
        Item.CHANNEL_ID, Item.TITLE, Item.AUTHOR, Item.DESCRIPTION,
        Item.CONTENTS, Item.READ, Item.LINK, Item.DATE };

    private static final SimpleDateFormat DATE_FMT_TODAY = new SimpleDateFormat(
        "h:mma");
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat(
        "MM/dd/yyyy h:mma");

    // @formatter:off
    private static final String HTML_HEADER = 
"<html>" + 
    "<head>" + 
        "<style type=\"text/css\">body { background-color: black; color: LightGray; } a { color: #ddf; }"
        + "</style>" + 
    "</head>" + 
    "<body>";

    private static final String HTML_FOOTER = 
    "</body>" + 
"</html>";
    // @formatter:on

    private static final int ITEM_LOADER_ID = 0x03;

    private ContentManager cm;
    private long[] itemIds;
    private long itemId = -1;
    private long newerItemId = -1;
    private long olderItemId = -1;
    private ChannelHeader header;
    private TextView titleTextView;
    private TextView authorTextView;
    private TextView dateTextView;
    private WebView contentsWebView;

    private final LoaderManager.LoaderCallbacks<Item> ilc = new LoaderManager.LoaderCallbacks<Item>() {

        @Override
        public Loader<Item> onCreateLoader(int id, Bundle args) {
            assert (id == ITEM_LOADER_ID);
            return new ItemLoader(ViewItemActivity.this, cm, itemId);
        }

        @Override
        public void onLoadFinished(Loader<Item> loader, Item item) {
            setData(item);
        }

        @Override
        public void onLoaderReset(Loader<Item> loader) {
        }
    };

    private static class ItemLoader extends AsyncTaskLoader<Item> {

        private final ContentManager cm;
        private final long itemId;

        public ItemLoader(Context context, ContentManager cm, long itemId) {
            super(context);
            this.cm = cm;
            this.itemId = itemId;
        }

        @Override
        public Item loadInBackground() {
            Item item = cm.queryItemById(itemId);
            item.setRead(true);
            cm.updateItem(item);
            Channel channel = cm.queryChannelById(item.getChannelId());
            item.setChannel(channel);
            return item;
        }
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.view_item_activity);
        cm = ContentManager.getInstance(getContentResolver());
        itemIds = getIntent().getLongArrayExtra(EXTRA_POST_IDS);
        itemId = getItemId();
        initLoaders();
        initSiblings();
        initControls();
    }

    @Override
    protected void onStart() {
        super.onStart();
        forceLoad();
    }

    private long getItemId() {
        return Long.parseLong(getIntent().getData().getPathSegments().get(1));
    }

    private void initSiblings() {
        if (itemIds != null) {
            int i;
            for (i = 0; i < itemIds.length; i++) {
                if (itemIds[i] == itemId)
                    break;
                newerItemId = itemIds[i];
            }
            if (i < itemIds.length - 1) {
                olderItemId = itemIds[i + 1];
            }
        }
    }

    private void initControls() {
        header = (ChannelHeader) findViewById(R.id.channelHeader);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        authorTextView = (TextView) findViewById(R.id.authorTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        contentsWebView = (WebView) findViewById(R.id.contentsWebView);
    }

    private void initLoaders() {
        getSupportLoaderManager().initLoader(ITEM_LOADER_ID, null, ilc);
    }

    private void forceLoad() {
        getSupportLoaderManager().getLoader(ITEM_LOADER_ID).forceLoad();
    }

    private void setData(Item item) {
        Channel channel = item.getChannel();
        header.setData(channel.getId(), channel.getTitle(),
            channel.getDescription(), channel.getIcon());

        titleTextView.setText(item.getTitle());

        authorTextView.setText(item.getAuthor());

        Date date = item.getDate();
        dateTextView.setText(getDateFormat(date).format(date));

        contentsWebView.loadData(HTML_HEADER + getItemBody(item) + HTML_FOOTER,
            "text/html", "utf-8");
    }

    private static SimpleDateFormat getDateFormat(Date date) {
        Calendar then = new GregorianCalendar();
        then.setTime(date);
        Calendar now = new GregorianCalendar();
        SimpleDateFormat fmt;
        if (now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR))
            fmt = DATE_FMT_TODAY;
        else
            fmt = DATE_FMT;
        return fmt;
    }

    private String getItemBody(Item item) {
        String body = "";
        String contents = item.getContents();
        if (contents != null && contents.length() > 0) {
            body += contents;
        } else {
            String description = item.getDescription();
            if (description != null && description.length() > 0) {
                body += description;
                String link = item.getLink();
                if (hasMoreLink(description, link) == false) {
                    body += "<p><a href=\"" + link + "\">"
                        + getResources().getText(R.string.read_more)
                        + "</a></p>";
                }
            }
        }
        return body;
    }

    private static boolean hasMoreLink(String body, String url) {
        int pos = body.indexOf(url);
        if (pos <= 0)
            return false;
        try {
            if (body.charAt(pos - 1) != '>')
                return false;
            else if (body.charAt(pos + url.length() + 1) != '<')
                return false;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.view_item_menu, menu);
        MenuUtils.setShowAsActionAll(menu);
        if (newerItemId < 0) {
            menu.removeItem(R.id.newer_item);
        }
        if (olderItemId < 0) {
            menu.removeItem(R.id.older_item);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.newer_item:
            return newerItem();
        case R.id.older_item:
            return olderItem();
        default:
            return false;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (KeyUtils.interpretDirection(keyCode)) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
            return newerItem();
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            return olderItem();
        default:
            return super.onKeyUp(keyCode, event);
        }
    }

    private boolean newerItem() {
        if (newerItemId < 0)
            return false;
        viewItem(newerItemId);
        return true;
    }

    private boolean olderItem() {
        if (olderItemId < 0)
            return false;
        viewItem(olderItemId);
        return true;
    }

    private void viewItem(long itemId) {
        Log.d(TAG, "Viewing item " + itemId);
        Uri uri = ContentManager.getItemUri(itemId);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(EXTRA_POST_IDS, itemIds);
        startActivity(intent);
        finish();
    }
}
