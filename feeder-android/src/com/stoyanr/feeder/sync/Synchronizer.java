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

package com.stoyanr.feeder.sync;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;

import com.stoyanr.feeder.R;
import com.stoyanr.feeder.content.ContentManager;
import com.stoyanr.feeder.model.Channel;
import com.stoyanr.feeder.model.Item;
import com.stoyanr.feeder.util.IOUtils;
import com.stoyanr.feeder.util.ResourceUtils;
import com.stoyanr.feeder.util.UrlUtils;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndContent;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndImage;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.XmlReader;

public class Synchronizer {

    private final Context ctx;
    private final ContentManager cm;

    public Synchronizer(Context ctx) {
        super();
        this.ctx = ctx;
        this.cm = ContentManager.getInstance(ctx.getContentResolver());
    }

    public long sync(long channelId, String url) throws Exception {
        SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));
        Channel channel = syncChannel(channelId, url, feed,
            getFeedIcon(url, feed));
        if (feed.getEntries() != null) {
            for (Object entry : feed.getEntries()) {
                syncItem(channel, (SyndEntry) entry);
            }
        }
        updateChannelImage(channel);
        return channel.getId();
    }

    private Channel syncChannel(long channelId, String url, SyndFeed feed,
        byte[] icon) {
        Channel channel = null;
        if (channelId == -1) {
            channel = createChannel(url, feed, icon);
        } else {
            channel = cm.queryChannelById(channelId);
            updateChannel(channel, feed, icon);
        }
        return channel;
    }

    private Item syncItem(Channel channel, SyndEntry entry) {
        Item item = cm.findItem(channel.getId(), entry.getLink());
        if (item == null) {
            item = createItem(channel, entry);
        } else {
            updateItem(item, entry);
        }
        return item;
    }

    private Channel createChannel(String url, SyndFeed feed, byte[] icon) {
        Channel channel = new Channel(-1, url, feed.getTitle(), icon,
            feed.getLink(), feed.getDescription(), feed.getLanguage(),
            new byte[] {});
        cm.createChannel(channel);
        return channel;
    }

    private Item createItem(Channel channel, SyndEntry entry) {
        String entryDescription = getEntryDescription(entry);
        byte[] itemImage = getItemImage(entryDescription);
        Item item = new Item(-1, channel, false, entry.getTitle(),
            entry.getAuthor(), entry.getLink(), entryDescription,
            getEntryContents(entry), entry.getPublishedDate(), itemImage);
        cm.createItem(item);
        return item;
    }

    private void updateChannel(Channel channel, SyndFeed feed, byte[] icon) {
        channel.setTitle(feed.getTitle());
        channel.setIcon(icon);
        channel.setLink(feed.getLink());
        channel.setDescription(feed.getDescription());
        channel.setLanguage(feed.getLanguage());
        cm.updateChannel(channel);
    }

    private void updateItem(Item item, SyndEntry entry) {
        String entryDescription = getEntryDescription(entry);
        byte[] itemImage = getItemImage(entryDescription);
        item.setTitle(entry.getTitle());
        item.setAuthor(entry.getAuthor());
        item.setDescription(entryDescription);
        item.setContents(getEntryContents(entry));
        item.setDate(entry.getPublishedDate());
        item.setImage(itemImage);
        cm.updateItem(item);
    }

    private void updateChannelImage(Channel channel) {
        Item latestItem = cm.getLatestItem(channel.getId());
        if (latestItem != null) {
            channel.setImage(latestItem.getImage());
            cm.updateChannel(channel);
        }
    }

    private byte[] getFeedIcon(String url, SyndFeed feed)
        throws MalformedURLException {
        URL iconUrl = getFeedIconUrl(url, feed);
        byte[] icon = IOUtils.getByteArray(iconUrl);
        if (icon == null || icon.length == 0) {
            icon = ResourceUtils.getBytes(ctx, R.drawable.feedicon);
        }
        return icon;
    }

    private URL getFeedIconUrl(String url, SyndFeed feed)
        throws MalformedURLException {
        URL iconUrl = null;
        SyndImage image = feed.getImage();
        if (image != null) {
            iconUrl = new URL(image.getUrl());
        } else {
            iconUrl = UrlUtils.getFaviconUrl(new URL(url));
        }
        return iconUrl;
    }

    private byte[] getItemImage(String description) {
        byte[] result = new byte[] {};
        String imageUrl = getFirstImageUrl("<html>" + description + "</html>");
        if (imageUrl != null) {
            try {
                result = IOUtils.getByteArray(new URL(imageUrl));
            } catch (MalformedURLException e) {
            }
        }
        return result;
    }

    private String getEntryDescription(SyndEntry entry) {
        StringBuffer description = new StringBuffer();
        if (entry.getDescription() != null) {
            SyndContent c = entry.getDescription();
            if (c.getValue() != null) {
                description.append(c.getValue());
            }
        }
        return description.toString();
    }

    private String getEntryContents(SyndEntry entry) {
        StringBuffer contents = new StringBuffer();
        if (entry.getContents() != null && entry.getContents().size() > 0) {
            for (Object o : entry.getContents()) {
                SyndContent c = (SyndContent) o;
                if (c.getValue() != null) {
                    contents.append(c.getValue());
                }
            }
        }
        return contents.toString();
    }

    private String getFirstImageUrl(String html) {
        String result = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(html));
            result = getFirstImageUrl(xpp);
        } catch (Exception e) {
        }
        return result;
    }

    private String getFirstImageUrl(XmlPullParser xpp)
        throws XmlPullParserException, IOException {
        String result = null;
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG
                && "img".equals(xpp.getName())) {
                result = xpp.getAttributeValue(null, "src");
                break;
            }
            eventType = xpp.next();
        }
        return result;
    }

}
