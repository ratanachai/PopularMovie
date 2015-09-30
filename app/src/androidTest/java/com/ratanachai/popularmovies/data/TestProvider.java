package com.ratanachai.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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
    public static final String LOG_TAG = TestProvider.class.getSimpleName();
    private static final long MAD_MAX_TMDB_ID = TestUtilities.MAD_MAX_TMDB_ID;

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

        // content://com.ratanachai.popularmovies/movie/76341
        // vnd.android.cursor.item/com.ratanachai.popularmovies/movie
        type = contentResolver.getType(MovieEntry.buildMovieUri(MAD_MAX_TMDB_ID));
        assertEquals("Error: MovieEntry CONTENT_URI with ID should return MovieEntry.CONTENT_ITEM_TYPE",
                MovieEntry.CONTENT_ITEM_TYPE, type);

        // content://com.ratanachai.popularmovies/movie/76341/videos
        // vnd.android.cursor.dir/com.ratanachai.popularmovies/videos
        type = contentResolver.getType(VideoEntry.buildMovieVideosUri(MAD_MAX_TMDB_ID));
        assertEquals("Error: VideoEntry CONTENT_URI should return VideoEntry.CONTENT_TYPE",
                VideoEntry.CONTENT_TYPE, type);

        // content://com.ratanachai.popularmovies/movie/76341/reviews
        // vnd.android.cursor.dir/com.ratanachai.popularmovies/reviews
        type = contentResolver.getType(ReviewEntry.buildMovieReviewsUri(MAD_MAX_TMDB_ID));
        assertEquals("Error: ReviewEntry CONTENT_URI should return ReviewEntry.CONTENT_TYPE",
                ReviewEntry.CONTENT_TYPE, type);

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

        // Insert Movie into DB directly
        long madMaxRowId = TestUtilities.insertMovie(mContext, TestUtilities.createMadmaxMovieValues());
        Cursor retCursor = db.query(MovieEntry.TABLE_NAME, null, null, null, null, null, null);
        assertEquals("Number of row returned incorrect", 1, retCursor.getCount());

        // Query out Movie via Provider (just double check)
        ContentResolver cr = mContext.getContentResolver();
        retCursor = cr.query(MovieEntry.CONTENT_URI, null, null, null, null);
//        Log.d(LOG_TAG, DatabaseUtils.dumpCursorToString(retCursor));
        assertEquals("Number of row returned incorrect", 1, retCursor.getCount());

        // Insert Video into DB directly
        ContentValues videoValue1 = TestUtilities.createVideo1ValuesForMovie(madMaxRowId);
        long rowId1 = db.insert(VideoEntry.TABLE_NAME, null, videoValue1);
        assertTrue("Unable to Insert a video into DB", rowId1 != -1);

        ContentValues videoValue2 = TestUtilities.createVideo2ValuesForMovie(madMaxRowId);
        long rowId2 = db.insert(VideoEntry.TABLE_NAME, null, videoValue2);
        assertTrue("Unable to Insert a video into DB", rowId2 != -1);

        retCursor = db.query(VideoEntry.TABLE_NAME, null, null, null, null, null, null);
        assertEquals("Number of row returned incorrect", 2, retCursor.getCount());

        // Query out Videos via Provider
        retCursor = cr.query(VideoEntry.CONTENT_URI, null, null, null, null);
        assertEquals("Number of row returned incorrect", 2, retCursor.getCount());
        retCursor = cr.query(VideoEntry.buildMovieVideosUri(MAD_MAX_TMDB_ID), null, null, null, null);
        assertEquals("Number of row returned incorrect", 2, retCursor.getCount());
        Log.d(LOG_TAG, DatabaseUtils.dumpCursorToString(retCursor));

        db.close();
    }

    public void testBasicReviewQuery() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Insert Reviews into DB directly
        long madMaxRowId = TestUtilities.insertMovie(mContext, TestUtilities.createMadmaxMovieValues());
        ContentValues reviewValue1 = TestUtilities.createReview1ValuesForMovie(madMaxRowId);
        long rowId1 = db.insert(ReviewEntry.TABLE_NAME, null, reviewValue1);
        assertTrue("Unable to Insert a review into DB", rowId1 != -1);

        ContentValues reviewValue2 = TestUtilities.createReview2ValuesForMovie(madMaxRowId);
        long rowId2 = db.insert(ReviewEntry.TABLE_NAME, null, reviewValue2);
        assertTrue("Unable to Insert a review into DB", rowId2 != -1);

        Cursor retCursor = db.query(ReviewEntry.TABLE_NAME, null, null, null, null, null, null);
        assertEquals("Number of row returned incorrect", 2, retCursor.getCount());

        // Query out Reviews via Content Provider
        ContentResolver cr = mContext.getContentResolver();
        retCursor = cr.query(ReviewEntry.CONTENT_URI, null, null, null, null);
        Log.d(LOG_TAG, DatabaseUtils.dumpCursorToString(retCursor));
        assertEquals("Number of row returned incorrect", 2, retCursor.getCount());

        retCursor = cr.query(ReviewEntry.buildMovieReviewsUri(MAD_MAX_TMDB_ID),
                null, null, null, null);
        Log.d(LOG_TAG, DatabaseUtils.dumpCursorToString(retCursor));
        assertEquals("Number of row returned incorrect", 2, retCursor.getCount());

        db.close();
    }

    public void testInsertReadProvider() {
        ContentResolver cr = mContext.getContentResolver();
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        ContentValues testValues = TestUtilities.createMadmaxMovieValues();

        // Register a content observer before insert via ContentResolver to test
        cr.registerContentObserver(MovieEntry.CONTENT_URI, true, tco);
        Uri movieUri = cr.insert(MovieEntry.CONTENT_URI, testValues);

        // Did our content observer get called? If this fails, your insert movie
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        cr.unregisterContentObserver(tco);

        // Verify we got a row back.
        long movieRowId = ContentUris.parseId(movieUri);
        assertTrue(movieRowId != -1);

        // Data's inserted. Now pull some out to stare at it and verify it made the round trip.
        Cursor retCursor = cr.query(MovieEntry.CONTENT_URI, null, null, null, null);
        assertEquals("Number of row returned incorrect", 1, retCursor.getCount());
        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry.",
                retCursor, testValues);
//
//        // Fantastic.  Now that we have a location, add some weather!
//        ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowId);
//        // The TestContentObserver is a one-shot class
//        tco = TestUtilities.getTestContentObserver();
//
//        cr.registerContentObserver(WeatherEntry.CONTENT_URI, true, tco);
//
//        Uri weatherInsertUri = cr
//                .insert(WeatherEntry.CONTENT_URI, weatherValues);
//        assertTrue(weatherInsertUri != null);
//
//        // Did our content observer get called?  Students:  If this fails, your insert weather
//        // in your ContentProvider isn't calling
//        // getContext().getContentResolver().notifyChange(uri, null);
//        tco.waitForNotificationOrFail();
//        cr.unregisterContentObserver(tco);
//
//        // A cursor is your primary interface to the query results.
//        Cursor weatherCursor = cr.query(
//                WeatherEntry.CONTENT_URI,  // Table to Query
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null // columns to group by
//        );
//
//        TestUtilities.validateCursor("testInsertReadProvider. Error validating WeatherEntry insert.",
//                weatherCursor, weatherValues);
//
//        // Add the location values in with the weather data so that we can make
//        // sure that the join worked and we actually get all the values back
//        weatherValues.putAll(testValues);
//
//        // Get the joined Weather and Location data
//        weatherCursor = cr.query(
//                WeatherEntry.buildWeatherLocation(TestUtilities.TEST_LOCATION),
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null  // sort order
//        );
//        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Weather and Location Data.",
//                weatherCursor, weatherValues);
//
//        // Get the joined Weather and Location data with a start date
//        weatherCursor = cr.query(
//                WeatherEntry.buildWeatherLocationWithStartDate(
//                        TestUtilities.TEST_LOCATION, TestUtilities.TEST_DATE),
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null  // sort order
//        );
//        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Weather and Location Data with start date.",
//                weatherCursor, weatherValues);
//
//        // Get the joined Weather data for a specific date
//        weatherCursor = cr.query(
//                WeatherEntry.buildWeatherLocationWithDate(TestUtilities.TEST_LOCATION, TestUtilities.TEST_DATE),
//                null,
//                null,
//                null,
//                null
//        );
//        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Weather and Location data for a specific date.",
//                weatherCursor, weatherValues);
    }

    // TODO: To which Test functions should this code be in ?
    // Has the NotificationUri been set correctly? --- we can only test this easily against API
    // level 19 or greater because getNotificationUri was added in API level 19.
//    if ( Build.VERSION.SDK_INT >= 19 ) {
//        assertEquals("Error: Location Query did not properly set NotificationUri",
//                locationCursor.getNotificationUri(), LocationEntry.CONTENT_URI);
//    }
}