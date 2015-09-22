package com.ratanachai.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by keng on 15/09/22.
 */
public class MovieProvider extends ContentProvider {

    // This UriMatcher will match each URI to integer constants defined above.
    // Test this by uncommenting the testUriMatcher test within TestUriMatcher.
    static final int MOVIES = 11;
    static final int MOVIE = 12;
    static final int VIDEOS_FOR_MOVIE = 21;
    static final int REVIEWS_FOR_MOVIE = 31;
    static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        final String authority = MovieContract.CONTENT_AUTHORITY;
        sURIMatcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIES);
        sURIMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE);
        sURIMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/videos", VIDEOS_FOR_MOVIE);
        sURIMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/reviews", REVIEWS_FOR_MOVIE);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
