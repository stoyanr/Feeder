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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

import com.stoyanr.feeder.R;
import com.stoyanr.feeder.content.ContentManager;
import com.stoyanr.feeder.download.DownloadManager;
import com.stoyanr.feeder.model.Channel;
import com.stoyanr.feeder.service.MainService;
import com.stoyanr.feeder.sync.Synchronizer;
import com.stoyanr.feeder.util.BitmapCache;
import com.stoyanr.feeder.util.DateUtils;
import com.stoyanr.feeder.util.DialogUtils;
import com.stoyanr.feeder.util.MenuUtils;
import com.stoyanr.feeder.view.ChannelView;

public class ChannelsActivity extends FragmentActivity {

    private static final String TAG = "ChannelsActivity";

    public static final String[] PROJECTION = new String[] { Channel._ID,
        Channel.ICON, Channel.TITLE, Channel.URL, Channel.IMAGE };

    private static final int CHANNELS_LOADER_ID = 0x01;

    private static final long WAIT_INTERVAL_MS = 1000;
    private static final int REGULAR_JOBS_INTERVAL_HOURS = 1;
    private static final int ITEM_AGE_DAYS = 30;

    private static final String PREF_KEYWORDS = "prefKeywords";
    private static final String PREF_LAST_PERFORMED = "prefLastPerformed";

    private ContentManager cm;
    private Handler handler;
    private DownloadManager downloadManager;
    private ChannelsAdapter adapter;
    private GridView gridView;
    private ActionMode actionMode;
    private ActionModeCallback amc;

    private final LoaderManager.LoaderCallbacks<Cursor> clc = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            assert (id == CHANNELS_LOADER_ID);
            return new CursorLoader(ChannelsActivity.this, getIntent()
                .getData(), PROJECTION, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            adapter.changeCursor(cursor);
            ActivityCompat.invalidateOptionsMenu(ChannelsActivity.this);
            BitmapCache.recycle();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapter.changeCursor(null);
            ActivityCompat.invalidateOptionsMenu(ChannelsActivity.this);
            BitmapCache.recycle();
        }
    };

    private final ServiceConnection sc = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // @formatter:off
            ((MainService.MainBinder) service).setActivity(ChannelsActivity.this);
            // @formatter:on
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.channels_activity);
        cm = ContentManager.getInstance(getContentResolver());
        initIntent();
        handler = new Handler();
        downloadManager = new DownloadManager(handler);
        adapter = new ChannelsAdapter(this, null);
        initLoaders();
        initControls();
        bindService(new Intent(this, MainService.class), sc, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        forceLoad();
        performRegularJobs();
        ActivityCompat.invalidateOptionsMenu(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(sc);
    }

    private void initIntent() {
        Intent intent = getIntent();
        if (intent.getData() == null)
            intent.setData(ContentManager.getChannelsUri());
        if (intent.getAction() == null)
            intent.setAction(Intent.ACTION_VIEW);
    }

    private void initControls() {
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                int position, long id) {
                onClick(id);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            gridView.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent,
                    View view, int position, long id) {
                    if (actionMode != null) {
                        return false;
                    }

                    if (amc == null) {
                        amc = new ActionModeCallback();
                    }

                    actionMode = startActionMode(amc);
                    view.setSelected(true);
                    return true;
                }
            });
        } else {
            registerForContextMenu(gridView);
        }
    }

    private void initLoaders() {
        getSupportLoaderManager().initLoader(CHANNELS_LOADER_ID, null, clc);
    }

    private void forceLoad() {
        getSupportLoaderManager().getLoader(CHANNELS_LOADER_ID).forceLoad();
    }

    public synchronized void performRegularJobs() {
        if (DateUtils.hasTimeElapsed(getLastPerformed(), Calendar.HOUR,
            REGULAR_JOBS_INTERVAL_HOURS)) {
            postRegularJobs();
        }
        setLastPerformed(new Date());
    }

    private Date getLastPerformed() {
        long ms = getPreferences(MODE_PRIVATE).getLong(PREF_LAST_PERFORMED, 0);
        return new Date(ms);
    }

    private void setLastPerformed(Date date) {
        getPreferences(MODE_PRIVATE).edit()
            .putLong(PREF_LAST_PERFORMED, date.getTime()).commit();
    }

    private void postRegularJobs() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = adapter.getCursor();
                if (cursor != null) {
                    refreshAll();
                } else {
                    postRegularJobs();
                }
            }
        }, WAIT_INTERVAL_MS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.channels_menu, menu);
        MenuUtils.setShowAsActionAll(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Cursor cursor = adapter.getCursor();
        if (cursor == null || cursor.getCount() == 0) {
            menu.removeGroup(R.id.group_channels);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.subscribe:
            addChannel();
            return true;
        case R.id.settings:
            settings();
            return true;
        case R.id.refresh_all:
            refreshAll();
            return true;
        case R.id.clean:
            cleanItems();
            return true;
        default:
            return false;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
        ContextMenuInfo info) {
        super.onCreateContextMenu(menu, v, info);
        getMenuInflater().inflate(R.menu.channels_context_menu, menu);
        MenuUtils.setShowAsActionAll(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh_channel:
            refreshChannel();
            return true;
        case R.id.remove_channel:
            removeChannel();
            return true;
        default:
            return false;
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.channels_context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            boolean result = onContextItemSelected(item);
            if (result) {
                mode.finish();
            }
            return result;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    };

    private void onClick(long id) {
        String action = getIntent().getAction();
        if (shouldSetResult(action)) {
            setResult(id);
        } else {
            viewChannelItems();
        }
    }

    private boolean shouldSetResult(String action) {
        return action.equals(Intent.ACTION_PICK)
            || action.equals(Intent.ACTION_GET_CONTENT);
    }

    private void setResult(long id) {
        getIntent().setData(ContentManager.getChannelUri(id));
        setResult(RESULT_OK, getIntent());
    }

    private void addChannel() {
        Log.d(TAG, "Adding new channel");
        Uri uri = ContentManager.getChannelsUri();
        startActivity(new Intent(Intent.ACTION_INSERT, uri));
    }

    private void viewChannelItems() {
        Cursor cursor = adapter.getCursor();
        assert (cursor != null);
        long channelId = ContentManager.getChannelId(cursor);
        Log.d(TAG, "Viewing items for channel " + channelId);
        Uri uri = ContentManager.getChannelItemsUri(channelId);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(ItemsActivity.EXTRA_CHANNEL_IDS, getChannelIds());
        intent.putExtra(ItemsActivity.EXTRA_KEYWORDS, getKeywordsAsArrayList());
        intent.putExtra(ItemsActivity.EXTRA_FEATURED_ONLY, false);
        startActivity(intent);
    }

    private void removeChannel() {
        Cursor cursor = adapter.getCursor();
        assert (cursor != null);
        long channelId = ContentManager.getChannelId(cursor);
        Log.d(TAG, "Removing channel " + channelId);
        new Task(TaskType.DELETE_CHANNELS).execute(channelId);
    }

    private void cleanItems() {
        Log.d(TAG, "Cleaning old items");
        new Task(TaskType.CLEAN_ITEMS).execute(0L);
    }

    private void refreshAll() {
        Cursor cursor = adapter.getCursor();
        assert (cursor != null);
        Log.d(TAG, "Refreshing all channels");
        if (cursor.moveToFirst() == false)
            return;
        do {
            refreshChannel();
        } while (cursor.moveToNext() == true);
    }

    private void refreshChannel() {
        Cursor cursor = adapter.getCursor();
        assert (cursor != null);
        long channelId = ContentManager.getChannelId(cursor);
        String channelUrl = ContentManager.getChannelUrl(cursor);
        Log.d(TAG, "Refreshing channel " + channelId + " with URL "
            + channelUrl);
        ChannelView view = getChannelView(channelId);
        assert (view != null);
        downloadManager.schedule(new RefreshRunnable(handler, view, channelId,
            channelUrl));
    }

    private void settings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private long[] getChannelIds() {
        Cursor cursor = adapter.getCursor();
        assert (cursor != null);
        long[] result = new long[cursor.getCount()];
        int i = 0;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            result[i++] = (ContentManager.getChannelId(cursor));
        }
        return result;
    }

    private ArrayList<String> getKeywordsAsArrayList() {
        return new ArrayList<String>(Arrays.asList(getKeywords()));
    }

    private String[] getKeywords() {
        SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(getBaseContext());
        String keywords = prefs.getString(PREF_KEYWORDS, "");
        return (keywords.length() > 0) ? keywords.split(" ") : new String[] {};
    }

    private ChannelView getChannelView(long channelId) {
        ChannelsAdapter adapter = (ChannelsAdapter) gridView.getAdapter();
        return adapter.getView(channelId);
    }

    private class RefreshRunnable implements Runnable {
        private final Handler handler;
        private final ChannelView view;
        private final long channelId;
        private final String channelUrl;

        public RefreshRunnable(Handler handler, ChannelView view,
            long channelId, String channelUrl) {
            this.handler = handler;
            this.view = view;
            this.channelId = channelId;
            this.channelUrl = channelUrl;
        }

        @Override
        public void run() {
            postStartRefresh();
            try {
                refreshChannel();
            } catch (Exception e) {
                postShowError(e.getMessage());
            }
            postFinishRefresh();
        }

        private void refreshChannel() throws Exception {
            Synchronizer refresh = new Synchronizer(ChannelsActivity.this);
            refresh.sync(channelId, channelUrl);
        }

        private void postStartRefresh() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    view.startRefresh();
                }
            });
        }

        private void postFinishRefresh() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    view.finishRefresh();
                    forceLoad();
                }
            });
        }

        private void postShowError(final String msg) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    DialogUtils.showErrorDialog(ChannelsActivity.this, msg);
                }
            });
        }
    }

    public enum TaskType {
        DELETE_CHANNELS, CLEAN_ITEMS
    };

    private class Task extends AsyncTask<Long, Void, Long> {

        private final TaskType type;

        public Task(TaskType type) {
            this.type = type;
        }

        @Override
        protected Long doInBackground(Long... params) {
            switch (type) {
            case DELETE_CHANNELS:
                for (Long param : params) {
                    deleteChannel(param.longValue());
                }
                break;
            case CLEAN_ITEMS:
                cleanItems();
                break;
            }
            BitmapCache.clear();
            postForceLoad();
            return new Long(0);
        }

        private void deleteChannel(long channelId) {
            cm.deleteChannelItems(channelId);
            cm.deleteChannelById(channelId);
        }

        private void cleanItems() {
            cm.deleteOldItems(Calendar.DAY_OF_YEAR, ITEM_AGE_DAYS, true);
        }

        private void postForceLoad() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    forceLoad();
                }
            });
        }
    }
}
