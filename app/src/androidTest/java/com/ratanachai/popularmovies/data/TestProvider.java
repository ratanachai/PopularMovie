package com.ratanachai.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.test.AndroidTestCase;

import com.ratanachai.popularmovies.data.MovieContract.MovieEntry;
/**
 * Tests for MovieProvider
 * Created by Ratanachai on 15/09/22.
 */
public class TestProvider extends AndroidTestCase {

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
        // content://com.ratanachai.popularmovies/movie/
        // vnd.android.cursor.dir/com.ratanachai.popularmovies/movie/
        ContentResolver contentResolver = mContext.getContentResolver();
        String type = contentResolver.getType(MovieEntry.buildMoviesUri());
        assertEquals("Error: MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MovieEntry.CONTENT_TYPE, type);

        type = contentResolver.getType(MovieEntry.buildMovieUri(TEST_MOVIE_ID));
        assertEquals("Error: MovieEntry CONTENT_URI with ID should return MovieEntry.CONTENT_ITEM_TYPE",
                MovieEntry.CONTENT_ITEM_TYPE, type);


//        String testLocation = "94074";
//        // content://com.example.android.sunshine.app/weather/94074
//        type = contentResolver.getType(
//                MovieEntry.buildWeatherLocation(testLocation));
//        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
//        assertEquals("Error: the MovieEntry CONTENT_URI with location should return MovieEntry.CONTENT_TYPE",
//                MovieEntry.CONTENT_TYPE, type);
//
//        long testDate = 1419120000L; // December 21st, 2014
//        // content://com.example.android.sunshine.app/weather/94074/20140612
//        type = contentResolver.getType(
//                MovieEntry.buildWeatherLocationWithDate(testLocation, testDate));
//        // vnd.android.cursor.item/com.example.android.sunshine.app/weather/1419120000
//        assertEquals("Error: the MovieEntry CONTENT_URI with location and date should return MovieEntry.CONTENT_ITEM_TYPE",
//                MovieEntry.CONTENT_ITEM_TYPE, type);
//
//        // content://com.example.android.sunshine.app/location/
//        type = contentResolver.getType(LocationEntry.CONTENT_URI);
//        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
//        assertEquals("Error: the LocationEntry CONTENT_URI should return LocationEntry.CONTENT_TYPE",
//                LocationEntry.CONTENT_TYPE, type);
    }

}