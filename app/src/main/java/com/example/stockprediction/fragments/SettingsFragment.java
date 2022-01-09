package com.example.stockprediction.fragments;

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

import com.example.stockprediction.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    private Preference myPerf;
    public static final String SETTINGS = "settings";
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        //view.setBackgroundColor(getContext().getResources().getDrawable(R.drawable.round_corners_bg));

        return view;
    }

    private void findViews(View view) {

    }

    private void initViews() {

    }
}
