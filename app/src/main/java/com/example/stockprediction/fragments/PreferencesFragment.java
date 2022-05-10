package com.example.stockprediction.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.stockprediction.R;

public class PreferencesFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private PreferenceManager myPerf;
    // Constants
    private final static String TAG = PreferencesFragment.class.getName().toLowerCase();
    public final static String SETTINGS_SHARED_PREFERENCES = TAG + ".settings";
    public static String theme_mode;
    public static String notification_mode;
    private ListPreference themePreference;
    private ListPreference notificationPreference;
    /*
     - To access shared preference inside this fragment use -> myPerf.
     - To access shared preference outside this fragment use :
             SharedPreferences preferences = getActivity().getSharedPreferences(
             PreferencesFragment.SETTINGS_SHARED_PREFERENCES,
             Context.MODE_PRIVATE);
     */

    private void findPreferences() {
        themePreference = (ListPreference) findPreference(getContext().getResources().getString(R.string.settings_theme_key));
        notificationPreference = (ListPreference) findPreference(getContext().getResources().getString(R.string.settings_notification_key));
        themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary((CharSequence) newValue);
            return true;
        });
        notificationPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary((CharSequence) newValue);
            return true;
        });
    }

    private void setDefaultValue(String value, ListPreference preferenceList) {
        if(!value.equals("")) {
            preferenceList.setValue(value);
        }
    }

    private void setSummary(String value, ListPreference preferenceList) {
        if(!value.equals("")) {
            preferenceList.setSummary(value);
        }
    }

    private void initListPreference() {
        String currentThemeValue = myPerf.getSharedPreferences().getString(getContext().getResources().getString(R.string.settings_theme_key),"");
        String currentNotificationValue = myPerf.getSharedPreferences().getString(getContext().getResources().getString(R.string.settings_notification_key),"");
        setSummary(currentThemeValue,themePreference);
        setSummary(currentNotificationValue,notificationPreference);
        setDefaultValue(currentThemeValue,themePreference);
        setDefaultValue(currentNotificationValue,notificationPreference);

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        // Define the settings file to use by this settings fragment
        myPerf = getPreferenceManager();
        myPerf.setSharedPreferencesName(SETTINGS_SHARED_PREFERENCES);
        findPreferences();
        Log.d("preferences_fragment", "onCreatePreferences: settings_notification="+ myPerf.getSharedPreferences().getString(getContext().getResources().getString(R.string.settings_notification_key),"X"));
        Log.d("preferences_fragment", "onCreatePreferences: settings_theme=" + myPerf.getSharedPreferences().getString(getContext().getResources().getString(R.string.settings_theme_key),"X"));
        initListPreference();
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
                themePreference.setSummary(themeVal);

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
                    notificationPreference.setSummary(notificationVal);
                    //MyPreference.SettingsInspector.getInstance(getContext()).s
                }
            }
        }
    }
}
