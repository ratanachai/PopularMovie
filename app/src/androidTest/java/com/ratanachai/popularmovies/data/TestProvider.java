package com.ratanachai.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
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

    private static final int MAD_MAX_TMDB_ID = TestUtilities.MAD_MAX_TMDB_ID;

    /*
       This helper function deletes all records from both database tables using the database
       functions only.  This is designed to be used to reset the state of the database until the
       delete functionality is available in the ContentProvider.
     */
    public void deleteAllRecordsFromDB() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(MovieEntry.TABLE_NAME, null, null);
        db.delete(VideoEntry.TABLE_NAME, null, null);
        db.delete(ReviewEntry.TABLE_NAME, null, null);
        db.close();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecordsFromDB();
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
        type = contentResolver.getType(MovieEntry.buildMovieUri(MAD_MAX_TMDB_ID));
        assertEquals("Error: MovieEntry CONTENT_URI with ID should return MovieEntry.CONTENT_ITEM_TYPE",
                MovieEntry.CONTENT_ITEM_TYPE, type);
        Log.v("===", type);

        // content://com.ratanachai.popularmovies/movie/76341/videos
        // vnd.android.cursor.dir/com.ratanachai.popularmovies/videos
        type = contentResolver.getType(VideoEntry.buildMovieVideosUri(MAD_MAX_TMDB_ID));
        assertEquals("Error: VideoEntry CONTENT_URI should return VideoEntry.CONTENT_TYPE",
                VideoEntry.CONTENT_TYPE, type);
        Log.v("===", type);

        // content://com.ratanachai.popularmovies/movie/76341/reviews
        // vnd.android.cursor.dir/com.ratanachai.popularmovies/reviews
        type = contentResolver.getType(ReviewEntry.buildMovieReviewsUri(MAD_MAX_TMDB_ID));
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
        assertEquals("Number of row returned incorrect", 2, retCursor.getCount());

        // Query for specific row
        retCursor = cr.query(MovieEntry.buildMovieUri(MAD_MAX_TMDB_ID), null, null, null, null);
        TestUtilities.validateCursor("testBasicMovieQuery [ITEM]: ", retCursor, testValues1);

        db.close();
    }

    public void testBasicVideoQuery() {

        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Insert into DB directly
        long madMaxRowId = TestUtilities.insertMovie(mContext, TestUtilities.createMadmaxMovieValues());
        Cursor retCursor = db.query(MovieEntry.TABLE_NAME, null, null, null, null, null, null);
        assertEquals("Number of row returned incorrect", 1, retCursor.getCount());

        // Query out Movie via Provider (just double check)
        ContentResolver cr = mContext.getContentResolver();
        retCursor = cr.query(MovieEntry.CONTENT_URI, null, null, null, null);
        Log.v("===", DatabaseUtils.dumpCursorToString(retCursor));
        assertEquals("Number of row returned incorrect", 1, retCursor.getCount());

        ContentValues videoValue1 = TestUtilities.createVideo1ValuesForMovie(madMaxRowId);
        long rowId1 = db.insert(VideoEntry.TABLE_NAME, null, videoValue1);
        assertTrue("Unable to Insert a video into DB", rowId1 != -1);

        ContentValues videoValue2 = TestUtilities.createVideo2ValuesForMovie(madMaxRowId);
        long rowId2 = db.insert(VideoEntry.TABLE_NAME, null, videoValue2);
        assertTrue("Unable to Insert a video into DB", rowId2 != -1);

        retCursor = db.query(VideoEntry.TABLE_NAME, null, null, null, null, null, null);
        assertEquals("Number of row returned should be 2", 2, retCursor.getCount());

        // Query out Videos via Provider
        retCursor = cr.query(VideoEntry.CONTENT_URI, null, null, null, null);
        assertEquals("Number of row returned should be 2", 2, retCursor.getCount());

        db.close();
    }

}