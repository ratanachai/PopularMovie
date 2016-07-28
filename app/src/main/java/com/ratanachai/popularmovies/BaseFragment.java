package com.ratanachai.popularmovies;

import android.support.v4.app.Fragment;

/**
 * Functions that are shared by all Fragments
 * Created by Ratanachai on 15/10/04.
 */
public class BaseFragment extends Fragment {
    // a flag for Detail to tell Main fragment that Star is removed, so Movie list need to be reFetch.
    protected static boolean needReFetch = false;
    protected String mSortBy = "";

    protected boolean isSortByFavorite(String sort_by){
        return sort_by.equalsIgnoreCase(getString(R.string.sort_value_favorite));
    }
    protected String getSortBy() {
        if (getArguments() != null)
            return getArguments().getString(getString(R.string.sort_key));
        else
            return "popularity"; // Default
    }
    public String getmSortBy() {
        return mSortBy;
    }
}
