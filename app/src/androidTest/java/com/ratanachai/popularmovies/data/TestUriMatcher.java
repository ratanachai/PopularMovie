package com.ratanachai.popularmovies.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 *  Test for UriMatcher
 *  Created by Ratanachai on 15/09/22.
 */
public class TestUriMatcher extends AndroidTestCase {

    /** Test for
     * content://com.ratanachai.popularmovies/movie
     * content://com.ratanachai.popularmovies/movie/1
     * content://com.ratanachai.popularmovies/movie/1/videos
     * content://com.ratanachai.popularmovies/movie/1/reviews
     */
    private static final long TEST_MOV_ROW_ID = 1L;
    private static final Uri TEST_MOVIE_DIR = MovieContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_MOVIE_DETAIL = MovieContract.MovieEntry.buildMovieUri(TEST_MOV_ROW_ID);
    private static final Uri TEST_VIDEO_DIR = MovieContract.VideoEntry.buildMovieVideosUri(TEST_MOV_ROW_ID);
    private static final Uri TEST_REVIEW_DIR = MovieContract.ReviewEntry.buildMovieReviewsUri(TEST_MOV_ROW_ID);


    public void testUriMatcher(){
        UriMatcher testMatcher = MovieProvider.sUriMatcher;

        assertEquals("Error: Movies URI does not matched",
                testMatcher.match(TEST_MOVIE_DIR), MovieProvider.MOVIES);
        assertEquals("Error: Movie Detail URI does not matched",
                testMatcher.match(TEST_MOVIE_DETAIL), MovieProvider.MOVIE);
        assertFalse("Error: URI does not matched", testMatcher.match(TEST_VIDEO_DIR) == -1);
        assertFalse("Error: URI does not matched", testMatcher.match(TEST_REVIEW_DIR) == -1);
        assertEquals("Error: Videos URI does not matched",
                testMatcher.match(TEST_VIDEO_DIR), MovieProvider.VIDEOS_FOR_MOVIE);
        assertEquals("Error: Reviews URI does not matched",
                testMatcher.match(TEST_REVIEW_DIR), MovieProvider.REVIEWS_FOR_MOVIE);
    }


}
