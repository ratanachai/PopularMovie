package com.ratanachai.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;


/**
 *  Utilities class providing Functions and some test data to make it easier
 *  to test database and Content Provider.
 *  Created by Ratanachai on 2015/09.
 */

public class TestUtilities extends AndroidTestCase {
    static final Long MAD_MAX_TMDB_ID = 76341L;
    static final Long INTERSTELLAR_TMDB_ID = 157336L;

    /**
     *  Helper functions to validate cursor
     */
    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue(error + "Empty cursor returned", valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }
    //TODO: This method yeild not error when comparing cursor with more than one row
    // with a single expectedValue because it compare only with first row.
    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
            //TODO: Wrong error string, only take value from ContentValues on both end.
        }
    }

    /**
        Use this to create some default values for your database tests.
     */
    static ContentValues createMadmaxMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID, MAD_MAX_TMDB_ID);
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Mad Max: Fury Road (2015)");
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "An apocalyptic story set in the " +
                "furthest reaches of our planet, in a stark desert landscape where humanity is broken, " +
                "and most everyone is crazed fighting for the necessities of life. Within this world " +
                "exist two rebels on the run who just might be able to restore order. There's Max, a " +
                "man of action and a man of few words, who seeks peace of mind following the loss of " +
                "his wife and child in the aftermath of the chaos. And Furiosa, a woman of action and " +
                "a woman who believes her path to survival may be achieved if she can make it across " +
                "the desert back to her childhood homeland.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, 7.6);
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "2015-05-15");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "/kqjL17yufvn9OVLyXYpvtyrFfak.jpg");
        return movieValues;
    }
    static ContentValues createInterstellarValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID, INTERSTELLAR_TMDB_ID);
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Interstellar");
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "Interstellar chronicles the " +
                "adventures of a group of explorers who make use of a newly discovered wormhole " +
                "to surpass the limitations on human space travel and conquer the vast distances " +
                "involved in an interstellar voyage.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, 8.3);
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "2014-11-05");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg");
        return movieValues;
    }

    static ContentValues createVideo1ValuesForMovie(long movieRowId) {
        ContentValues videoValues = new ContentValues();
        videoValues.put(MovieContract.VideoEntry.COLUMN_MOV_KEY, movieRowId);
        videoValues.put(MovieContract.VideoEntry.COLUMN_KEY, "FRDdRto_3SA");
        videoValues.put(MovieContract.VideoEntry.COLUMN_NAME, "Trailers From Hell");
        videoValues.put(MovieContract.VideoEntry.COLUMN_TYPE, "Featurette");
        videoValues.put(MovieContract.VideoEntry.COLUMN_SITE, "YouTube");
        return videoValues;
    }

    static ContentValues createVideo2ValuesForMovie(long movieRowId) {
        ContentValues videoValues = new ContentValues();
        videoValues.put(MovieContract.VideoEntry.COLUMN_MOV_KEY, movieRowId);
        videoValues.put(MovieContract.VideoEntry.COLUMN_KEY, "jnsgdqppAYA");
        videoValues.put(MovieContract.VideoEntry.COLUMN_NAME, "Trailer 2");
        videoValues.put(MovieContract.VideoEntry.COLUMN_TYPE, "Trailer");
        videoValues.put(MovieContract.VideoEntry.COLUMN_SITE, "YouTube");
        return videoValues;
    }

    static ContentValues createReview1ValuesForMovie(long movieRowId) {
        ContentValues reviewValues = new ContentValues();
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOV_KEY, movieRowId);
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_TMDB_REVIEW_ID, "55660928c3a3687ad7001db1");
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, "Phileas Fogg");
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT, "Fabulous action movie. Lots of " +
                "interesting characters. They don't make many movies like this. The whole movie from" +
                " start to finish was entertaining I'm looking forward to seeing it again. I definitely " +
                "recommend seeing it.");
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_URL, "http://j.mp/1HLTNzT");
        return reviewValues;
    }
    static ContentValues createReview2ValuesForMovie(long movieRowId) {
        ContentValues reviewValues = new ContentValues();
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOV_KEY, movieRowId);
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_TMDB_REVIEW_ID, "55732a53925141456e000639");
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, "Andres Gomez");
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT, "Good action movie with a decent " +
                "script for the genre. The photography is really good too but, in the end, it is " +
                "quite repeating itself from beginning to end and the stormy OST is exhausting.");
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_URL, "http://j.mp/1dUnvpG");
        return reviewValues;
    }



    static long insertMovie(Context context, ContentValues contentValues){
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, contentValues);
        assertTrue("Error: cannot insert a movie", rowId != -1);

        db.close();
        return rowId;
    }
}