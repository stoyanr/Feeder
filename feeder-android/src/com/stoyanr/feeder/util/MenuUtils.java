package com.stoyanr.feeder.util;

import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_ALWAYS;
import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_IF_ROOM;
import static android.support.v4.view.MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;

public class MenuUtils {

    public static void setShowAsActionAll(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem mi = menu.getItem(i);
            int x = (i <= 2) ? SHOW_AS_ACTION_ALWAYS : SHOW_AS_ACTION_IF_ROOM;
            MenuItemCompat.setShowAsAction(mi, x | SHOW_AS_ACTION_WITH_TEXT);
        }
    }

}
