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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.stoyanr.feeder.model.Data;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private final Class<? extends Data>[] classes;
    // @formatter:off
    private final Map<Class<? extends Data>, Dao<? extends Data, Long>> daos = 
        new HashMap<Class<? extends Data>, Dao<? extends Data, Long>>();
    private final Map<Class<? extends Data>, DatabaseTableConfig<? extends Data>> tableConfigs = 
        new HashMap<Class<? extends Data>, DatabaseTableConfig<? extends Data>>();
    // @formatter:on

    public DatabaseHelper(Class<? extends Data>[] classes, String name,
        int version, Context ctx) {
        super(ctx, name, null, version);
        this.classes = classes;
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource cs) {
        Log.i(TAG, "Creating database");
        createTables(db, cs);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource cs,
        int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrading database from version " + oldVersion + " to "
            + newVersion);
        switch (oldVersion) {
        default:
            Log.i(TAG, "Version too old, deleting database contents");
            deleteTables(db, cs);
            createTables(db, cs);
            break;
        }
    }

    @Override
    public void close() {
        super.close();
        daos.clear();
        tableConfigs.clear();
    }

    @SuppressWarnings("unchecked")
    public <T extends Data> Dao<T, Long> getDaoEx(Class<T> clazz) {
        Dao<T, Long> result = null;
        if (daos.containsKey(clazz)) {
            result = (Dao<T, Long>) daos.get(clazz);
        } else {
            try {
                result = getDao(clazz);
            } catch (java.sql.SQLException e) {
                throw new SQLException(e.getMessage());
            }
            daos.put(clazz, result);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T extends Data> DatabaseTableConfig<T> getTableConfig(Class<T> clazz) {
        DatabaseTableConfig<T> result = null;
        if (tableConfigs.containsKey(clazz)) {
            result = (DatabaseTableConfig<T>) tableConfigs.get(clazz);
        } else {
            try {
                result = DatabaseTableConfig.fromClass(getConnectionSource(),
                    clazz);
            } catch (java.sql.SQLException e) {
                throw new SQLException(e.getMessage());
            }
            tableConfigs.put(clazz, result);
        }
        return result;
    }

    public <T extends Data> String getTableName(Class<T> clazz) {
        DatabaseTableConfig<? extends Data> cfg = getTableConfig(clazz);
        return cfg.getTableName();
    }

    public <T extends Data> String[] getColumnNames(Class<T> clazz,
        boolean foreignOnly) {
        List<String> columnNames = new ArrayList<String>();
        try {
            DatabaseTableConfig<? extends Data> cfg = getTableConfig(clazz);
            SqliteAndroidDatabaseType dbType = new SqliteAndroidDatabaseType();
            for (FieldType fieldType : cfg.getFieldTypes(dbType)) {
                if (!foreignOnly || fieldType.isForeign()) {
                    columnNames.add(fieldType.getColumnName());
                }
            }
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        return columnNames.toArray(new String[] {});
    }

    public <T extends Data> T queryById(long id, Class<T> clazz) {
        T entity = null;
        try {
            entity = getDaoEx(clazz).queryForId(id);
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        return entity;
    }

    public <T extends Data> long create(Data entity, Class<T> clazz) {
        long id = -1;
        try {
            if (getDaoEx(clazz).create(clazz.cast(entity)) == 1) {
                id = entity.getId();
            }
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
        return id;
    }

    public <T extends Data> void update(Data entity, Class<T> clazz) {
        try {
            int count = getDaoEx(clazz).update(clazz.cast(entity));
            assert (count == 1);
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public <T extends Data> void deleteById(long id, Class<T> clazz) {
        try {
            int count = getDaoEx(clazz).deleteById(id);
            assert (count == 1);
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    private void createTables(SQLiteDatabase db, ConnectionSource cs) {
        for (Class<? extends Data> clazz : classes) {
            createTable(clazz, cs);
        }
    }

    private void deleteTables(SQLiteDatabase db, ConnectionSource cs) {
        for (Class<? extends Data> clazz : classes) {
            dropTable(clazz, cs);
        }
    }

    private void createTable(Class<? extends Data> clazz, ConnectionSource cs) {
        try {
            TableUtils.createTable(cs, clazz);
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    private void dropTable(Class<? extends Data> clazz, ConnectionSource cs) {
        try {
            TableUtils.dropTable(cs, clazz, false);
        } catch (java.sql.SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }
}