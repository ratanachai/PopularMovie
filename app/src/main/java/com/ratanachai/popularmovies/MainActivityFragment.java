package com.ratanachai.popularmovies;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.ratanachai.popularmovies.data.MovieContract.MovieEntry;
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

public class MainActivityFragment extends BaseFragment {
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private CustomImageAdapter mMovieAdapter; // Adapter for Grid of Poster Image
    private ArrayList<String> mMoviePosterPaths; // URLs used to populate the grid
    private ArrayList<Movie> mMovies = new ArrayList<>(); // of Movie objects
    private String mFetchedSortBy = ""; // Sort Mode that has been Fetched
    private ProgressDialog mProgress;

    /** A callback interface that all activities containing this fragment must implement.
     *  This mechanism allows activities to be notified of item selections.
     *  DetailFragmentCallback for when an item has been selected. */
    public interface Callback {
        void onItemSelected(String[] movieInfo);
    }

    public MainActivityFragment() {}

    public void updateMoviesGrid() {
        getMovies();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "== onCreate()");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); //For this fragment to handle menu events.

        // Create MovieAdapter every times
        mMoviePosterPaths = new ArrayList<>();
        mMovieAdapter = new CustomImageAdapter(mMoviePosterPaths);

        // Fetch Movies or Restore from savedInstanceState
        // http://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate
        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            getMovies();
        }else {
            mFetchedSortBy = savedInstanceState.getString("sort_mode");
            mMovies = savedInstanceState.getParcelableArrayList("movies");
            populatePoster();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "== onCreateView()");
        final Context c = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        /** Set Number of Columns in different devices **
         - Default Portrait = 2 columns; Default Land = 5 columns;
         - Small Tablet (Part and Land) = 2 columns; (sw600dp-land = 2)
         - Large Tablet Port = 2 columns; Large Tab Land = 3 columns (sw800dp-land = 3) */
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setNumColumns(c.getResources().getInteger(R.integer.num_columns));

        // Set EmptyView, Adapter, OnItemClickListener
        View emptyView = rootView.findViewById(R.id.gridview_movies_empty);
        gridView.setEmptyView(emptyView);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((Callback) c).onItemSelected(mMovies.get(position).getAll()); }
        });

        return rootView;
    }
    @Override
    public void onStart(){
        super.onStart();
        Log.d(LOG_TAG, "== onStart()");

        String newSortMode = getPrefSortBy(getActivity());
        getActivity().setTitle(getString(R.string.app_name) + " - " + getCurrentSortByLabel(newSortMode));

        // Force Re-Fetch If needReFetch OR Sort Criteria changed (New differs from fetched)
        if( needReFetch || (!mFetchedSortBy.isEmpty() && newSortMode != null && !newSortMode.equals(mFetchedSortBy)) )
            getMovies();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("movies", mMovies);
        outState.putString("sort_mode", mFetchedSortBy);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_fragment, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            getMovies(); // Force Fetch Movies data
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getMovies(){
        Log.d(LOG_TAG, "== fetchMovieInfo()");
        String sortBy = getPrefSortBy(getActivity());

        if(isSortByFavorite(sortBy)) {
            Log.d(LOG_TAG, "== Getting Favorite Movies from DB");

            // Populate mMovies from DB:
            // Get Movie from Database, then Put into ArrayList of Movies
            mMovies.clear(); // Must clear the list before adding new
            Cursor cur = getActivity().getContentResolver().query(MovieEntry.CONTENT_URI,
                    Movie.MOVIE_COLUMNS, null, null, MovieEntry._ID + " DESC");
            while(cur.moveToNext()) {
                Movie movieObj = new Movie(cur.getString(Movie.COL_TMDB_MOVIE_ID),
                        cur.getString(Movie.COL_TITLE), cur.getString(Movie.COL_POSTER_PATH),
                        cur.getString(Movie.COL_OVERVIEW), cur.getString(Movie.COL_USER_RATING),
                        cur.getString(Movie.COL_RELEASE_DATE));
                mMovies.add(movieObj);
            }
            cur.close();
            populatePoster(); // Populate Grid of Posters
            needReFetch = false; //Reset flag after fetched

        }else if(Utility.isNetworkAvailable(getActivity())) {
            Log.d(LOG_TAG, "== Getting Movies from the Internet");

            mProgress = new ProgressDialog(getActivity());
            mProgress.setMessage("Downloading from TMDB");
            mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgress.show();

            // Get Movie from Internet
            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
            fetchMoviesTask.execute(sortBy);

        }else{
            Toast toast = Toast.makeText(getActivity(), "No network connection. " +
                    "Please Switch to Favorite Movie list.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            mMovies.clear();
            mMoviePosterPaths.clear();
            mMovieAdapter.notifyDataSetChanged();
        }

        mFetchedSortBy = sortBy; //Update FetchedSortBy
    }

    private void populatePoster() {
        mMoviePosterPaths.clear();
        for(Movie aMovie : mMovies) {
            mMoviePosterPaths.add(aMovie.getPosterPath());
        }
        mMovieAdapter.notifyDataSetChanged();
    }

    // How-to use Picasso with ArrayAdapter from Big Nerd Ranch
    // https://www.bignerdranch.com/blog/solving-the-android-image-loading-problem-volley-vs-picasso/
    private class CustomImageAdapter extends ArrayAdapter<String> {

        // Comment out unnecessary Override
//        private ArrayList<String> items;
//
        public CustomImageAdapter(ArrayList<String> urls) {
            super(getActivity(), 0, urls);
//            this.items = urls;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.grid_item_movie, parent, false);

            // Adjust its bound to max while Preserve the aspect ratio of Image
            ImageView imageView = (ImageView)convertView;
            imageView.setAdjustViewBounds(true);

            // Download Image from TMDB using mMoviePosterPath
            Picasso.with(getActivity())
                    .load("http://image.tmdb.org/t/p/w185" + getItem(position))
                    .into(imageView);

            return convertView;
        }
//        @Override
//        public int getCount() { return items.size();}
//        @Override
//        public String getItem(int position){ return items.get(position); }
//
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        /** Takes the JSON string and converts it into an Object hierarchy. */
        private ArrayList<Movie> getMoviesFromJson(String moviesJsonStr) throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_MOVIES = "results";
            final String TMDB_MOVIE_ID = "id";
            final String TMDB_ORIGINAL_TITLE = "original_title";
            final String TMDB_POSTER_PATH = "poster_path";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_USER_RATING = "vote_average";
            final String TMDB_RELEASE = "release_date";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_MOVIES);

            // Populate mMovies from JSON:
            // Create a Movie object then add to ArrayList of Movies
            mMovies.clear(); // Must clear the list before adding new
            for(int i = 0; i < moviesArray.length(); i++){
                JSONObject aMovie = moviesArray.getJSONObject(i);
                Movie movieObj = new Movie(aMovie.getString(TMDB_MOVIE_ID), aMovie.getString(TMDB_ORIGINAL_TITLE),
                        aMovie.getString(TMDB_POSTER_PATH), aMovie.getString(TMDB_OVERVIEW),
                        aMovie.getString(TMDB_USER_RATING), aMovie.getString(TMDB_RELEASE));
                mMovies.add(movieObj);
            }
            return mMovies;
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;
            String sort_by = params[0] +".desc";
            String api_key = getString(R.string.api_key);

            try {
                // Construct the URL for TMDB query
                // http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=<api_key>
                final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, sort_by)
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
                moviesJsonStr = buffer.toString();

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
                return getMoviesFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null; // This will only happen if there was an error getting or parsing
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            if (movies != null) {
                populatePoster();
                mProgress.hide();
            }
        }
    }
}
