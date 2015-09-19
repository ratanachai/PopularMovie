package com.ratanachai.popularmovies.data;
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    void deleteTheDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.
        This makes sure that we always have a clean test. */
    public void setUp() {
        // Since we want each test to start with a clean slate
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {

        // Build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.VideoEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.ReviewEntry.TABLE_NAME);

        // TEST Database creation
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MovieDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // TEST Tables creation (All 3 tables + sqlite_master table
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: The database has not been created correctly", c.moveToFirst());
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );
        assertTrue("Error: Your database was created with all 3 tables", tableNameHashSet.isEmpty());

        // TEST if our tables contain the correct columns?

        // Movie Table ---------------------
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")", null);
        assertTrue("Error: Unable to query the database for table information.", c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieColumnHashSet = new HashSet<String>();
        movieColumnHashSet.add(MovieContract.MovieEntry._ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_TMDB_MOVIE_ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_TITLE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_OVERVIEW);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_USER_RATING);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_POSTER_PATH);

        do {
            String columnName = c.getString(c.getColumnIndex("name"));
            movieColumnHashSet.remove(columnName);
        } while(c.moveToNext());
        assertTrue("Error: The database doesn't contain all of the required columns", movieColumnHashSet.isEmpty());

        // Video Table ---------------------
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.VideoEntry.TABLE_NAME + ")", null);
        assertTrue("Error: Unable to query the database for table information.", c.moveToFirst());
        final HashSet<String> videoColumnHashSet = new HashSet<String>();
        videoColumnHashSet.add(MovieContract.VideoEntry._ID);
        videoColumnHashSet.add(MovieContract.VideoEntry.COLUMN_MOV_KEY);
        videoColumnHashSet.add(MovieContract.VideoEntry.COLUMN_KEY);
        videoColumnHashSet.add(MovieContract.VideoEntry.COLUMN_NAME);
        videoColumnHashSet.add(MovieContract.VideoEntry.COLUMN_TYPE);
        videoColumnHashSet.add(MovieContract.VideoEntry.COLUMN_SITE);
        do {
            String columnName = c.getString(c.getColumnIndex("name"));
            videoColumnHashSet.remove(columnName);
        } while(c.moveToNext());
        assertTrue("Error: The database doesn't contain all of the required columns", videoColumnHashSet.isEmpty());

        // Review Table ---------------------
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.ReviewEntry.TABLE_NAME + ")", null);
        assertTrue("Error: Unable to query the database for table information.", c.moveToFirst());
        final HashSet<String> reviewColumnHashSet = new HashSet<String>();
        reviewColumnHashSet.add(MovieContract.ReviewEntry._ID);
        reviewColumnHashSet.add(MovieContract.ReviewEntry.COLUMN_MOV_KEY);
        reviewColumnHashSet.add(MovieContract.ReviewEntry.COLUMN_TMDB_REVIEW_ID);
        reviewColumnHashSet.add(MovieContract.ReviewEntry.COLUMN_AUTHOR);
        reviewColumnHashSet.add(MovieContract.ReviewEntry.COLUMN_CONTENT);
        reviewColumnHashSet.add(MovieContract.ReviewEntry.COLUMN_URL);

        do {
            String columnName = c.getString(c.getColumnIndex("name"));
            reviewColumnHashSet.remove(columnName);
        } while(c.moveToNext());
        assertTrue("Error: The database doesn't contain all of the required columns", reviewColumnHashSet.isEmpty());

        db.close();
    }

    /*
        TEST: Each table (Create and Read)
     */
    public void testMovieTable(){
        insertMovie();
    }
//    public void testVideoTable(){
//
//    }
//    public void testReviewTable(){
//
//    }
    public long insertMovie(){

        // Create DB with Table via DbHealper's onCreate, then getWritableDatabase
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // TEST: insert
        ContentValues testValues = TestUtilities.createMadmaxMovieValues();
        long movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);
        assertTrue("Error: Did not get a row back after insert", movieRowId != -1);

        // TEST: read and validate data
        Cursor cursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                null, null, null, null, null, null);
        assertTrue("Error: No Records returned from movie query", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord("Error: Movie record validation Failed", cursor, testValues);
        // Check that there's only one record
        assertFalse("Error: More than one record returned from Movie query",
                cursor.moveToNext());

        // Clean things up
        cursor.close();
        db.close();
        return movieRowId;
    }

    /** ============== */


}