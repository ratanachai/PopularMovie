package com.ratanachai.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

/**
 * Brief explaination about this file
 * Created by Ratanachai on 15/10/04.
 */
public class BaseFragment extends Fragment {
    protected static boolean needReFetch = false;

    // Get Sort_by settings from Pref
    protected String getCurrentSortBy(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
    }
    protected boolean isSortByFavorite(String sort_by){
        return sort_by.equalsIgnoreCase(getString(R.string.pref_favorite));
    }
}
