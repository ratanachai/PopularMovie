package com.ratanachai.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 *  Defines tables and column names for the app database.
 *  Created by Ratanachai on 2015/09
 */
public class MovieContract {

    // Constants for Content Provider
    public static final String CONTENT_AUTHORITY = "com.ratanachai.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_VIDEO = "video";
    public static final String PATH_REVIEW = "review";

    //TODO: Add these 3 Uris?
    // content://com.ratanachai.popularmovies/movie?sort_by=popularity.desc
    // content://com.ratanachai.popularmovies/movie?sort_by=vote_average.desc
    // content://com.ratanachai.popularmovies/movie?sort_by=favorite

    /** CONTENT URIs will follow patterns below
     *
     * -- Movie URIs --
     * [DIR] content://com.ratanachai.popularmovies/movie
     * [ITEM] content://com.ratanachai.popularmovies/movie/[MOVIE_ID]
     * -- Video URIs --
     * [DIR] content://com.ratanachai.popularmovies/movie/[MOVIE_ID]/videos
     * -- Review URIs --
     * [DIR] content://com.ratanachai.popularmovies/movie/[MOVIE_ID]/reviews
     *
     * NOTE: This is API request URL http://api.themoviedb.org/3/movie/76341/reviews
     */

    /* Inner classes that defines the contents of each DB Table in Popular Movie app */
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
        public static Uri buildMovieUri(int id){
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

        public static Uri buildVideoUri(int id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildMovieVideosUri(int movieId){
            return MovieEntry.CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(movieId))
                    .appendPath("videos").build();
//            return CONTENT_URI.buildUpon().appendQueryParameter(
//                    "for_movie", Integer.toString(movieId)).build();
        }
        public static int getMovieIdFromUri(Uri uri){
            return Integer.parseInt(uri.getQueryParameter("for_movie"));
        }

    }

    public static final class ReviewEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static final String TABLE_NAME = "review";
        public static final String COLUMN_MOV_KEY = "movie_id";
        public static final String COLUMN_TMDB_REVIEW_ID = "id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_URL = "url";

        public static Uri buildReviewUri(int id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildMovieReviewsUri(int movieId){
            return MovieEntry.CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(movieId))
                    .appendPath("reviews").build();

//            return CONTENT_URI.buildUpon().appendQueryParameter(
//                    "for_movie", Integer.toString(movieId)).build();
        }
        public static int getMovieIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getQueryParameter("for_movie"));
        }
    }

}