package com.ratanachai.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.ratanachai.popularmovies.data.MovieContract.MovieEntry;
import com.ratanachai.popularmovies.data.MovieContract.ReviewEntry;
import com.ratanachai.popularmovies.data.MovieContract.VideoEntry;

/**
 *  The Content Provider class for this app
 *  Created by Ratanachai on 15/09/22.
 */
public class MovieProvider extends ContentProvider {

    private MovieDbHelper mOpenHelper;

    // This UriMatcher will match each URI to integer constants defined above.
    // Test this by uncommenting the testUriMatcher test within TestUriMatcher.
    static final int MOVIES = 11;
    static final int MOVIE = 12;
    static final int VIDEOS = 21;
    static final int VIDEO = 22;
    static final int VIDEOS_FOR_MOVIE = 31;
    static final int REVIEWS = 31;
    static final int REVIEW = 32;
    static final int REVIEWS_FOR_MOVIE = 33;
    static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        final String authority = MovieContract.CONTENT_AUTHORITY;
        sUriMatcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIES);
        sUriMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE);
        sUriMatcher.addURI(authority, MovieContract.PATH_VIDEO, VIDEOS);
        sUriMatcher.addURI(authority, MovieContract.PATH_VIDEO + "/#", VIDEO);
        sUriMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/videos", VIDEOS_FOR_MOVIE);
        sUriMatcher.addURI(authority, MovieContract.PATH_REVIEW, REVIEWS);
        sUriMatcher.addURI(authority, MovieContract.PATH_REVIEW + "/#", REVIEW);
        sUriMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/reviews", REVIEWS_FOR_MOVIE);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] proj, String select, String[] selectArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)){
            // "movie"
            case MOVIES: {
                SQLiteDatabase db = mOpenHelper.getReadableDatabase();
                retCursor = db.query(
                        MovieContract.MovieEntry.TABLE_NAME, proj, select, selectArgs, null, null, sortOrder);
                break;
            // "video"
            }case VIDEOS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        VideoEntry.TABLE_NAME, proj, select, selectArgs, null, null, sortOrder);
                break;

//            }case VIDEOS_FOR_MOVIE: {
//                retCursor = mOpenHelper.getReadableDatabase().query(VideoEntry.TABLE_NAME, null,null,null,null,null,null);
//
//                retCursor = mOpenHelper.getReadableDatabase().query(
//                        VideoEntry.TABLE_NAME, proj, select, selectArgs, null, null, sortOrder);
//                        VideoEntry.COLUMN_MOV_KEY + "= ?",
//                        new String[]{Integer.toString(VideoEntry.getMovieIdFromUri(uri))},
//                        new String[]{VideoEntry.getMovieIdFromUri(uri)},
//                        null,
//                        null,
//                        sortOrder);
//                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return MovieEntry.CONTENT_TYPE;
            case MOVIE:
                return MovieEntry.CONTENT_ITEM_TYPE;
            case VIDEOS_FOR_MOVIE:
                return VideoEntry.CONTENT_TYPE;
            case REVIEWS_FOR_MOVIE:
                return ReviewEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
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
