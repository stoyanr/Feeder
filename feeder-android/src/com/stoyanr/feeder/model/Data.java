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

import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;

public class Data implements BaseColumns {

    public static final String TABLE_NAME_PFX = "feeder_";
    public static final String MIME_TYPE_PFX = "vnd.feeder.";

    @DatabaseField(columnName = _ID, generatedId = true)
    private final long id;

    Data() {
        this.id = -1;
    }

    public Data(long id) {
        super();
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
