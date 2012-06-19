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

import com.stoyanr.feeder.annotations.DefaultSortOrder;
import com.stoyanr.feeder.annotations.MimeType;
import com.stoyanr.feeder.annotations.UriPaths;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Data.TABLE_NAME_PFX + Channel.ENTITY)
@UriPaths({ Channel.ENTITY_PL, Channel.ENTITY_PL + "/#" })
@MimeType(Data.MIME_TYPE_PFX + Channel.ENTITY)
@DefaultSortOrder(Channel.TITLE + " ASC")
public class Channel extends Data {
    public static final String ENTITY = "channel";
    public static final String ENTITY_PL = ENTITY + "s";

    public static final String URL = "url";
    public static final String TITLE = "title";
    public static final String ICON = "icon";
    public static final String LINK = "link";
    public static final String DESCRIPTION = "description";
    public static final String LANGUAGE = "language";
    public static final String IMAGE = "image";

    @DatabaseField(columnName = URL, unique = true, canBeNull = false)
    private String url;

    @DatabaseField(columnName = TITLE, unique = true, canBeNull = false)
    private String title;

    @DatabaseField(columnName = ICON, dataType = DataType.BYTE_ARRAY)
    private byte[] icon;

    @DatabaseField(columnName = LINK, canBeNull = false)
    private String link;

    @DatabaseField(columnName = DESCRIPTION)
    private String description;

    @DatabaseField(columnName = LANGUAGE)
    private String language;

    @DatabaseField(columnName = IMAGE, dataType = DataType.BYTE_ARRAY)
    private byte[] image;

    public Channel() {
        super();
        this.url = "";
        this.title = "";
        this.icon = null;
        this.link = "";
        this.description = null;
        this.language = null;
        this.image = null;
    }

    public Channel(long id, String url, String title, byte[] icon, String link,
        String description, String language, byte[] image) {
        super(id);
        assert (url != null);
        assert (title != null);
        assert (link != null);
        this.url = url;
        this.title = title;
        this.icon = icon;
        this.link = link;
        this.description = description;
        this.language = language;
        this.image = image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        assert (url != null);
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        assert (title != null);
        this.title = title;
    }

    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        assert (link != null);
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
