package com.stoyanr.feeder.activity;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;

import com.stoyanr.feeder.view.ItemView;

public class ItemsAdapter extends CursorAdapter implements Filterable {

    public ItemsAdapter(Context context, Cursor c) {
        super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((ItemView) view).bindView(cursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ItemView item = new ItemView(context, parent);
        item.bindView(cursor);
        return item;
    }
}