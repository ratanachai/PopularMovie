package com.ratanachai.popularmovies.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 *  Tests for MoviesContract
 *  Created by Ratanachai on 15/09/21.
 */
public class TestMovieContract extends AndroidTestCase {

    private static final long MAD_MAX_ROW_ID = 1L;

    public void testBuildMovieUri(){
        Uri movieUri = MovieContract.MovieEntry.buildMovieUri(MAD_MAX_ROW_ID);
        assertNotNull("Error: Null Uri returned", movieUri);
        assertEquals("Error: Movie ID not appended to the end of Uri",
                Long.toString(MAD_MAX_ROW_ID), movieUri.getLastPathSegment());
        assertEquals("Error: Movie Uri does not match",
                movieUri.toString(), "content://com.ratanachai.popularmovies/movie/1");
    }

    public void testBuildMoviesUri(){
        Uri moviesUri = MovieContract.MovieEntry.CONTENT_URI;
        assertEquals("Error: Movies Uri does not match",
                moviesUri.toString(), "content://com.ratanachai.popularmovies/movie");
    }

    public void testBuildMovieReviewsUri(){
        Uri reviewsUri = MovieContract.ReviewEntry.buildMovieReviewsUri(MAD_MAX_ROW_ID);
        assertEquals("Error: Reviews Uri does not match",
                reviewsUri.toString(), "content://com.ratanachai.popularmovies/movie/1/reviews");
    }
    public void testBuildMovieVideosUri(){
        Uri reviewsUri = MovieContract.VideoEntry.buildMovieVideosUri(MAD_MAX_ROW_ID);
        assertEquals("Error: Videos Uri does not match",
                reviewsUri.toString(), "content://com.ratanachai.popularmovies/movie/1/videos");
    }

}
