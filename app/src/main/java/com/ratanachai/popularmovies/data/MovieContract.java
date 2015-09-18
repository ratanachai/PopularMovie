package com.ratanachai.popularmovies.data;

import android.provider.BaseColumns;

/**
 * Defines tables and column names for the app database.
 */
public class MovieContract {

    /* Inner classes that defines the contents of each DB Table in Popular Movie app */

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";
        public static final String COLUMN_TMDB_MOVIE_ID = "tmdb_movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_USER_RATING = "user_rating";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER_PATH = "poster";
    }

    public static final class VideoEntry implements BaseColumns {
        public static final String TABLE_NAME = "video";
        public static final String COLUMN_MOV_KEY = "movie_id";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_SITE = "site";
    }

    public static final class ReviewEntry implements BaseColumns {
        public static final String TABLE_NAME = "review";
        public static final String COLUMN_MOV_KEY = "movie_id";
        public static final String COLUMN_TMDB_REVIEW_ID = "id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_URL = "url";
    }

}