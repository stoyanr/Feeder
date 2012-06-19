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

package com.stoyanr.feeder.model;

import java.util.Date;

import com.stoyanr.feeder.annotations.DefaultSortOrder;
import com.stoyanr.feeder.annotations.MimeType;
import com.stoyanr.feeder.annotations.UriPaths;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Data.TABLE_NAME_PFX + Item.ENTITY)
@UriPaths({ Item.ENTITY_PL, Item.ENTITY_PL + "/#",
    Channel.ENTITY_PL + "/#/" + Item.ENTITY_PL })
@MimeType(Data.MIME_TYPE_PFX + Item.ENTITY)
@DefaultSortOrder(Item.DATE + " DESC")
public class Item extends Data {
    public static final String ENTITY = "item";
    public static final String ENTITY_PL = ENTITY + "s";

    public static final String CHANNEL_ID = "channel_id";
    public static final String READ = "read";
    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String LINK = "link";
    public static final String DESCRIPTION = "description";
    public static final String CONTENTS = "contents";
    public static final String DATE = "date";
    public static final String IMAGE = "image";

    public static final String IDX_NAME = CHANNEL_ID + "_" + LINK + "_idx";

    @DatabaseField(columnName = CHANNEL_ID, canBeNull = false, foreign = true, index = true, uniqueIndex = true, uniqueIndexName = IDX_NAME)
    private Channel channel;

    @DatabaseField(columnName = READ, defaultValue = "false")
    private boolean read;

    @DatabaseField(columnName = TITLE, canBeNull = false)
    private String title;

    @DatabaseField(columnName = AUTHOR)
    private String author;

    @DatabaseField(columnName = LINK, canBeNull = false, uniqueIndex = true, uniqueIndexName = IDX_NAME)
    private final String link;

    @DatabaseField(columnName = DESCRIPTION)
    private String description;

    @DatabaseField(columnName = CONTENTS)
    private String contents;

    @DatabaseField(columnName = DATE)
    private Date date;

    @DatabaseField(columnName = IMAGE, dataType = DataType.BYTE_ARRAY)
    private byte[] image;

    Item() {
        super(-1);
        this.channel = new Channel();
        this.read = false;
        this.title = "";
        this.author = null;
        this.link = "";
        this.description = null;
        this.contents = null;
        this.date = null;
        this.image = null;
    }

    public Item(long id, Channel channel, boolean read, String title,
        String author, String link, String description, String contents,
        Date date, byte[] image) {
        super(id);
        assert (channel != null);
        assert (title != null);
        assert (link != null);
        this.channel = channel;
        this.read = read;
        this.title = title;
        this.author = author;
        this.link = link;
        this.description = description;
        this.contents = contents;
        this.date = date;
        this.image = image;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        assert (channel != null);
        this.channel = channel;
    }

    public long getChannelId() {
        return channel.getId();
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        assert (title != null);
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

}
