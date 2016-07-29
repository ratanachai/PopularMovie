package com.ratanachai.popularmovies;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Locale;

public class MainActivityFragment extends BaseFragment {
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private MovieAdapter mMovieAdapter; // Adapter for Grid of Poster Image
    private ArrayList<Movie> mMovies = new ArrayList<>(); // of Movie objects
    private ProgressDialog mProgress;
    private int page = 1; // For Endless Scrolling

    /** A callback interface that all activities containing this fragment must implement.
     *  This allows activities to be notified of item selections, Movie is ready to display*/
    public interface Callback {
        void onItemSelected(String[] movieInfo, String sortBy, View view);
//        void onMoviesReady(String[] movieInfo, String sortBy);
    }

    public void updateMoviesGrid() {
        getMoviesFromDb();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible() && isSortByFavorite(mSortBy))
            getMoviesFromDb();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); //For this fragment to handle menu events.
        mSortBy = getSortBy();

        // Create MovieAdapter every times
        mMovies = new ArrayList<>();
        mMovieAdapter = new MovieAdapter(mMovies);

        // Fetch Movies or Restore from savedInstanceState
        // http://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate
        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            getMovies(page); //Nothing to restore so getMovies
        }else {
            mSortBy = savedInstanceState.getString("sort_mode");
            mMovies = savedInstanceState.getParcelableArrayList("movies");
            mMovieAdapter = new MovieAdapter(mMovies);
            mMovieAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Activity activity = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(mMovieAdapter);

        if( !isSortByFavorite(mSortBy) ) {
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
                ((Callback) activity).onItemSelected(mMovies.get(position).getAll(), mSortBy, view); }
        });
        // To hide Toolbar when scroll (ListView and GridView CoordinatorLayout by default
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            gridView.setNestedScrollingEnabled(true);

        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(LOG_TAG, "== onStart()");
//        getActivity().setTitle(getString(R.string.app_name) + " - " + getCurrentSortByLabel(newSortBy));
        getActivity().setTitle(getString(R.string.app_name));
        // Force Re-Fetch If needReFetch
        mSortBy = getSortBy();
        if( needReFetch ){
            mMovies.clear();
            mMovieAdapter.notifyDataSetChanged();
            getMovies(page);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mProgress!= null) {
            mProgress.dismiss();
            mProgress = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("movies", mMovies);
        outState.putString("sort_mode", mSortBy);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (BuildConfig.DEBUG)
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

    private void getMovies(int page){

        if(isSortByFavorite(mSortBy)) {
            Log.d(LOG_TAG, "== Getting Favorite Movies from DB");
            getMoviesFromDb();

        }else if(Utility.isNetworkAvailable(getActivity())) {
            Log.d(LOG_TAG, "== Getting Movies from the Internet");
            getMoviesFromInternet(mSortBy, page);

        }else{
            Utility.networkToast(getActivity());
            mMovies.clear();
            mMovieAdapter.notifyDataSetChanged();
        }
    }

    private void getMoviesFromDb() {
        // Clear All
        mMovies.clear();

        // Populate mMovies from DB: Get Movie from Database, then Put into ArrayList of Movies
        Cursor cur = getActivity().getContentResolver().query(MovieEntry.CONTENT_URI,
                Movie.MOVIE_COLUMNS, null, null, MovieEntry._ID + " DESC");
        while(cur.moveToNext()) {
            Movie movieObj = new Movie(cur.getString(Movie.COL_TMDB_MOVIE_ID),
                    cur.getString(Movie.COL_TITLE), cur.getString(Movie.COL_POSTER_PATH),
                    cur.getString(Movie.COL_OVERVIEW), cur.getString(Movie.COL_RELEASE_DATE),
                    cur.getString(Movie.COL_VOTE_AVERAGE), cur.getString(Movie.COL_VOTE_COUNT)
            );
            mMovies.add(movieObj);
        }
        cur.close();
        mMovieAdapter.notifyDataSetChanged();
        needReFetch = false; //Reset flag after fetched

        // Auto load the first movie into Tablet right pane
//        if(mMovies.size() > 0)
//            ((Callback) getActivity()).onMoviesReady(mMovies.get(0).getAll(), mSortBy);

    }

    private void getMoviesFromInternet(String sortBy, int page) {
        mProgress = new ProgressDialog(getActivity());
        mProgress.setMessage(getString(R.string.downloading_from_tmdb));
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgress.show();

        // Determine which language, then Get Movie from the TMDB API
        String lang = Locale.getDefault().getLanguage().equals("th") ? "th" : "en";
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        fetchMoviesTask.execute(sortBy, String.valueOf(page), lang);
    }

    /** How-to use Picasso with ArrayAdapter from Big Nerd Ranch
        https://www.bignerdranch.com/blog/solving-the-android-image-loading-problem-volley-vs-picasso/
     */
    private class MovieAdapter extends ArrayAdapter<Movie> {

        public MovieAdapter(ArrayList<Movie> movies) {
            super(getActivity(), 0, movies);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.grid_item_movie, parent, false);

            Movie movie = getItem(position);

            // Download Image from TMDB using mMoviePosterPath (Width: 92, 154, 185, 342, 500, 780)
            ImageView imageView = (ImageView)convertView.findViewById(R.id.grid_item_movie);
            Picasso.with(getActivity())
                    .load("http://image.tmdb.org/t/p/w342" + movie.getPosterPath())
                    .placeholder(R.drawable.film)
                    .fit()
                    .centerCrop()
                    .into(imageView);
            imageView.setContentDescription(movie.getTitle());

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
            final String MOVIES = "results";
            final String MOVIE_ID = "id";
            final String TITLE = "title";
            final String POSTER_PATH = "poster_path";
            final String OVERVIEW = "overview";
            final String RELEASE_DATE = "release_date";
            final String VOTE_AVERAGE = "vote_average";
            final String VOTE_COUNT = "vote_count";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(MOVIES);

            // Populate mMovies from JSON: Create a Movie object then add to ArrayList of Movies
            ArrayList<Movie> movies = new ArrayList<>();
            for(int i = 0; i < moviesArray.length(); i++){
                JSONObject aMovie = moviesArray.getJSONObject(i);
                Movie movieObj = new Movie(aMovie.getString(MOVIE_ID),
                        aMovie.getString(TITLE), aMovie.getString(POSTER_PATH),
                        aMovie.getString(OVERVIEW), aMovie.getString(RELEASE_DATE),
                        aMovie.getString(VOTE_AVERAGE), aMovie.getString(VOTE_COUNT));
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
                //  http://api.themoviedb.org/3/discover/movie?language=th&page=1&sort_by=vote_average.desc&vote_count.gte=500&api_key=4275cdb955fd96533adb7f51ec340f21
                final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String LANG = "language";
                final String PAGE = "page";
                final String SORT_PARAM = "sort_by";
                final String VOTE_COUNT_THREASHOLD = "vote_count.gte";
                final String API_KEY_PARAM = "api_key";

                // Params0-2 = sortBy, page, language, respectively
                Uri.Builder ub = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(LANG, params[2])
                        .appendQueryParameter(PAGE, params[1])
                        .appendQueryParameter(SORT_PARAM, params[0]+".desc")
                        .appendQueryParameter(API_KEY_PARAM, api_key);

                if (params[0].equals(getString(R.string.sort_value_vote)))
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
                mMovieAdapter.notifyDataSetChanged();
                mProgress.hide();
                // Auto load the first movie into Tablet right pane
//                if(mMovies.size() > 0)
//                    ((Callback) getActivity()).onMoviesReady(mMovies.get(0).getAll(), mSortBy);
            }
        }
    }
}
