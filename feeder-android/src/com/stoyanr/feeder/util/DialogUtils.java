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

package com.stoyanr.feeder.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.stoyanr.feeder.R;

public class DialogUtils {

    private DialogUtils() {
    }

    public static void showErrorDialog(Context ctx, String msg) {
        new AlertDialog.Builder(ctx)
            .setTitle(ctx.getResources().getText(R.string.error))
            .setMessage(msg)
            .setPositiveButton(ctx.getResources().getText(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int wb) {
                    }
                }).create().show();
    }
}
