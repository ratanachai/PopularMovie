package com.ratanachai.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import java.util.Arrays;

/**
 * Functions that are shared by all Fragments
 * Created by Ratanachai on 15/10/04.
 */
public class BaseFragment extends Fragment {
    // a flag for Detail to tell Main fragment that Star is removed, so Movie list need to be reFetch.
    protected static boolean needReFetch = false;
    protected String mSortBy = "";

    // Get Sort_by settings from Pref
    protected String getPrefSortBy(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
    }
    protected boolean isSortByFavorite(String sort_by){
        return sort_by.equalsIgnoreCase(getString(R.string.pref_favorite));
    }
    protected String getCurrentSortByLabel(String sort_by){
        int index = Arrays.asList(getResources().getStringArray(R.array.pref_sort_values)).indexOf(sort_by);
        return getResources().getStringArray(R.array.pref_sort_options)[index];
    }
    protected String getSortBy() {
        if (getArguments() != null)
            return getArguments().getString("SortBy");
        else
            return getPrefSortBy(getActivity());
    }
}
