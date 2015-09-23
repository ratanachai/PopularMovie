package com.ratanachai.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.ratanachai.popularmovies.data.MovieContract.MovieEntry;
import com.ratanachai.popularmovies.data.MovieContract.ReviewEntry;
import com.ratanachai.popularmovies.data.MovieContract.VideoEntry;

/**
 * Tests for MovieProvider
 * Created by Ratanachai on 15/09/22.
 */
public class TestProvider extends AndroidTestCase {

    public void setUp() throws Exception {
        super.setUp();
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
        //TODO: Change to delete each table via Provider?
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the Provider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(), MovieProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: Provider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {

            assertTrue("Error: Provider not registered at " + mContext.getPackageName(), false);
        }
    }

    public void testGetType() {
        int TEST_MOVIE_ID = TestUtilities.MAD_MAX_MOVIE_ID;

        // URI and expected Return Type
        // content://com.ratanachai.popularmovies/movie
        // vnd.android.cursor.dir/com.ratanachai.popularmovies/movie
        ContentResolver contentResolver = mContext.getContentResolver();
        String type = contentResolver.getType(MovieEntry.buildMoviesUri());
        assertEquals("Error: MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MovieEntry.CONTENT_TYPE, type);
        Log.v("===", type);

        // content://com.ratanachai.popularmovies/movie/76341
        // vnd.android.cursor.item/com.ratanachai.popularmovies/movie
        type = contentResolver.getType(MovieEntry.buildMovieUri(TEST_MOVIE_ID));
        assertEquals("Error: MovieEntry CONTENT_URI with ID should return MovieEntry.CONTENT_ITEM_TYPE",
                MovieEntry.CONTENT_ITEM_TYPE, type);
        Log.v("===", type);

        // content://com.ratanachai.popularmovies/movie/76341/videos
        // vnd.android.cursor.dir/com.ratanachai.popularmovies/videos
        type = contentResolver.getType(VideoEntry.buildMovieVideosUri(TEST_MOVIE_ID));
        assertEquals("Error: VideoEntry CONTENT_URI should return VideoEntry.CONTENT_TYPE",
                VideoEntry.CONTENT_TYPE, type);
        Log.v("===", type);

        // content://com.ratanachai.popularmovies/movie/76341/reviews
        // vnd.android.cursor.dir/com.ratanachai.popularmovies/reviews
        type = contentResolver.getType(ReviewEntry.buildMovieReviewsUri(TEST_MOVIE_ID));
        assertEquals("Error: ReviewEntry CONTENT_URI should return ReviewEntry.CONTENT_TYPE",
                ReviewEntry.CONTENT_TYPE, type);
        Log.v("===", type);

    }


    // Insert directly to DB, then uses the ContentProvider to read out the data.
    public void testBasicMovieQuery(){
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Insert into DB directly
        ContentValues testValues1 = TestUtilities.createMadmaxMovieValues();
        long movieRowId1 = db.insert(MovieEntry.TABLE_NAME, null, testValues1);
        assertTrue("Unable to Insert a Movie into the Database", movieRowId1 != -1);

        // Then query out via Content Provider to compare
        ContentResolver cr = mContext.getContentResolver();

        // Query for all rows should return only 1 row
        Cursor retCursor = cr.query(MovieEntry.CONTENT_URI, null, null, null, null);
        assertEquals("Number of row returned should be 1", 1, retCursor.getCount());
        TestUtilities.validateCursor("testBasicMovieQuery [DIR]: ", retCursor, testValues1);

        // Insert another Movie into DB
        ContentValues testValues2 = TestUtilities.createInterstellarValues();
        long movieRowId2 = db.insert(MovieEntry.TABLE_NAME, null, testValues2);
        assertTrue("Unable to Insert a Movie into the Database", movieRowId2 != -1);

        // Query for all rows now should return only 2 rows
        retCursor = cr.query(MovieEntry.CONTENT_URI, null, null, null, null);
        assertEquals("Number of row returned should be 1", 2, retCursor.getCount());

        // Query for specific row
        retCursor = cr.query(
                MovieEntry.CONTENT_URI,
                null,
                MovieEntry.COLUMN_TMDB_MOVIE_ID + " = ?",
                new String[]{Integer.toString(TestUtilities.MAD_MAX_MOVIE_ID)},
                null);
        TestUtilities.validateCursor("testBasicMovieQuery [ITEM]: ", retCursor, testValues1);

        db.close();
    }

}