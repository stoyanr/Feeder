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

package com.stoyanr.feeder.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.stoyanr.feeder.activity.ChannelsActivity;

public class MainService extends Service {

    private static final String TAG = "MainService";

    private static final long SLEEP_INTERVAL_MS = 60 * 1000; // 1 minute

    private ChannelsActivity activity = null;
    private ServiceThread thread = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Creating service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "Starting service");
        return result;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroying service");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Binding to service");
        if (thread == null) {
            thread = new ServiceThread();
            thread.start();
        }
        return new MainBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Unbinding from service");
        if (thread != null) {
            thread.cancel();
            thread.interrupt();
            thread = null;
        }
        return true;
    }

    public class MainBinder extends Binder {
        public void setActivity(ChannelsActivity activity) {
            MainService.this.activity = activity;
        }
    }

    private class ServiceThread extends Thread {

        private boolean cancelled = false;

        public synchronized boolean isCancelled() {
            return cancelled;
        }

        public synchronized void cancel() {
            cancelled = true;
        }

        @Override
        public void run() {
            Log.i(TAG, "Service thread started");
            while (!isCancelled()) {
                try {
                    Thread.sleep(SLEEP_INTERVAL_MS);
                    Log.i(TAG, "Checking if regular jobs should be performed");
                    if (activity != null) {
                        activity.performRegularJobs();
                    }
                } catch (InterruptedException e) {
                }
            }
            Log.i(TAG, "Service thread finished");
        }
    }
}