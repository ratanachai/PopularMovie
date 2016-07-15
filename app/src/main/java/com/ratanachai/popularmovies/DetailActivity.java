package com.ratanachai.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;


public class DetailActivity extends AppCompatActivity implements DetailActivityFragment.Callback {

    // Call back methods
    @Override
    public void onAddRemoveMovieFromFavorite(boolean needReFetch) {
        // For one pane Activity, tell MasterView to refetch Grid on Create
        BaseFragment.needReFetch = needReFetch;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if(savedInstanceState == null){
            Bundle args = new Bundle();
            // Pass what been passed on from MainActivity in Intent Extra to Fragment via args
            args.putStringArray(DetailActivityFragment.MOVIE_INFO, getIntent().getStringArrayExtra("strings"));
            args.putString("SortBy", getIntent().getStringExtra("SortBy"));
            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case android.R.id.home:
                // To reverse shared element transition when up button is pressed
                supportFinishAfterTransition();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
