package com.ratanachai.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra("strings")){
            String[] movieInfo = intent.getStringArrayExtra("strings");

            // Set all TextView and Poster
            ((TextView) rootview.findViewById(R.id.movie_title)).setText(movieInfo[0]);
            ((TextView) rootview.findViewById(R.id.movie_overview)).setText(movieInfo[2]);
            ((TextView) rootview.findViewById(R.id.movie_rating)).append(" "+movieInfo[3]+"/10");
            ((TextView) rootview.findViewById(R.id.movie_release)).append(" "+movieInfo[4]);
            Picasso.with(getActivity())
                    .load("http://image.tmdb.org/t/p/w185" + movieInfo[1])
                    .into((ImageView)rootview.findViewById(R.id.movie_poster));

        }

        return rootview;
    }
}
