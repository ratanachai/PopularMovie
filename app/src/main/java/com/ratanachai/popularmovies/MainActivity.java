package com.ratanachai.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;


public class MainActivity extends AppCompatActivity
        implements MainActivityFragment.Callback, DetailActivityFragment.Callback {

    public static boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    ViewPager mViewPager;

    @Override
    public void onAddRemoveMovieFromFavorite(boolean needReFetch){
        MainActivityFragment mainFragment = (MainActivityFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment);
        mainFragment.updateMoviesGrid();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Two pane or One pane
        if (findViewById(R.id.movie_detail_container) != null){
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            // Setup Sliding Tab navigation
            ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
            PagerAdapter pagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager(), this);
            viewPager.setAdapter(pagerAdapter);
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
            tabLayout.setupWithViewPager(viewPager);
            // Setup Toolbar
            Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
            toolbar.setTitle(getString(R.string.app_name));
            setSupportActionBar(toolbar);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(String[] movieInfo, String sortBy, View view) {
        if (mTwoPane) {
            replaceDetailFragment(movieInfo, sortBy);
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra("strings", movieInfo)
                    .putExtra("SortBy", sortBy);

            // Save Low-res poster into disk for a placeholder with/without transition animation
            ImageView imageView = (ImageView) view.findViewById(R.id.grid_item_movie);
            createJpgFromImageView(imageView);

            // Add Shared element activity transition for poster
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(this, imageView, "poster_zoom");
                startActivity(intent, options.toBundle());
            }else {
                startActivity(intent);
            }

        }
    }
    private String createJpgFromImageView(ImageView imageView){
        String filename = "InterimPoster";
        try{
            // Get bitmap from ImageView
            BitmapDrawable drawable = (BitmapDrawable)imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            // Convert bitmap to JPG
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            // Write into JPG file
            FileOutputStream fo = openFileOutput(filename, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();

        } catch (Exception e){
            e.printStackTrace();
            filename = null;
        }
        return filename;
    }
    @Override
    public void onMoviesReady(String[] movieInfo, String sortBy) {
        if (mTwoPane)
            replaceDetailFragment(movieInfo, sortBy);
    }

    private void replaceDetailFragment(String[] movieInfo, String sortBy) {
        Bundle args = new Bundle();
        args.putStringArray(DetailActivityFragment.MOVIE_INFO, movieInfo);
        args.putString("SortBy", sortBy);
        DetailActivityFragment fragment = new DetailActivityFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                .commit();
    }

    /**
     * Returns a fragment corresponding to one of the primary sections of the app.
     */
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        Context mContext;
        public AppSectionsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            mContext = context;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    Fragment f = new MainActivityFragment();
                    Bundle args = new Bundle();
                    args.putString("SortBy", "popularity");
                    f.setArguments(args);
                    return f;
                case 1:
                    Fragment f1 = new MainActivityFragment();
                    Bundle args1 = new Bundle();
                    args1.putString("SortBy", "vote_average");
                    f1.setArguments(args1);
                    return f1;
                default:
                    Fragment f2 = new MainActivityFragment();
                    Bundle args2 = new Bundle();
                    args2.putString("SortBy", "favorite");
                    f2.setArguments(args2);
                return f2;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int i) {
            switch (i) {
                case 0: return mContext.getString(R.string.pref_sort_popularity_label);
                case 1: return mContext.getString(R.string.pref_sort_rating_label);
                default: return mContext.getString(R.string.pref_favorite_label);
            }
        }
    }

}
