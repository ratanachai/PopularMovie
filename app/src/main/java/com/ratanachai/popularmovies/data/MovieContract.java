package com.ratanachai.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.List;
/**
 *  Defines Tables and column names for the app database.
 */

public class MovieContract {

    // Constants for Content Provider
    public static final String CONTENT_AUTHORITY = "com.ratanachai.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_VIDEO = "video";
    public static final String PATH_REVIEW = "review";

    public static int getMovieIdFromUri(Uri uri){
        List<String> segment = uri.getPathSegments();
        int movieId = Integer.parseInt(segment.get(1));
        return movieId;
    }

    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = "movie";
        public static final String COLUMN_TMDB_MOVIE_ID = "tmdb_movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_USER_RATING = "user_rating";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER_PATH = "poster";

        // URI Builders
        public static Uri buildMoviesUri(){
            return CONTENT_URI;
        }
        public static Uri buildMovieUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class VideoEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEO).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;
        
        public static final String TABLE_NAME = "video";
        public static final String COLUMN_MOV_KEY = "movie_id";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_SITE = "site";

        public static Uri buildVideoUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildMovieVideosUri(long movieId){
            return MovieEntry.CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(movieId))
                    .appendPath("videos").build();
        }
    }

    public static final class ReviewEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static final String TABLE_NAME = "review";
        public static final String COLUMN_MOV_KEY = "movie_id"; //TODO: Be careful movie_id is the ID from URI
        public static final String COLUMN_TMDB_REVIEW_ID = "id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_URL = "url";

        public static Uri buildReviewUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildMovieReviewsUri(long movieId){
            return MovieEntry.CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(movieId))
                    .appendPath("reviews").build();
        }
    }

}