package com.ratanachai.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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

public class MainActivityFragment extends Fragment {
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private CustomImageAdapter mMovieAdapter;
    private ArrayList<Movie> mMovies = new ArrayList<Movie>();
    private String mSortMode = "";

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "== onCreate()");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); //For this fragment to handle menu events.

        // Create MovieAdapter every times
        mMovieAdapter = new CustomImageAdapter(new ArrayList<String>());

        /** Fetch Movies or Restore from savedInstanceState */
        // http://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate
        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            fetchMoviesInfo();

        }else {
            mSortMode = savedInstanceState.getString("sort_mode");
            mMovies = savedInstanceState.getParcelableArrayList("movies");
            mMovieAdapter.clear();
            for (Movie aMovie : mMovies) {
                // Store movie poster URL into Adapter
                mMovieAdapter.add(aMovie.getPosterUrl());
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "== onCreateView()");
        // Inflate fragment main into Root View, and get gridView
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);

        // Set more Number of columns in Landscape mode
        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            gridView.setNumColumns(5);

        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra("strings", mMovies.get(position).getAll());
                startActivity(intent);
            }
        });

        return rootView;
    }
    @Override
    public void onStart(){
        Log.d(LOG_TAG, "== onStart()");
        String sort_by = getCurrentSortBy();

        // If Sort Criteria changed
        if(!mSortMode.isEmpty() && sort_by != null && !sort_by.equals(mSortMode))
            fetchMoviesInfo();

        super.onStart();
    }
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("movies", mMovies);
        outState.putString("sort_mode", mSortMode);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_fragment, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            // Force Fetch Movies data
            fetchMoviesInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Get Sort_by settings from Pref
    private String getCurrentSortBy(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
    }
    private boolean isSortByFavorite(String sort_by){
        return sort_by.equalsIgnoreCase(getString(R.string.pref_favorite));
    }
    private void fetchMoviesInfo(){
        Log.d(LOG_TAG, "== fetchMovieInfo()");
        String sort_by = getCurrentSortBy();

        // Fetch movies information in background if Network Available and Not Favorite movie
        if(Utility.isNetworkAvailable(getActivity()) & !isSortByFavorite(sort_by)) {
            Log.d(LOG_TAG, "== Getting Movies from the Internet");

            // Get Movie from Internet
            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
            fetchMoviesTask.execute(sort_by);
            mSortMode = sort_by;

        }else if(isSortByFavorite(sort_by)) {
            Log.d(LOG_TAG, "== Getting Favorite Movies from DB");

            // Get Movie from Database
            String sortOrder = MovieEntry._ID + " DESC";
            Cursor cur = getActivity().getContentResolver().query(MovieEntry.CONTENT_URI,
                    Movie.MOVIE_COLUMNS, null, null, sortOrder);

            // Populate ArrayList of Movies
            mMovies.clear(); // Must clear the list before adding new
            while(cur.moveToNext()) {
                Movie movieObj = new Movie(cur.getString(Movie.COL_TMDB_MOVIE_ID),
                                        cur.getString(Movie.COL_TITLE),
                                        cur.getString(Movie.COL_POSTER_PATH),
                                        cur.getString(Movie.COL_OVERVIEW),
                                        cur.getString(Movie.COL_USER_RATING),
                                        cur.getString(Movie.COL_RELEASE_DATE));
                mMovies.add(movieObj);
            }
            // Populate Movie Poster to ArrayAdapter
            mMovieAdapter.clear(); //Must clear adapter before adding new
            for(Movie aMovie : mMovies) { mMovieAdapter.add(aMovie.getPosterUrl()); }

            mSortMode = sort_by; //Remember current SortMode
        }else{
            Toast toast = Toast.makeText(getActivity(), "Please check your network connection", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    // How-to use Picasso with ArrayAdapter from Big Nerd Ranch
    // https://www.bignerdranch.com/blog/solving-the-android-image-loading-problem-volley-vs-picasso/
    private class CustomImageAdapter extends ArrayAdapter<String> {
        public CustomImageAdapter(ArrayList<String> urls) {
            super(getActivity(), 0, urls);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.grid_item_movie, parent, false);
            }

            ImageView imageView = (ImageView)convertView;
            imageView.setAdjustViewBounds(true); //Adjust its bound to max while Preserve the aspect ratio of Image

            // Download Image from TMDB
            Picasso.with(getActivity())
                    .load("http://image.tmdb.org/t/p/w185" + getItem(position))
                    .into(imageView);

            return convertView;
        }
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        /** Takes the JSON string and converts it into an Object hierarchy. */
        private ArrayList<Movie> getMoviesFromJson(String moviesJsonStr)
                throws JSONException {

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

            // Create a Movie object then add to ArrayList of Movies
            mMovies.clear(); // Must clear the list before adding new
            for(int i = 0; i < moviesArray.length(); i++){
                JSONObject aMovie = moviesArray.getJSONObject(i);
                Movie movieObj = new Movie(aMovie.getString(TMDB_MOVIE_ID),
                                            aMovie.getString(TMDB_ORIGINAL_TITLE),
                                            aMovie.getString(TMDB_POSTER_PATH),
                                            aMovie.getString(TMDB_OVERVIEW),
                                            aMovie.getString(TMDB_USER_RATING),
                                            aMovie.getString(TMDB_RELEASE));
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

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            if (movies != null) {
                mMovieAdapter.clear(); // Must clear adapter before adding new
                for(Movie aMovie : movies) {
                    // Store movie poster URL into Adapter
                    mMovieAdapter.add(aMovie.getPosterUrl());
                }
            }
        }
    }
}
