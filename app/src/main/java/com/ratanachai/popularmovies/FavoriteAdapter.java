package com.ratanachai.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

/**
 * CursorAdapter to fetch Favorite Movie from Content Provider to View
 * Created by Ratanachai on 15/10/04.
 */
public class FavoriteAdapter extends CursorAdapter {

    public FavoriteAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}
