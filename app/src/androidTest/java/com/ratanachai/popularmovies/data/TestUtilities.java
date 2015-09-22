package com.ratanachai.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
    Functions and some test data to make it easier to test your database and Content Provider.
 */
public class TestUtilities extends AndroidTestCase {
    static final Integer MAD_MAX_MOVIE_ID = 76341;

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
        }
    }

    /**
        Use this to create some default values for your database tests.
     */
    static ContentValues createMadmaxMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID, MAD_MAX_MOVIE_ID);
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
    static ContentValues createVideoValuesForMadmax(long movieRowId) {
        ContentValues videoValues = new ContentValues();
        videoValues.put(MovieContract.VideoEntry.COLUMN_MOV_KEY, movieRowId);
        videoValues.put(MovieContract.VideoEntry.COLUMN_KEY, "FRDdRto_3SA");
        videoValues.put(MovieContract.VideoEntry.COLUMN_NAME, "Trailers From Hell");
        videoValues.put(MovieContract.VideoEntry.COLUMN_TYPE, "Featurette");
        videoValues.put(MovieContract.VideoEntry.COLUMN_SITE, "YouTube");
        return videoValues;
    }
    static ContentValues createReviewValuesForMadmax(long movieRowId) {
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

}