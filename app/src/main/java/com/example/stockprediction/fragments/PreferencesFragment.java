package com.example.stockprediction.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.stockprediction.R;

public class PreferencesFragment extends PreferenceFragmentCompat {
    private PreferenceManager myPerf;
    // Constants
    private final static String TAG = PreferencesFragment.class.getName().toLowerCase();
    public final static String SETTINGS_SHARED_PREFERENCES = TAG + ".settings";

    /*
     - To access shared preference inside this fragment use -> myPerf.
     - To access shared preference outside this fragment use :
             SharedPreferences preferences = getActivity().getSharedPreferences(
             PreferencesFragment.SETTINGS_SHARED_PREFERENCES,
             Context.MODE_PRIVATE);
     */

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        //setPreferencesFromResource(R.xml.preferences,rootKey);
//        myPerf = (ListPreference) findPreference("settings");
//        myPerf.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                // Enter changes in application
//                return false;
//            }
//        });

        // Define the settings file to use by this settings fragment
        myPerf = getPreferenceManager();
        myPerf.setSharedPreferencesName(SETTINGS_SHARED_PREFERENCES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        //view.setBackgroundColor(getContext().getResources().getDrawable(R.drawable.round_corners_bg));
        return view;
    }
}
