package com.ratanachai.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * CursorAdapter to fetch Favorite Movie from Content Provider to View
 * Created by Ratanachai on 15/10/04.
 */
public class FavoriteMovieAdapter extends CursorAdapter {

    public FavoriteMovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private String convertCursorRowToUXFormat(Cursor cursor) {

        String title = cursor.getString(Movie.COL_TITLE);
        int tmdb_id = cursor.getInt(Movie.COL_MOVIE_ROW_ID);

        return Integer.toString(tmdb_id) + ": " + title;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_favorite, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = (TextView)view;
        tv.setText(convertCursorRowToUXFormat(cursor));
    }
}
