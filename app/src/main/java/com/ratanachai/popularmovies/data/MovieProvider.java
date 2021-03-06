package com.ratanachai.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.ratanachai.popularmovies.data.MovieContract.MovieEntry;
import com.ratanachai.popularmovies.data.MovieContract.ReviewEntry;
import com.ratanachai.popularmovies.data.MovieContract.VideoEntry;

/**
 *  The Content Provider class for this app
 *  Created by Ratanachai on 15/09/22.
 *
 *  ** Content URIs will follow patterns below **
 *  -- Movie URIs --
 *  [DIR] content://com.ratanachai.popularmovies/movie
 *  [ITEM] content://com.ratanachai.popularmovies/movie/[MOV_ID]
 *  -- Video URIs --
 *  [DIR] content://com.ratanachai.popularmovies/video
 *  [DIR] content://com.ratanachai.popularmovies/movie/[MOV_ID]/videos
 *  -- Review URIs --
 *  [DIR] content://com.ratanachai.popularmovies/review
 *  [DIR] content://com.ratanachai.popularmovies/movie/[MOV_ID]/reviews
 *
 *  NOTE: This is API request URL http://api.themoviedb.org/3/movie/76341/reviews
 *
 *  Created by Ratanachai on 2015/09
 */

public class MovieProvider extends ContentProvider {

    private MovieDbHelper mOpenHelper;

    // UriMatcher will match each URI to integer constants defined here.
    static final int MOVIES = 11;
    static final int MOVIE = 12;
    static final int VIDEOS = 21;
    static final int VIDEO = 22;
    static final int VIDEOS_FOR_MOVIE = 23;
    static final int REVIEWS = 31;
    static final int REVIEW = 32;
    static final int REVIEWS_FOR_MOVIE = 33;
    static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static final SQLiteQueryBuilder sVideoByMovieQueryBuilder = new SQLiteQueryBuilder();
    static final SQLiteQueryBuilder sReviewByMovieQueryBuilder = new SQLiteQueryBuilder();

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

        // Define INNER JOIN in FROM part here
        sVideoByMovieQueryBuilder.setTables(
                VideoEntry.TABLE_NAME + " INNER JOIN " + MovieEntry.TABLE_NAME +
                " ON " + VideoEntry.TABLE_NAME + "." + VideoEntry.COLUMN_MOV_KEY +
                " = " + MovieEntry.TABLE_NAME + "." + MovieEntry._ID);
        sReviewByMovieQueryBuilder.setTables(
                ReviewEntry.TABLE_NAME + " INNER JOIN " + MovieEntry.TABLE_NAME +
                " ON " + ReviewEntry.TABLE_NAME + "." + ReviewEntry.COLUMN_MOV_KEY +
                " = " + MovieEntry.TABLE_NAME + "." + MovieEntry._ID);
    }

    static final String sMovieIdSelection = MovieEntry.TABLE_NAME + "." + MovieEntry._ID + " = ? ";

    private Cursor getVideobyMovieId(Uri uri, String[] proj, String sortOrder) {
        long mov_id = MovieContract.getMovieIdFromUri(uri);
        String select = sMovieIdSelection;
        String[] selectArgs = new String[]{Long.toString(mov_id)};

        return sVideoByMovieQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                proj,
                select,
                selectArgs,
                null,
                null,
                sortOrder);
    }
    private Cursor getReviewbyMovieId(Uri uri, String[] proj, String sortOrder){
        long mov_id = MovieContract.getMovieIdFromUri(uri);
        String select = sMovieIdSelection;
        String[] selectArgs = new String[]{Long.toString(mov_id)};

        return sReviewByMovieQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                proj,
                select,
                selectArgs,
                null,
                null,
                sortOrder);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
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
    public Cursor query(Uri uri, String[] proj, String select, String[] selectArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)){
            // "movie"
            case MOVIES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME, proj, select, selectArgs, null, null, sortOrder);
                break;

            // "movie/[MOV_ID]
            }case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME, proj,
                        MovieEntry._ID + " = ?",
                        new String[]{Long.toString(MovieContract.getMovieIdFromUri(uri))},
                        null,
                        null,
                        sortOrder);
                break;

            // "video"
            }case VIDEOS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        VideoEntry.TABLE_NAME, proj, select, selectArgs, null, null, sortOrder);
                break;

            // "movie/[MOV_ID]/videos
            }case VIDEOS_FOR_MOVIE: {
                retCursor = getVideobyMovieId(uri, proj, sortOrder);
                break;

            // "review"
            }case REVIEWS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ReviewEntry.TABLE_NAME, proj, select, selectArgs, null, null, sortOrder);
                break;

            // "movie/[MOV_ID]/reviews
            }case REVIEWS_FOR_MOVIE: {
                retCursor = getReviewbyMovieId(uri, proj, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Watch queried URI for a change, The listener attached to this resolver will be notified.
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri retUri;

        switch (match) {
            case MOVIES: {
                long _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    retUri = MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                break;
            }
            case VIDEOS: {
                long _id = db.insert(VideoEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    retUri = VideoEntry.buildVideoUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEWS: {
                long _id = db.insert(ReviewEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    retUri = ReviewEntry.buildReviewUri(_id);
                else
                    throw new android.database.SQLException("Fail to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        // Notify registered observer that content in URI is changed
        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    @Override
    public int delete(Uri uri, String select, String[] selectArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        if (select == null) select = "1"; // This makes Delete All rows and return num of row deleted;

        switch (match){
            case MOVIES:
                rowsDeleted = db.delete(MovieEntry.TABLE_NAME, select, selectArgs);
                break;
            case VIDEOS:
                rowsDeleted = db.delete(VideoEntry.TABLE_NAME, select, selectArgs);
                break;
            case REVIEWS:
                rowsDeleted = db.delete(ReviewEntry.TABLE_NAME, select, selectArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if (rowsDeleted != 0) getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String select, String[] selectArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match){
            case MOVIES:
                rowsUpdated = db.update(MovieEntry.TABLE_NAME, values, select, selectArgs);
                    break;

            case VIDEOS:
                rowsUpdated = db.update(VideoEntry.TABLE_NAME, values, select, selectArgs);
                break;

            case REVIEWS:
                rowsUpdated = db.update(ReviewEntry.TABLE_NAME, values, select, selectArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        // Notify registered observer that content in URI is changed
        if (rowsUpdated != 0) { getContext().getContentResolver().notifyChange(uri, null); }
        return rowsUpdated;
    }
}
