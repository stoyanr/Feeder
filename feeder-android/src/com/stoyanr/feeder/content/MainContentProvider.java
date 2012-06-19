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

import com.stoyanr.feeder.model.Channel;
import com.stoyanr.feeder.model.Item;

public class MainContentProvider extends AbstractContentProvider {
    public static final String AUTHORITY = "com.stoyanr.feeder.content";

    private static final String DATABASE_NAME = "feeder.db";
    private static final int DATABASE_VERSION = 10;

    @SuppressWarnings("rawtypes")
    private static final Class[] CLASSES = { Channel.class, Item.class };

    private DatabaseHelper helper = null;

    @SuppressWarnings("unchecked")
    @Override
    public boolean onCreate() {
        helper = new DatabaseHelper(CLASSES, DATABASE_NAME, DATABASE_VERSION,
            getContext());
        super.setHelper(helper);
        super.setAuthority(AUTHORITY);
        super.initialize(CLASSES);
        return true;
    }

}
