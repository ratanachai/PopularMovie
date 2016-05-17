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
    private int page = 1; // For Endless Scrolling

    /** A callback interface that all activities containing this fragment must implement.
     *  This mechanism allows activities to be notified of item selections. */
    public interface Callback {
        void onItemSelected(String[] movieInfo, String sortBy);
        void onMoviesReady(String[] movieInfo, String sortBy);
    }

    public void updateMoviesGrid() {
        getMoviesFromDb();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "== onCreate()");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); //For this fragment to handle menu events.
        mSortBy = getSortBy();

        // Create MovieAdapter every times
        mMoviePosterPaths = new ArrayList<>();
        mMovieAdapter = new CustomImageAdapter(mMoviePosterPaths);

        // Fetch Movies or Restore from savedInstanceState
        // http://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate
        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            getMovies(page); //Nothing to restore so getMovies
        }else {
            mFetchedSortBy = savedInstanceState.getString("sort_mode");
            mMovies = savedInstanceState.getParcelableArrayList("movies");
            populatePoster(mMovies);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "== onCreateView()");
        final Context c = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        /** Set Number of Columns in different devices **
         - Default Portrait = 2 columns; Default Land = 4 columns;
         - Small Tablet (Part and Land) = 2 columns; (sw600dp-land = 2)
         - Large Tablet Port = 2 columns; Large Tab Land = 3 columns (sw800dp-land = 3) */
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setNumColumns(c.getResources().getInteger(R.integer.num_columns));

        // Set EmptyView, Adapter, OnItemClickListener
        // TODO: How to deal with Flash of emptyView before poster is loaded.
//        View emptyView = rootView.findViewById(R.id.gridview_movies_empty);
//        gridView.setEmptyView(emptyView);
        gridView.setAdapter(mMovieAdapter);
        if( !mSortBy.equals("favorite") ) {
            gridView.setOnScrollListener(new MyScrollListener() {
                @Override
                public boolean onLoadMore(int page, int totalItemsCount) {
                    getMovies(page);
                    return true;
                }
            });
        }

        // See MainActivity's onItemSelected()
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((Callback) c).onItemSelected(mMovies.get(position).getAll(), mSortBy); }
        });

        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(LOG_TAG, "== onStart()");
//        getActivity().setTitle(getString(R.string.app_name) + " - " + getCurrentSortByLabel(newSortBy));
        getActivity().setTitle(getString(R.string.app_name));
        // Force Re-Fetch If needReFetch OR SortBy changed (New differs from fetched)
        mSortBy = getSortBy();
        if( needReFetch || hasSortByChanged(mSortBy) ){
            mMovies.clear();
            mMoviePosterPaths.clear();
            mMovieAdapter.notifyDataSetChanged();
            getMovies(page);
        }
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if(mProgress!= null)
//            mProgress.dismiss();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if(mProgress!= null) {
//            mProgress.dismiss();
//            mProgress = null;
//        }
//    }

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
            getMovies(page); // Force Fetch Movies data
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean hasSortByChanged(String newSortBy){
        return !mFetchedSortBy.isEmpty() && newSortBy != null && !newSortBy.equals(mFetchedSortBy);
    }

    private void getMovies(int page){

        if(isSortByFavorite(mSortBy)) {
            Log.d(LOG_TAG, "== Getting Favorite Movies from DB");
            getMoviesFromDb();

        }else if(Utility.isNetworkAvailable(getActivity())) {
            Log.d(LOG_TAG, "== Getting Movies from the Internet");
            getMoviesFromInternet(mSortBy, page);

        }else{
            Toast toast = Toast.makeText(getActivity(), "No network connection. " +
                    "Please Switch to your Watchlist.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            mMovies.clear();
            mMoviePosterPaths.clear();
            mMovieAdapter.notifyDataSetChanged();
        }

        mFetchedSortBy = mSortBy; //Update FetchedSortBy
    }

    private void getMoviesFromDb() {
        // Clear All
        mMovies.clear();
        mMoviePosterPaths.clear();

        // Populate mMovies from DB: Get Movie from Database, then Put into ArrayList of Movies
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
        populatePoster(mMovies); // Populate Grid of Posters
        needReFetch = false; //Reset flag after fetched

        // Auto load the first movie into Tablet right pane
        if(mMovies.size() > 0)
            ((Callback) getActivity()).onMoviesReady(mMovies.get(0).getAll(), mSortBy);

    }

    private void getMoviesFromInternet(String sortBy, int page) {
        mProgress = new ProgressDialog(getActivity());
        mProgress.setMessage("Downloading from TMDB");
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgress.show();
        // TODO: Fix "E/WindowManager: android.view.WindowLeaked" when rotatage screen after 1st start

        // Get Movie from Internet
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        fetchMoviesTask.execute(sortBy, String.valueOf(page));
    }

    private void populatePoster(ArrayList<Movie> movies) {
        for(Movie aMovie : movies) {
            mMoviePosterPaths.add(aMovie.getPosterPath());
        }
        mMovieAdapter.notifyDataSetChanged();
    }

    /** How-to use Picasso with ArrayAdapter from Big Nerd Ranch
        https://www.bignerdranch.com/blog/solving-the-android-image-loading-problem-volley-vs-picasso/
     */
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
            ImageView imageView = (ImageView)convertView.findViewById(R.id.grid_item_movie);
            imageView.setAdjustViewBounds(true);

            // Download Image from TMDB using mMoviePosterPath (Width: 92, 154, 185, 342, 500, 780)
            Picasso.with(getActivity())
                    .load("http://image.tmdb.org/t/p/w342" + getItem(position))
                    .placeholder(R.drawable.film)
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
//            mMovies.clear(); // Must clear the list before adding new
            ArrayList<Movie> movies = new ArrayList<>();
            for(int i = 0; i < moviesArray.length(); i++){
                JSONObject aMovie = moviesArray.getJSONObject(i);
                Movie movieObj = new Movie(aMovie.getString(TMDB_MOVIE_ID), aMovie.getString(TMDB_ORIGINAL_TITLE),
                        aMovie.getString(TMDB_POSTER_PATH), aMovie.getString(TMDB_OVERVIEW),
                        aMovie.getString(TMDB_USER_RATING), aMovie.getString(TMDB_RELEASE));
                movies.add(movieObj);
            }
            mMovies.addAll(movies);
            return movies;
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;
            String api_key = getString(R.string.api_key);

            try {
                // Construct the URL for TMDB query
                // http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=<api_key>
                final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String PAGE = "page";
                final String SORT_PARAM = "sort_by";
                final String VOTE_COUNT_THREASHOLD = "vote_count.gte";
                final String API_KEY_PARAM = "api_key";


                Uri.Builder ub = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(PAGE, params[1])
                        .appendQueryParameter(SORT_PARAM, params[0]+".desc")
                        .appendQueryParameter(API_KEY_PARAM, api_key);

                if (params[0].equals(getString(R.string.pref_sort_rating)))
                    ub.appendQueryParameter(VOTE_COUNT_THREASHOLD, "500");

                Uri builtUri = ub.build();
                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Fetch: "+builtUri.toString());
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
                populatePoster(movies);
                mProgress.hide();
                // Auto load the first movie into Tablet right pane
                if(mMovies.size() > 0)
                    ((Callback) getActivity()).onMoviesReady(mMovies.get(0).getAll(), mSortBy);
            }
        }
    }
}
