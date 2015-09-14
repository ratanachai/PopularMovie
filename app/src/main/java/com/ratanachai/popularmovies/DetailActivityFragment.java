package com.ratanachai.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class DetailActivityFragment extends Fragment {

    private ArrayList<Video> mVideos = new ArrayList<>();
    private View mRootview;

    public DetailActivityFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        /** Fetch Videos from TMDB */
        // http://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate
        if (savedInstanceState == null || !savedInstanceState.containsKey("videos")) {
            if(savedInstanceState == null) {Log.v("======", "savedInsta is NULL");}
            fetchVideosInfo(getActivity().getIntent().getStringArrayExtra("strings")[0]);
            Log.v("======", "fetched");

        /** or Restore from savedInstanceState */
        }else {
            mVideos = savedInstanceState.getParcelableArrayList("videos");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if(!mVideos.isEmpty()) outState.putParcelableArrayList("videos", mVideos);
        Log.v("======", "onSaveIn");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootview = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra("strings")){
            String[] movieInfo = intent.getStringArrayExtra("strings");

            // Set all TextView and Poster
            ((TextView) mRootview.findViewById(R.id.movie_title)).setText(movieInfo[1]);
            ((TextView) mRootview.findViewById(R.id.movie_overview)).setText(movieInfo[3]);
            ((TextView) mRootview.findViewById(R.id.movie_rating)).append(" "+movieInfo[4]+"/10");
            ((TextView) mRootview.findViewById(R.id.movie_release)).append(" " + movieInfo[5]);
            Picasso.with(getActivity())
                    .load("http://image.tmdb.org/t/p/w185" + movieInfo[2])
                    .into((ImageView) mRootview.findViewById(R.id.movie_poster));

            // Setup Trailer Videos TextView
            if (!mVideos.isEmpty()) {
                ViewGroup containerView = (ViewGroup) mRootview.findViewById(R.id.movie_trailers_container);
                for (Video aVideo : mVideos) {

                    LayoutInflater in = getLayoutInflater(null);
                    View tmpView = in.inflate(R.layout.video_link_item, null);
                    TextView textView = (TextView) tmpView.findViewById(R.id.tmp);

                    textView.setText(aVideo.getName());
                    containerView.addView(textView);
                    
                }
            }
            // Setup Youtube App launch a video OnItemClick

//            videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    //String name = (String)parent.getItemAtPosition(position);
//                    String key = mVideos.get(position-1).getKey(); //ListView's header is position 0
//                    try {
//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:"+key));
//                        startActivity(intent);
//                    }catch (ActivityNotFoundException e){
//                        Intent intent = new Intent(Intent.ACTION_VIEW,
//                                Uri.parse("http://www.youtube.com/watch?v="+key));
//                        startActivity(intent);
//                    }
//                }
//            });


        }

        return mRootview;
    }


    //Based on a http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void fetchVideosInfo(String video_id){

        // Fetch movies information in background
        if(isNetworkAvailable()) {
            //Toast.makeText(getActivity(),"Fetching", Toast.LENGTH_SHORT).show();
            FetchVideosTask fetchMoviesTask = new FetchVideosTask();
            fetchMoviesTask.execute(video_id);
        }else{
            Toast toast = Toast.makeText(getActivity(), "Please check your network connection", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
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
                                            aVideo.getString(TMDB_VIDEO_SITE),
                                            aVideo.getString(TMDB_VIDEO_TYPE));
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

                ViewGroup containerView = (ViewGroup) mRootview.findViewById(R.id.movie_trailers_container);

                for(Video aVideo : videos) {

                    LayoutInflater in = getLayoutInflater(null);
                    View tmpView = in.inflate(R.layout.video_link_item, null);
                    TextView textView = (TextView) tmpView.findViewById(R.id.tmp);
                    textView.setText(aVideo.getName());
                    containerView.addView(textView);
                }
            }
        }
    }
}
