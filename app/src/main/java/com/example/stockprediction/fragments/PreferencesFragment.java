package com.example.stockprediction.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.stockprediction.R;
import com.example.stockprediction.utils.MyPreference;
import com.example.stockprediction.utils.MySignal;

public class PreferencesFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private PreferenceManager myPerf;
    // Constants
    private final static String TAG = PreferencesFragment.class.getName().toLowerCase();
    public final static String SETTINGS_SHARED_PREFERENCES = TAG + ".settings";
    public static String theme_mode;
    public static String notification_mode;
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
        // Define the settings file to use by this settings fragment
        myPerf = getPreferenceManager();
        myPerf.setSharedPreferencesName(SETTINGS_SHARED_PREFERENCES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        //view.setBackgroundColor(getContext().getResources().getDrawable(R.drawable.round_corners_bg));
        if(theme_mode == null) {theme_mode = getString(R.string.settings_theme_key);}
        if(notification_mode == null) {notification_mode = getString(R.string.settings_notification_key);}

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key == theme_mode) {
            String themeVal = sharedPreferences.getString(key, null);
            if (themeVal != null) {
                Log.d("pttt", "onSharedPreferenceChanged: theme = " + themeVal);
                int theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                switch (themeVal.toLowerCase()) {
                    case "day":
                        theme = AppCompatDelegate.MODE_NIGHT_NO;
                        break;
                    case "night":
                        theme = AppCompatDelegate.MODE_NIGHT_YES;
                        break;
                    case "same as system":
                        theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                        break;
                }
                AppCompatDelegate.setDefaultNightMode(theme);
            } else if (key == notification_mode) {
                // code block
                Log.d("pttt", "onSharedPreferenceChanged: ");
                String notificationVal = sharedPreferences.getString(key, null);
                if (notificationVal != null) {
                    Log.d("pttt", "onSharedPreferenceChanged: theme = " + notificationVal);
                    switch (notificationVal.toLowerCase()) {
                        case "all predictions":
                            break;
                        case "favorite stocks":
                            break;
                        case "none":
                            break;
                    }
                }
            }
        }
    }
}
