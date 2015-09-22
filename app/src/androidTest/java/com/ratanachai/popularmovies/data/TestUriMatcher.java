package com.ratanachai.popularmovies.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by keng on 15/09/22.
 */
public class TestUriMatcher extends AndroidTestCase {

    /** Test for
     * content://com.ratanachai.popularmovies/movie
     * content://com.ratanachai.popularmovies/movie/76341
     * content://com.ratanachai.popularmovies/video?for_movie=76341
     * content://com.ratanachai.popularmovies/review?for_movie=76341
     */
    private static final int TEST_MOVIE_ID = TestUtilities.MAD_MAX_MOVIE_ID;
    private static final Uri TEST_MOVIE_DIR = MovieContract.MovieEntry.buildMoviesUri();
    private static final Uri TEST_MOVIE_DETAIL = MovieContract.MovieEntry.buildMovieUri(TEST_MOVIE_ID);
    private static final Uri TEST_VIDEO_DIR = MovieContract.VideoEntry.buildMovieVideosUri(TEST_MOVIE_ID);
    private static final Uri TEST_REVIEW_DIR = MovieContract.ReviewEntry.buildMovieReviewsUri(TEST_MOVIE_ID);


    public void testUriMatcher(){
        UriMatcher testMatcher = MovieProvider.sURIMatcher;

        assertEquals("Error: Movies URI does not matched",
                testMatcher.match(TEST_MOVIE_DIR), MovieProvider.MOVIES);
        assertEquals("Error: Movie Detail URI does not matched",
                testMatcher.match(TEST_MOVIE_DETAIL), MovieProvider.MOVIE);
//        assertFalse("Error: URI does not matched", testMatcher.match(TEST_VIDEO_DIR) == -1);
//        assertFalse("Error: URI does not matched", testMatcher.match(TEST_REVIEW_DIR) == -1);
//        assertEquals("Error: Videos URI does not matched",
//                testMatcher.match(TEST_VIDEO_DIR), MovieProvider.VIDEOS_FOR_MOVIE);
//        assertEquals("Error: Reviews URI does not matched",
//                testMatcher.match(TEST_REVIEW_DIR), MovieProvider.REVIEWS_FOR_MOVIE);
    }


}
