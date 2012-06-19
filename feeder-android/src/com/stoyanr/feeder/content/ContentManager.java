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

package com.stoyanr.feeder.content;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import com.stoyanr.feeder.model.Channel;
import com.stoyanr.feeder.model.Item;
import com.stoyanr.feeder.util.DateUtils;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.Where;

public final class ContentManager {

    public static final Uri CHANNELS_URI = getUri(Channel.ENTITY_PL);
    public static final Uri POSTS_URI = getUri(Item.ENTITY_PL);

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss.SSSSSS");

    // @formatter:off
    private static Map<ContentResolver, ContentManager> instances = 
        new HashMap<ContentResolver, ContentManager>();
    // @formatter:on

    public static long getChannelId(Cursor c) {
        return c.getInt(c.getColumnIndex(Channel._ID));
    }

    public static String getChannelTitle(Cursor c) {
        return c.getString(c.getColumnIndex(Channel.TITLE));
    }

    public static String getChannelUrl(Cursor c) {
        return c.getString(c.getColumnIndex(Channel.URL));
    }

    public static byte[] getChannelIcon(Cursor c) {
        return c.getBlob(c.getColumnIndex(Channel.ICON));
    }

    public static String getChannelLink(Cursor c) {
        return c.getString(c.getColumnIndex(Channel.LINK));
    }

    public static String getChannelDescription(Cursor c) {
        return c.getString(c.getColumnIndex(Channel.DESCRIPTION));
    }

    public static String getChannelLanguage(Cursor c) {
        return c.getString(c.getColumnIndex(Channel.LANGUAGE));
    }

    public static byte[] getChannelImage(Cursor c) {
        return c.getBlob(c.getColumnIndex(Channel.IMAGE));
    }

    public static long getItemId(Cursor c) {
        return c.getInt(c.getColumnIndex(Item._ID));
    }

    public static int getItemChannelId(Cursor c) {
        return c.getInt(c.getColumnIndex(Item.CHANNEL_ID));
    }

    public static boolean isItemRead(Cursor c) {
        return c.getInt(c.getColumnIndex(Item.READ)) != 0;
    }

    public static String getItemTitle(Cursor c) {
        return c.getString(c.getColumnIndex(Item.TITLE));
    }

    public static String getItemAuthor(Cursor c) {
        return c.getString(c.getColumnIndex(Item.AUTHOR));
    }

    public static String getItemLink(Cursor c) {
        return c.getString(c.getColumnIndex(Item.LINK));
    }

    public static String getItemDescription(Cursor c) {
        return c.getString(c.getColumnIndex(Item.DESCRIPTION));
    }

    public static String getItemContents(Cursor c) {
        return c.getString(c.getColumnIndex(Item.CONTENTS));
    }

    public static byte[] getItemImage(Cursor c) {
        return c.getBlob(c.getColumnIndex(Item.IMAGE));
    }

    public static Date getItemDate(Cursor c) {
        Date date = null;
        try {
            date = DATE_FORMAT.parse(c.getString(c.getColumnIndex(Item.DATE)));
        } catch (ParseException e) {
        }
        return date;
    }

    public static Channel getChannel(Cursor c) {
        return new Channel(getChannelId(c), getChannelUrl(c),
            getChannelTitle(c), getChannelIcon(c), getChannelLink(c),
            getChannelDescription(c), getChannelLanguage(c), getChannelImage(c));
    }

    public static ContentValues getContentValues(Channel channel,
        String[] projection) {
        ContentValues values = new ContentValues();
        Set<String> columns = new HashSet<String>(Arrays.asList(projection));
        if (columns.contains(Channel.URL))
            values.put(Channel.URL, channel.getUrl());
        if (columns.contains(Channel.TITLE))
            values.put(Channel.TITLE, channel.getTitle());
        if (columns.contains(Channel.ICON))
            values.put(Channel.ICON, channel.getIcon());
        if (columns.contains(Channel.LINK))
            values.put(Channel.LINK, channel.getLink());
        if (columns.contains(Channel.DESCRIPTION))
            values.put(Channel.DESCRIPTION, channel.getDescription());
        if (columns.contains(Channel.LANGUAGE))
            values.put(Channel.LANGUAGE, channel.getLanguage());
        if (columns.contains(Channel.IMAGE))
            values.put(Channel.IMAGE, channel.getImage());
        return values;
    }

    public static Item getItem(Cursor c) {
        Channel channel = new Channel(getItemChannelId(c), "", "", null, "",
            null, null, null);
        return new Item(getItemId(c), channel, false, getItemTitle(c),
            getItemAuthor(c), getItemLink(c), getItemDescription(c),
            getItemContents(c), getItemDate(c), getItemImage(c));
    }

    public static ContentValues getContentValues(Item item, String[] projection) {
        ContentValues values = new ContentValues();
        Set<String> columns = new HashSet<String>(Arrays.asList(projection));
        if (columns.contains(Item.CHANNEL_ID))
            values.put(Item.CHANNEL_ID, item.getChannelId());
        if (columns.contains(Item.READ))
            values.put(Item.READ, item.isRead());
        if (columns.contains(Item.TITLE))
            values.put(Item.TITLE, item.getTitle());
        if (columns.contains(Item.AUTHOR))
            values.put(Item.AUTHOR, item.getAuthor());
        if (columns.contains(Item.LINK))
            values.put(Item.LINK, item.getLink());
        if (columns.contains(Item.DESCRIPTION))
            values.put(Item.DESCRIPTION, item.getDescription());
        if (columns.contains(Item.CONTENTS))
            values.put(Item.CONTENTS, item.getContents());
        if (columns.contains(Item.DATE))
            values.put(Item.DATE, DATE_FORMAT.format(item.getDate()));
        if (columns.contains(Item.IMAGE))
            values.put(Item.IMAGE, item.getImage());
        return values;
    }

    public static Uri getUri(String uriPath) {
        return Uri.parse(ContentResolver.SCHEME_CONTENT + "://"
            + MainContentProvider.AUTHORITY + "/" + uriPath);
    }

    public static Uri getChannelsUri() {
        return CHANNELS_URI;
    }

    public static Uri getChannelUri(long id) {
        return ContentUris.withAppendedId(CHANNELS_URI, id);
    }

    public static Uri getItemsUri() {
        return POSTS_URI;
    }

    public static Uri getItemUri(long id) {
        return ContentUris.withAppendedId(POSTS_URI, id);
    }

    public static Uri getChannelItemsUri(long id) {
        return getChannelUri(id).buildUpon().appendPath(Item.ENTITY_PL).build();
    }

    public static Uri getChannelIconUri(long id) {
        return getChannelUri(id).buildUpon().appendPath(Channel.ICON).build();
    }

    public static ContentManager getInstance(ContentResolver cr) {
        ContentManager instance = null;
        if (instances.containsKey(cr)) {
            instance = instances.get(cr);
        } else {
            instance = new ContentManager(cr);
            instances.put(cr, instance);
        }
        return instance;
    }

    private final DatabaseHelper helper;

    private ContentManager(ContentResolver cr) {
        AbstractContentProvider provider = (AbstractContentProvider) cr
            .acquireContentProviderClient(MainContentProvider.AUTHORITY)
            .getLocalContentProvider();
        assert (provider != null);
        this.helper = provider.getHelper();
    }

    public Channel queryChannelById(long id) {
        return helper.queryById(id, Channel.class);
    }

    public long createChannel(Channel channel) {
        return helper.create(channel, Channel.class);
    }

    public void updateChannel(Channel channel) {
        helper.update(channel, Channel.class);
    }

    public void deleteChannelById(long id) {
        helper.deleteById(id, Channel.class);
    }

    public Item queryItemById(long id) {
        return helper.queryById(id, Item.class);
    }

    public long createItem(Item item) {
        return helper.create(item, Item.class);
    }

    public void updateItem(Item item) {
        helper.update(item, Item.class);
    }

    public void deleteItemById(long id) {
        helper.deleteById(id, Item.class);
    }

    public void deleteChannelItems(long channelId) {
        try {
            DeleteBuilder<Item, Long> db = helper.getDaoEx(Item.class)
                .deleteBuilder();
            db.setWhere(db.where().eq(Item.CHANNEL_ID, channelId));
            db.delete();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public void deleteOldItems(int field, int age, boolean onlyRead) {
        try {
            DeleteBuilder<Item, Long> db = helper.getDaoEx(Item.class)
                .deleteBuilder();
            Date datex = DateUtils.add(new Date(), field, -age);
            Where<Item, Long> where = db.where().lt(Item.DATE, datex);
            if (onlyRead) {
                where = where.and().eq(Item.READ, true);
            }
            db.setWhere(where);
            db.delete();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public Item findItem(long channelId, String link) {
        Item result = null;
        try {
            List<Item> items = helper.getDaoEx(Item.class).queryBuilder()
                .where().eq(Item.CHANNEL_ID, channelId).and()
                .eq(Item.LINK, link).query();
            if (items.size() > 0) {
                assert (items.size() == 1);
                result = items.get(0);
            }
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        return result;
    }

    public Item getLatestItem(long channelId) {
        Item result = null;
        try {
            List<Item> items = helper.getDaoEx(Item.class).queryBuilder()
                .orderBy(Item.DATE, false).where()
                .eq(Item.CHANNEL_ID, channelId).query();
            if (items.size() > 0) {
                result = items.get(0);
            }
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        return result;
    }

    public int getUnreadItemsCount(long channelId) {
        int result = -1;
        try {
            List<Item> items = helper.getDaoEx(Item.class).queryBuilder()
                .where().eq(Item.CHANNEL_ID, channelId).and()
                .eq(Item.READ, false).query();
            result = items.size();
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        return result;
    }
}
