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

package com.stoyanr.feeder.util;

import android.view.KeyEvent;

public class KeyUtils {
    private KeyUtils() {
    }

    public static int interpretDirection(int code) {
        switch (code) {
        case KeyEvent.KEYCODE_LEFT_BRACKET:
        case KeyEvent.KEYCODE_DPAD_LEFT:
        case KeyEvent.KEYCODE_1:
        case KeyEvent.KEYCODE_4:
        case KeyEvent.KEYCODE_7:
            return KeyEvent.KEYCODE_DPAD_LEFT;

        case KeyEvent.KEYCODE_RIGHT_BRACKET:
        case KeyEvent.KEYCODE_DPAD_RIGHT:
        case KeyEvent.KEYCODE_3:
        case KeyEvent.KEYCODE_6:
        case KeyEvent.KEYCODE_9:
            return KeyEvent.KEYCODE_DPAD_RIGHT;
        default:
            return code;
        }
    }
}
