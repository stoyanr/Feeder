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

package com.stoyanr.feeder.test;

import static com.stoyanr.feeder.content.ContentManager.getChannel;
import static com.stoyanr.feeder.content.ContentManager.getChannelItemsUri;
import static com.stoyanr.feeder.content.ContentManager.getChannelUri;
import static com.stoyanr.feeder.content.ContentManager.getChannelsUri;
import static com.stoyanr.feeder.content.ContentManager.getItem;
import static com.stoyanr.feeder.content.ContentManager.getItemUri;
import static com.stoyanr.feeder.content.ContentManager.getItemsUri;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.test.ProviderTestCase2;

import com.stoyanr.feeder.content.ContentManager;
import com.stoyanr.feeder.content.MainContentProvider;
import com.stoyanr.feeder.model.Channel;
import com.stoyanr.feeder.model.Data;
import com.stoyanr.feeder.model.Item;

public class MainContentProviderTest extends
    ProviderTestCase2<MainContentProvider> {

    // @formatter:off
    private static final Channel CH_XKCD = new Channel(-1,
        "http://xkcd.com/rss.xml", "xkcd.com", new byte[] { 1, 2, 3 },
        "http://xkcd.com", "xkcd ...", "en", new byte[] { 4, 5, 6 });

    private static final Channel CH_NOSOFTSKILLS = new Channel(-1,
        "http://nosoftskills.com/feed", "nosoftskills.com", new byte[] { 7, 8, 9 },
        "http://nosoftskills.com", "No soft skills at all", "en", new byte[] { 10, 11, 12 });
    
    private static final Item IT_XKCD_1 = new Item(-1, CH_XKCD, false, 
        "Strange", "", "http://xkcd.com/1", "...", "", new Date(), new byte[] { 4, 5, 6 });

    private static final Channel[] EMPTY_CHANNELS = new Channel[] {};
    // @formatter:on

    public MainContentProviderTest() {
        this(MainContentProvider.class, MainContentProvider.AUTHORITY);
    }

    public MainContentProviderTest(Class<MainContentProvider> providerClass,
        String providerAuthority) {
        super(providerClass, providerAuthority);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetTypeChannels() {
        testGetType(getChannelsUri(), getDirType(Channel.ENTITY));
    }

    public void testGetTypeChannel() {
        testGetType(getChannelUri(1), getItemType(Channel.ENTITY));
    }

    public void testGetTypeChannelItems() {
        testGetType(getChannelItemsUri(1), getDirType(Item.ENTITY));
    }

    public void testGetTypeItems() {
        testGetType(getItemsUri(), getDirType(Item.ENTITY));
    }

    public void testGetTypeItem() {
        testGetType(getItemUri(1), getItemType(Item.ENTITY));
    }

    public void testGetTypeEmpty() {
        testGetTypeException(Uri.parse(""));
    }

    public void testGetTypeUnknown() {
        testGetTypeException(getUrl("x"));
    }

    public void testQueryNoChannels() {
        testQueryChannels(getChannelsUri(), EMPTY_CHANNELS);
    }

    public void testQueryChannel() {
        long id = insertChannel(getChannelsUri(), CH_XKCD);
        testQueryChannels(getChannelUri(id), new Channel[] { CH_XKCD });
    }

    public void testQueryChannels() {
        insertChannel(getChannelsUri(), CH_XKCD);
        insertChannel(getChannelsUri(), CH_NOSOFTSKILLS);
        testQueryChannels(getChannelsUri(), new Channel[] { CH_XKCD,
            CH_NOSOFTSKILLS });
    }

    public void testQueryItem() {
        long channelId = insertChannel(getChannelsUri(), CH_XKCD);
        setChannelId(IT_XKCD_1, channelId);
        long id = insertItem(getItemsUri(), IT_XKCD_1);
        testQueryItems(getItemUri(id), new Item[] { IT_XKCD_1 });
    }

    public void testQueryItems() {
        long channelId = insertChannel(getChannelsUri(), CH_XKCD);
        setChannelId(IT_XKCD_1, channelId);
        insertItem(getItemsUri(), IT_XKCD_1);
        testQueryItems(getItemsUri(), new Item[] { IT_XKCD_1 });
    }

    public void testQueryChannelItems() {
        long channelId = insertChannel(getChannelsUri(), CH_XKCD);
        setChannelId(IT_XKCD_1, channelId);
        insertItem(getItemsUri(), IT_XKCD_1);
        testQueryItems(getChannelItemsUri(channelId), new Item[] { IT_XKCD_1 });
    }

    public void testQueryChannelsInvalidUri() {
        testQueryChannelsException(getUrl("x"));
    }

    public void testInsertNullChannel() {
        testInsertChannelException(getChannelsUri(), null);
    }

    public void testInsertEmptyChannel() {
        testInsertChannel(getChannelsUri(), new Channel());
    }

    public void testInsertOneChannel() {
        testInsertChannel(getChannelsUri(), CH_XKCD);
    }

    public void testInsertOneChannelTwoTimes() {
        testInsertChannel(getChannelsUri(), CH_XKCD);
        testInsertChannelException(getChannelsUri(), CH_XKCD);
    }

    public void testInsertTwoChannels() {
        testInsertChannel(getChannelsUri(), CH_XKCD);
        testInsertChannel(getChannelsUri(), CH_NOSOFTSKILLS);
    }

    public void testInsertChannelInvalidUri() {
        testInsertChannelException(getChannelUri(1), null);
    }

    public void testUpdateNullChannel() {
        long id = insertChannel(getChannelsUri(), CH_XKCD);
        testUpdateChannelException(getChannelUri(id), null);
    }

    public void testUpdateNonExistingChannel() {
        long id = insertChannel(getChannelsUri(), CH_XKCD);
        int result = updateChannels(getChannelUri(id + 1), new Channel(),
            new String[] { Channel.TITLE }, null, null);
        assertEquals(result, 0);
    }

    public void testUpdateOneChannel() {
        long id = insertChannel(getChannelsUri(), CH_XKCD);
        CH_XKCD.setTitle(CH_XKCD.getTitle() + "_");
        testUpdateChannelTitle(getChannelUri(id), CH_XKCD);
        CH_XKCD.setTitle("xkcd.com");
    }

    public void testUpdateOneChannelDuplicateTitle() {
        long id = insertChannel(getChannelsUri(), CH_XKCD);
        insertChannel(getChannelsUri(), CH_NOSOFTSKILLS);
        CH_XKCD.setTitle(CH_NOSOFTSKILLS.getTitle());
        testUpdateChannelException(getChannelUri(id), CH_XKCD);
        CH_XKCD.setTitle("xkcd.com");
    }

    public void testUpdateTwoChannels() {
        insertChannel(getChannelsUri(), CH_XKCD);
        insertChannel(getChannelsUri(), CH_NOSOFTSKILLS);
        CH_XKCD.setLanguage(CH_XKCD.getLanguage() + "_");
        testUpdateChannelsLanguage(getChannelsUri(), CH_XKCD, null, null);
        CH_XKCD.setLanguage("en");
    }

    public void testUpdateOneChannelWithWhere() {
        insertChannel(getChannelsUri(), CH_XKCD);
        insertChannel(getChannelsUri(), CH_NOSOFTSKILLS);
        CH_XKCD.setLanguage(CH_XKCD.getLanguage() + "_");
        testUpdateChannelsLanguage(getChannelsUri(), CH_XKCD, Channel.TITLE
            + "=?", new String[] { CH_XKCD.getTitle() });
        CH_XKCD.setLanguage("en");
    }

    public void testUpdateChannelInvalidUri() {
        testUpdateChannelException(getUrl("x"), null);
    }

    public void testDeleteNonExistingChannel() {
        long id = insertChannel(getChannelsUri(), CH_XKCD);
        int result = deleteChannels(getChannelUri(id + 1), null, null);
        assertEquals(result, 0);
    }

    public void testDeleteOneChannel() {
        long id = insertChannel(getChannelsUri(), CH_XKCD);
        testDeleteChannel(getChannelUri(id));
    }

    public void testDeleteTwoChannels() {
        insertChannel(getChannelsUri(), CH_XKCD);
        insertChannel(getChannelsUri(), CH_NOSOFTSKILLS);
        testDeleteChannels(getChannelsUri(), null, null);
    }

    public void testDeleteOneChannelWithWhere() {
        insertChannel(getChannelsUri(), CH_XKCD);
        insertChannel(getChannelsUri(), CH_NOSOFTSKILLS);
        testDeleteChannels(getChannelsUri(), Channel.TITLE + "=?",
            new String[] { CH_XKCD.getTitle() });
    }

    public void testDeleteChannelInvalidUri() {
        testDeleteChannelException(getUrl("x"));
    }

    private void testGetType(Uri uri, String type) {
        assertEquals(getProvider().getType(uri), type);
        assertEquals(getMockContentResolver().getType(uri), type);
    }

    private void testGetTypeException(Uri uri) {
        try {
            getProvider().getType(uri);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    private void testQueryChannels(Uri uri, Channel[] channels) {
        List<Channel> channelsx = queryChannels(uri, null, null);
        assertEquals(channelsx.size(), channels.length);
        // TODO: Compare the contents of the arrays as well
    }

    private void testQueryChannelsException(Uri uri) {
        try {
            queryChannels(uri, null, null);
            fail();
        } catch (IllegalArgumentException e) {
        } catch (SQLException e) {
        }
    }

    private void testQueryItems(Uri uri, Item[] items) {
        List<Item> itemsx = queryItems(uri, null, null);
        assertEquals(items.length, itemsx.size());
        // TODO: Compare the contents of the arrays as well
    }

    private void testInsertChannel(Uri uri, Channel channel) {
        List<Channel> channels = queryChannels(uri, null, null);
        int size = channels.size();
        long result = insertChannel(uri, channel);
        assertTrue(result >= 0);
        channels = queryChannels(uri, null, null);
        assertEquals(channels.size(), size + 1);
    }

    private void testInsertChannelException(Uri uri, Channel channel) {
        try {
            insertChannel(uri, channel);
            fail();
        } catch (IllegalArgumentException e) {
        } catch (SQLException e) {
        }
    }

    private void testUpdateChannelTitle(Uri uri, Channel channel) {
        List<Channel> channels = queryChannels(uri, null, null);
        assertEquals(channels.size(), 1);
        assertFalse(channel.getTitle().equals(channels.get(0).getTitle()));
        int result = updateChannels(uri, channel,
            new String[] { Channel.TITLE }, null, null);
        assertEquals(result, 1);
        channels = queryChannels(uri, null, null);
        assertEquals(channels.size(), 1);
        assertEquals(channels.get(0).getTitle(), channel.getTitle());
    }

    private void testUpdateChannelsLanguage(Uri uri, Channel channel,
        String where, String[] whereArgs) {
        List<Channel> channels = queryChannels(uri, where, whereArgs);
        int size = channels.size();
        assertTrue(size > 0);
        assertFalse(channel.getLanguage().equals(channels.get(0).getLanguage()));
        int result = updateChannels(uri, channel,
            new String[] { Channel.LANGUAGE }, where, whereArgs);
        assertEquals(result, size);
        channels = queryChannels(uri, where, whereArgs);
        assertTrue(channels.size() > 0);
        assertEquals(channels.get(0).getLanguage(), channel.getLanguage());
    }

    private void testUpdateChannelException(Uri uri, Channel channel) {
        try {
            updateChannels(uri, channel, new String[] { Channel.TITLE }, null,
                null);
            fail();
        } catch (IllegalArgumentException e) {
        } catch (SQLException e) {
        }
    }

    private void testDeleteChannel(Uri uri) {
        List<Channel> channels = queryChannels(uri, null, null);
        assertEquals(channels.size(), 1);
        int result = deleteChannels(uri, null, null);
        assertEquals(result, 1);
        channels = queryChannels(uri, null, null);
        assertEquals(channels.size(), 0);
    }

    private void testDeleteChannels(Uri uri, String where, String[] whereArgs) {
        List<Channel> channels = queryChannels(uri, where, whereArgs);
        int size = channels.size();
        assertTrue(size > 0);
        int result = deleteChannels(uri, where, whereArgs);
        assertEquals(result, size);
        channels = queryChannels(uri, where, whereArgs);
        assertEquals(channels.size(), 0);
    }

    private void testDeleteChannelException(Uri uri) {
        try {
            deleteChannels(uri, null, null);
            fail();
        } catch (IllegalArgumentException e) {
        } catch (SQLException e) {
        }
    }

    private static final String[] CHANNELS_PROJECTION = new String[] {
        Channel._ID, Channel.URL, Channel.TITLE, Channel.ICON, Channel.LINK,
        Channel.DESCRIPTION, Channel.LANGUAGE, Channel.IMAGE };

    private static final String[] ITEMS_PROJECTION = new String[] { Item._ID,
        Item.CHANNEL_ID, Item.TITLE, Item.AUTHOR, Item.LINK, Item.DESCRIPTION,
        Item.CONTENTS, Item.DATE, Item.IMAGE };

    private List<Channel> queryChannels(Uri uri, String where,
        String[] whereArgs) {
        Cursor c = getMockContentResolver().query(uri, CHANNELS_PROJECTION,
            where, whereArgs, null);
        assertTrue(c != null);
        List<Channel> channels = new ArrayList<Channel>();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Channel channel = getChannel(c);
            channels.add(channel);
        }
        return channels;
    }

    private List<Item> queryItems(Uri uri, String where, String[] whereArgs) {
        Cursor c = getMockContentResolver().query(uri, ITEMS_PROJECTION, where,
            whereArgs, null);
        assertTrue(c != null);
        List<Item> items = new ArrayList<Item>();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Item item = getItem(c);
            items.add(item);
        }
        return items;
    }

    private long insertChannel(Uri uri, Channel channel) {
        ContentValues cv = (channel != null) ? ContentManager.getContentValues(
            channel, CHANNELS_PROJECTION) : null;
        Uri result = getMockContentResolver().insert(uri, cv);
        return Long.parseLong(result.getPathSegments().get(1));
    }

    private long insertItem(Uri uri, Item item) {
        ContentValues cv = (item != null) ? ContentManager.getContentValues(
            item, ITEMS_PROJECTION) : null;
        Uri result = getMockContentResolver().insert(uri, cv);
        return Long.parseLong(result.getPathSegments().get(1));
    }

    private int updateChannels(Uri uri, Channel channel, String[] projection,
        String where, String[] whereArgs) {
        ContentValues cv = (channel != null) ? ContentManager.getContentValues(
            channel, projection) : null;
        return getMockContentResolver().update(uri, cv, where, whereArgs);
    }

    private int deleteChannels(Uri uri, String where, String[] whereArgs) {
        return getMockContentResolver().delete(uri, where, whereArgs);
    }

    private static void setChannelId(Item item, long channelId) {
        Channel channel = item.getChannel();
        item.setChannel(new Channel(channelId, channel.getUrl(), channel
            .getTitle(), channel.getIcon(), channel.getLink(), channel
            .getDescription(), channel.getLanguage(), channel.getImage()));
    }

    private static Uri getUrl(String suffix) {
        return Uri.parse(ContentResolver.SCHEME_CONTENT + "://"
            + MainContentProvider.AUTHORITY + "/" + suffix);
    }

    private static String getDirType(String suffix) {
        return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + Data.MIME_TYPE_PFX
            + suffix;
    }

    private static String getItemType(String suffix) {
        return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + Data.MIME_TYPE_PFX
            + suffix;
    }
}
