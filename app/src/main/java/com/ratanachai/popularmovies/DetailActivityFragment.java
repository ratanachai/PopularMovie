package com.ratanachai.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ratanachai.popularmovies.data.MovieContract.MovieEntry;
import com.ratanachai.popularmovies.data.MovieContract.ReviewEntry;
import com.ratanachai.popularmovies.data.MovieContract.VideoEntry;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DetailActivityFragment extends BaseFragment {

    public static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    static final String MOVIE_INFO = "MOVIE_INFO";
    private String[] mMovieInfo;
    private ArrayList<Video> mVideos = new ArrayList<>();
    private ArrayList<Review> mReviews = new ArrayList<>();
    private View mRootview;
    private boolean mAddVideosAndReviews = false;
    private ShareActionProvider mShareActionProvider;
//    private Typeface lobster;

    public interface Callback {
        // All activity that contain this fragment must implement this Callback
        // (MainActivity for tablet and DetailActivity for phone)
        void onAddRemoveMovieFromFavorite(boolean needReFetch);
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        lobster = Typeface.createFromAsset(getActivity().getAssets(), "Lobster-Regular.ttf");
//    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.v(LOG_TAG, "=== onCreate");
        super.onCreate(savedInstanceState);
        mSortBy = getSortBy();

        // Get data passed from Activity
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovieInfo = arguments.getStringArray(MOVIE_INFO);

            /** Fetch if no savedInstance at all, or for both video and review */
            // http://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate
            if (savedInstanceState == null
                    || !savedInstanceState.containsKey("videos")
                    || !savedInstanceState.containsKey("reviews")) {
                String movie_id = mMovieInfo[0];
                // Fetch from the internet for DB in offline mode
                getVideosFromInternetOrDb(movie_id);
                getReviewsFromInternetOrDb(movie_id);
            } else {
                // Restore from savedInstanceState
                mVideos = savedInstanceState.getParcelableArrayList("videos");
                mReviews = savedInstanceState.getParcelableArrayList("reviews");
                mAddVideosAndReviews = true;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if(!mVideos.isEmpty()) outState.putParcelableArrayList("videos", mVideos);
        if(!mReviews.isEmpty()) outState.putParcelableArrayList("reviews", mReviews);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(LOG_TAG, "=== onCreateView");
        setHasOptionsMenu(true);
        mRootview = inflater.inflate(R.layout.fragment_detail, container, false);

        if (getArguments() == null) return mRootview; //Early Exit

        // Set all TextView and Poster
        TextView movieTitleTv = (TextView) mRootview.findViewById(R.id.movie_title);
        // movieTitleTv.setTypeface(lobster);
        getActivity().setTitle(mMovieInfo[1]);
        movieTitleTv.setText(mMovieInfo[1]);
        ((TextView) mRootview.findViewById(R.id.movie_overview)).setText(mMovieInfo[3]);
        ((TextView) mRootview.findViewById(R.id.movie_rating)).append(" " + mMovieInfo[4] + "/10");
        ((TextView) mRootview.findViewById(R.id.movie_release)).append(" " + mMovieInfo[5]);
        Picasso.with(getActivity())
                .load("http://image.tmdb.org/t/p/w185" + mMovieInfo[2])
                .fit().centerInside()
                .into((ImageView) mRootview.findViewById(R.id.movie_poster));

        // Set Listener: Add/Remove TMDB_MOV_ID on checked/unchecked
        ToggleButton favToggle = (ToggleButton) mRootview.findViewById(R.id.favorite_toggle);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String key = getString(R.string.pref_movie_ids_key);
        final Set<String> outSet = prefs.getStringSet(key, new HashSet<String>());
        final String tmdb_id = mMovieInfo[0];

        // Toggle ON if current movie is in the Favorite Movie Set
        if(outSet.contains(tmdb_id)) favToggle.setChecked(true);

        // Set OnCheckChanged
        favToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                SharedPreferences.Editor editor = prefs.edit();
                Set<String> fav_movie_ids = new HashSet<String>(outSet);
                // Add/Remove to SharedPref and Database
                if (isChecked) {
                    fav_movie_ids.add(tmdb_id);
                    // Save the Movie, its Videos and Reviews
                    long movieRowId = saveMovieOffline(mMovieInfo);

                    // In case of Favorite Movie Criteria ..
                    // MainFragment will need to refetch movies if movie added
                    if ( isSortByFavorite(mSortBy) )
                        ((Callback) getActivity()).onAddRemoveMovieFromFavorite(false);

                    saveVideosOffline(movieRowId);
                    saveReviewOffline(movieRowId);
                    if ( isSortByFavorite(mSortBy) )
                        needReFetch = false;
                }
                else{
                    fav_movie_ids.remove(tmdb_id);
                    // Delete the Movie, its videos will be DELETE CASCADE via Foreign key constrain
                    removeOfflineMovie(tmdb_id);

                    // In case of Favorite Movie Criteria ..
                    // MainFragment will need to refetch movies if movie removed
                    if ( isSortByFavorite(mSortBy) )
                        ((Callback) getActivity()).onAddRemoveMovieFromFavorite(true);
                }
                // Save new Set into SharedPref
                editor.putStringSet(key, fav_movie_ids);
                editor.commit();

                // Pull out from SharedPref again to check
                Log.d(LOG_TAG + "==After==", prefs.getStringSet(key, new HashSet<String>()).toString());
            }
        });
        // Restore Trailer Videos and Reviews (First time added via OnPostExecute)
        if (mAddVideosAndReviews) {
            addVideosTextView(mVideos);
            addReviewsTextView(mReviews);
        }
        return mRootview;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail_fragment, menu);

        /** Based on Android ActionBarCompat-ShareActionProvider Sample
           https://github.com/googlesamples/android-ActionBarCompat-ShareActionProvider */
        // Get ActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        // Set Share Intent with/without Video URL depend whether Video fetching onPostExec() has finished
        if (mVideos.isEmpty()) {
            mShareActionProvider.setShareIntent(createShareVideoLinkIntent(""));
            Log.v(LOG_TAG, "=== onCreateOptionsMenu() Set intent with EMPTY STRING");
        }else {
            mShareActionProvider.setShareIntent(createShareVideoLinkIntent(mVideos.get(0).getYoutubeUrl()));
            Log.v(LOG_TAG, "=== onCreateOptionsMenu() Set intent with " + mVideos.get(0).getYoutubeUrl());
        }
    }
    private Intent createShareVideoLinkIntent(String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        TextView tv = (TextView)getActivity().findViewById(R.id.movie_title);
        String movieTitle = (String)tv.getText();

        String text = "Please select a movie before you share!";
        if (mMovieInfo != null)
            text = "Have you seen this movie, " + movieTitle + "?\n" + url;
        intent.putExtra(Intent.EXTRA_TEXT, text);

        return intent;
    }
    /** SAVE MOVIE into Favorite List */
    long saveMovieOffline(String[] movieInfo){

        // QUERY to check before INSERT
        long rowId;
        ContentResolver cr = getActivity().getContentResolver();
        Cursor movieCursor = cr.query(MovieEntry.CONTENT_URI, Movie.MOVIE_COLUMNS,
                MovieEntry.COLUMN_TMDB_MOVIE_ID + " = ? ", new String[]{movieInfo[0]}, null);
        if (movieCursor.getCount() == 0) {

            // Prepare ContentValues, then INSERT
            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieEntry.COLUMN_TMDB_MOVIE_ID, movieInfo[0]);
            movieValues.put(MovieEntry.COLUMN_TITLE, movieInfo[1]);
            movieValues.put(MovieEntry.COLUMN_POSTER_PATH, movieInfo[2]);
            movieValues.put(MovieEntry.COLUMN_OVERVIEW, movieInfo[3]);
            movieValues.put(MovieEntry.COLUMN_USER_RATING, movieInfo[4]);
            movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, movieInfo[5]);
            Uri movieUri = cr.insert(MovieEntry.CONTENT_URI, movieValues);
            Toast.makeText(getActivity(), "The Movie is added to your Watchlist", Toast.LENGTH_SHORT).show();
            rowId = ContentUris.parseId(movieUri);

        }else{
            Log.v(LOG_TAG, "Movie with the same TMDB ID already saved");
            rowId = -1;
        }
        movieCursor.close();
        return rowId;
    }
    void saveVideosOffline(long movieRowId){
        ContentResolver cr = getActivity().getContentResolver();

        // FOR each Video...
        for (int i = 0; i < mVideos.size(); i++) {
            Video video = mVideos.get(i);

            // QUERY to check before INSERT
            Cursor videoCursor = cr.query(VideoEntry.CONTENT_URI, null,
                    VideoEntry.COLUMN_KEY + " = ? ", new String[]{video.getKey()}, null);
            Log.v(LOG_TAG, "== getCount() = " + Integer.toString(videoCursor.getCount()));
            if (videoCursor.getCount() == 0) {

                // Prepare ContentValues, then INSERT
                ContentValues videoValues = new ContentValues();
                videoValues.put(VideoEntry.COLUMN_MOV_KEY, movieRowId);
                videoValues.put(VideoEntry.COLUMN_KEY, video.getKey());
                videoValues.put(VideoEntry.COLUMN_NAME, video.getName());
                videoValues.put(VideoEntry.COLUMN_TYPE, video.getType());
                videoValues.put(VideoEntry.COLUMN_SITE, video.getSite());
                cr.insert(VideoEntry.CONTENT_URI, videoValues);
                Log.v(LOG_TAG, "== a Video inserted into DB");
            } else {
                Log.v(LOG_TAG, "== a Video with the same Key is already in DB");
            }
            videoCursor.close();
        }
    }
    void saveReviewOffline(long movieRowId){
        ContentResolver cr = getActivity().getContentResolver();

        for (int i = 0; i < mReviews.size(); i++ ){
            Review review = mReviews.get(i);
            // Query to check before insert
            Cursor cur = cr.query(ReviewEntry.CONTENT_URI, null,
                    ReviewEntry.COLUMN_TMDB_REVIEW_ID + " = ?", new String[]{review.getId()}, null);
            if (cur.getCount() == 0){
                ContentValues reviewValues = new ContentValues();
                reviewValues.put(ReviewEntry.COLUMN_MOV_KEY, movieRowId);
                reviewValues.put(ReviewEntry.COLUMN_TMDB_REVIEW_ID, review.getId());
                reviewValues.put(ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
                reviewValues.put(ReviewEntry.COLUMN_CONTENT, review.getContent());
                reviewValues.put(ReviewEntry.COLUMN_URL, review.getUrl());
                cr.insert(ReviewEntry.CONTENT_URI, reviewValues);
                Log.v(LOG_TAG, "== a Review inserted into DB");
            } else {
                Log.v(LOG_TAG, "== a Review with the same Review ID is already in DB");
            }
            cur.close();
        }
    }
    // This will remove not just Movie, but its associated videos and reviews too.
    void removeOfflineMovie(String tmdbMovieId){
        ContentResolver cr = getActivity().getContentResolver();
        int rowsDeleted = cr.delete(MovieEntry.CONTENT_URI,
                MovieEntry.COLUMN_TMDB_MOVIE_ID + " = ?", new String[]{tmdbMovieId});

        if (rowsDeleted != 0)
            Toast.makeText(getActivity(), "Movie is removed from Offline view", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getActivity(), "No movie removed from Offline view", Toast.LENGTH_SHORT).show();
    }

    private long getMovieRowId(String tmdb_movie_id){
        ContentResolver cr = getActivity().getContentResolver();
        Cursor movieCursor = cr.query(MovieEntry.CONTENT_URI, Movie.MOVIE_COLUMNS,
                MovieEntry.COLUMN_TMDB_MOVIE_ID + " = ? ", new String[]{tmdb_movie_id}, null);
        movieCursor.moveToNext();
        long movieRowId = movieCursor.getLong(Movie.COL_MOVIE_ROW_ID);
        movieCursor.close();
        return movieRowId;
    }
    /** Code for Movie Video (Trailer) ---------------------------------------------------------- */
    private void getVideosFromInternetOrDb(String tmdb_movie_id){

        // Get Videos from Database
        if(isSortByFavorite(mSortBy)) {
            ContentResolver cr = getActivity().getContentResolver();
            long movieRowId = getMovieRowId(tmdb_movie_id);

            // Query Video for the movie, then Populate ArrayList of Videos
            Cursor cur  = cr.query(VideoEntry.buildMovieVideosUri(movieRowId), Video.VIDEO_COLUMNS,
                    null, null , null);
            mVideos.clear();
            while (cur.moveToNext()){
                Video videoObj = new Video(cur.getString(Video.COL_KEY), cur.getString(Video.COL_NAME),
                        cur.getString(Video.COL_TYPE), cur.getString(Video.COL_SITE));
                mVideos.add(videoObj);
            }
            mAddVideosAndReviews = true;

        // Fetch videos from the Internet in background
        }else if(Utility.isNetworkAvailable(getActivity())) {

            FetchVideosTask fetchVideosTask = new FetchVideosTask();
            fetchVideosTask.execute(tmdb_movie_id);

        }else{
            Log.v(LOG_TAG, "== No Network, Not in offline mode: will not try to fetch videos");
        }
    }
    private void addVideosTextView(ArrayList<Video> videos) {

        ViewGroup containerView = (ViewGroup) mRootview.findViewById(R.id.movie_trailers_container);
        for (int i=0; i < videos.size(); i++) {

            // Get TextView from Item layout
            View v = getLayoutInflater(null).inflate(R.layout.video_link_item, null);
            TextView videoTextView = (TextView) v.findViewById(R.id.movie_trailer_item);

            // Set text and tag on TextView (Tag to be used in onClick
            videoTextView.setText(videos.get(i).getName());
            videoTextView.setTag(videos.get(i).getKey());

            // Setup OnItemClick to launch Youtube App with the video
            videoTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "vnd.youtube:" + v.getTag()));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "http://www.youtube.com/watch?v=" + v.getTag()));
                        startActivity(intent);
                    }
                }
            });
            // Add the Video TextView into DetailActivityFragment
            containerView.addView(videoTextView);
        }
    }

    public class FetchVideosTask extends AsyncTask<String, Void, ArrayList<Video>> {

        private final String LOG_TAG = FetchVideosTask.class.getSimpleName();

        /** Takes the JSON string and converts it into an Object hierarchy. */
        private ArrayList<Video> getVideosFromJson(String videosJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_VIDEOS = "results";
            final String TMDB_VIDEO_KEY = "key";
            final String TMDB_VIDEO_NAME = "name";
            final String TMDB_VIDEO_SITE = "site";
            final String TMDB_VIDEO_TYPE = "type";

            JSONObject videosJson = new JSONObject(videosJsonStr);
            JSONArray videosArray = videosJson.getJSONArray(TMDB_VIDEOS);

            // Create Video objects and put them into ArrayList
            mVideos.clear(); // Must clear the list before adding new
            for(int i = 0; i < videosArray.length(); i++){
                JSONObject aVideo = videosArray.getJSONObject(i);
                Video videoObj = new Video(aVideo.getString(TMDB_VIDEO_KEY),
                                            aVideo.getString(TMDB_VIDEO_NAME),
                                            aVideo.getString(TMDB_VIDEO_TYPE),
                                            aVideo.getString(TMDB_VIDEO_SITE));
                mVideos.add(videoObj);
            }

            return mVideos;
        }

        @Override
        protected ArrayList<Video> doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String videosJsonStr = null;
            String movie_id = params[0];
            String api_key = getString(R.string.api_key);

            try {
                // Construct the URL for TMDB query
                // http://api.themoviedb.org/3/movie/15121/videos?api_key=<api_key>
                final String BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(movie_id)
                        .appendPath("videos")
                        .appendQueryParameter(API_KEY_PARAM, api_key)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to TMDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                videosJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getVideosFromJson(videosJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Video> videos) {
            if (videos != null) {
                addVideosTextView(videos);

                // Update ShareIntent IF onCreateOptionsMenu already happened and there's some videos
                if (mShareActionProvider != null & !mVideos.isEmpty()) {
                    Log.v(LOG_TAG, "=== onPostExec() Updating intent with " + videos.get(0).getYoutubeUrl());
                    mShareActionProvider.setShareIntent(createShareVideoLinkIntent(videos.get(0).getYoutubeUrl()));
                }
            }
        }
    }

    /** Code for Movie Review ------------------------------------------------------------------- */
    private void getReviewsFromInternetOrDb(String tmdb_movie_id){

        // Get from Database
        if(isSortByFavorite(mSortBy)) {
            ContentResolver cr = getActivity().getContentResolver();
            long movieRowId = getMovieRowId(tmdb_movie_id);

            // Query Reviews for the movie, then Populate ArrayList of Reviews
            Cursor cur  = cr.query(ReviewEntry.buildMovieReviewsUri(movieRowId), Review.REVIEW_COLUMNS,
                    null, null , null);
            mReviews.clear();
            while (cur.moveToNext()){
                Review reviewObj = new Review(cur.getString(Review.COL_TMDB_REVIEW_ID), cur.getString(
                        Review.COL_AUTHOR), cur.getString(Review.COL_CONTENT), cur.getString(Review.COL_URL));
                mReviews.add(reviewObj);
            }
            mAddVideosAndReviews = true;

        // Fetch videos from the Internet in background
        }else if(Utility.isNetworkAvailable(getActivity())) {

            FetchReviewsTask fetchReviewsTask = new FetchReviewsTask();
            fetchReviewsTask.execute(tmdb_movie_id);

        }else{
            Log.v(LOG_TAG, "== No Network, Not in offline mode: will not try to fetch reviews");
        }
    }

    private void addReviewsTextView(ArrayList<Review> reviews) {

        ViewGroup containerView = (ViewGroup) mRootview.findViewById(R.id.movie_reviews_container);
        for (int i=0; i < reviews.size(); i++) {

            View v = getLayoutInflater(null).inflate(R.layout.review_item, null);
            View rootView = v.getRootView();
            TextView reviewAuthor = (TextView) v.findViewById(R.id.movie_review_author);
            TextView reviewContent = (TextView) v.findViewById(R.id.movie_review_content);
            reviewAuthor.setText(reviews.get(i).getAuthor());
            reviewContent.setText(reviews.get(i).getContent());
            containerView.addView(rootView);

        }
    }
    public class FetchReviewsTask extends AsyncTask<String, Void, ArrayList<Review>> {

        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

        private ArrayList<Review> getReviewsFromJson(String reviewsJsonStr) throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_REVIEWS = "results";
            final String TMDB_REVIEW_ID = "id";
            final String TMDB_REVIEW_AUTHOR = "author";
            final String TMDB_REVIEW_CONTENT = "content";
            final String TMDB_REVIEW_URL = "url";

            JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
            JSONArray reviewsArray = reviewsJson.getJSONArray(TMDB_REVIEWS);

            // Create Review objects and put them into ArrayList
            mReviews.clear(); // Must clear the list before adding new
            for(int i = 0; i < reviewsArray.length(); i++){
                JSONObject aReview = reviewsArray.getJSONObject(i);
                Review reviewObj = new Review(aReview.getString(TMDB_REVIEW_ID),
                        aReview.getString(TMDB_REVIEW_AUTHOR),
                        aReview.getString(TMDB_REVIEW_CONTENT),
                        aReview.getString(TMDB_REVIEW_URL));
                mReviews.add(reviewObj);
            }

            return mReviews;
        }

        @Override
        protected ArrayList<Review> doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String reviewsJsonStr = null;
            String movie_id = params[0];
            String api_key = getString(R.string.api_key);

            try {
                // Construct the URL for TMDB query
                // http://api.themoviedb.org/3/movie/15121/reviews?api_key=<api_key>
                final String BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(movie_id).appendPath("reviews")
                        .appendQueryParameter(API_KEY_PARAM, api_key)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to TMDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                reviewsJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the data, no point in attempting to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getReviewsFromJson(reviewsJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Review> reviews) {
            if (reviews != null) {
                addReviewsTextView(reviews);
            }
        }

    }
}
