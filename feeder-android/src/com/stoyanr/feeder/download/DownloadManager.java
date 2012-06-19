/*
 * $Id: $
 *
 * Copyright (C) 2012 Stoyan Rachev (stoyanr@gmail.com)
 * Copyright (C) 2007 Josh Guilfoyle (jasta@devtcg.org)
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

package com.stoyanr.feeder.download;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.os.Handler;

/**
 * Generic network connection manager to gracefully throttle and control
 * unpredictable download threads.
 * 
 * This class implements a strategy to gracefully satisfy the "refresh all"
 * semantics. The default behavior is to spawn a new connection {@link Thread}
 * every 8 seconds or whenever the previously queued {@link Thread} finishes
 * execution. There is no specific awareness of what action is being performed
 * by the spawned {@link Thread}, nor is it even necessary that the
 * {@link Thread} utilizes the network.
 * 
 * @see Thread
 * @see android.os.Handler
 */
public class DownloadManager {

    private static final int TIMEOUT = 4000;

    private final Handler handler;

    // @formatter:off
    private final ConcurrentLinkedQueue<DownloadThread> threads = 
        new ConcurrentLinkedQueue<DownloadThread>();
    private final ConcurrentLinkedQueue<Runnable> queue = 
        new ConcurrentLinkedQueue<Runnable>();
    // @formatter:on

    /**
     * Associate a new DownloadManager instance with a thread's message queue.
     */
    public DownloadManager(Handler handler) {
        this.handler = handler;
    }

    /**
     * Schedule a new download worker.
     */
    public boolean schedule(Runnable r) {
        queue.add(r);
        if (threads.size() == 0) {
            handler.removeCallbacks(wakeUpThread);
            wakeUpThread.run();
            return true;
        }
        return false;
    }

    private Runnable wakeUpThread = new Runnable() {
        @Override
        public void run() {
            Runnable r = queue.poll();
            if (r == null) {
                return;
            }

            // Flag all currently processing workers as being too slow, so that
            // we know not to wake up when they finish. This would cause
            // connections that take just slightly longer than TIMEOUT to add a
            // new concurrent connection every TIMEOUT msec.
            for (DownloadThread t : threads)
                t.setTooSlow();

            DownloadThread t = new DownloadThread(threads, handler, r);
            threads.add(t);
            t.start();

            // Schedule a check-up in TIMEOUT msec. This will be bumped if t
            // finishes within that window.
            handler.removeCallbacks(this);
            handler.postDelayed(this, TIMEOUT);
        }
    };

    /**
     * Wraps the invocation of queued Runnables so that we can trap their exit.
     * Normally, this would be done with a Handler, but our implementation is
     * non-invasive to the Activity's Handler.
     */
    private class DownloadThread extends Thread {

        private final Collection<DownloadThread> activeThreads;
        private final Handler handler;
        private final Runnable runnable;
        private boolean tooSlow = false;

        public DownloadThread(Collection<DownloadThread> activeThreads,
            Handler handler, Runnable runnable) {
            this.activeThreads = activeThreads;
            this.handler = handler;
            this.runnable = runnable;
        }

        public void setTooSlow() {
            tooSlow = true;
        }

        @Override
        public void run() {
            runnable.run();

            // There may be another entry waiting in the queue for this one to
            // finish. So, bump the wait callback up to run now.
            activeThreads.remove(this);

            if (!tooSlow) {
                handler.removeCallbacks(wakeUpThread);
                handler.post(wakeUpThread);
            }
        }
    }
}
