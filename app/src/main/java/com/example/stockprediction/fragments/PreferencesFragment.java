package com.example.stockprediction.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.example.stockprediction.R;
import com.example.stockprediction.activites.MainActivity;
import com.example.stockprediction.utils.MyPreference;
import com.example.stockprediction.utils.firebase.MyFireBaseServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class PreferencesFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private PreferenceManager myPerf;
    // Constants
    private final static String TAG = PreferencesFragment.class.getName().toLowerCase();
    public final static String SETTINGS_SHARED_PREFERENCES = TAG + ".settings";
    public static String theme_mode;
    public static String notification_mode;
    private ListPreference themePreference;
    private SwitchPreference notificationPreference;
    private Preference aboutUsBtn;
    /*
     - To access shared preference inside this fragment use -> myPerf.
     - To access shared preference outside this fragment use :
             SharedPreferences preferences = getActivity().getSharedPreferences(
             PreferencesFragment.SETTINGS_SHARED_PREFERENCES,
             Context.MODE_PRIVATE);
     */

    private void initPreferences() {
        themePreference = (ListPreference) findPreference(getContext().getResources().getString(R.string.settings_theme_key));
        notificationPreference = (SwitchPreference) findPreference(getContext().getResources().getString(R.string.settings_notification_key));
        aboutUsBtn = (Preference) findPreference("about_us");
        themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue != null) {
                Log.d("preferences_fragment", "onSharedPreferenceChanged: theme = " + newValue);
                int theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                switch (newValue.toString().toLowerCase()) {
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
            }
            preference.setSummary((CharSequence) newValue);
            return true;
        });
        notificationPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue != null) {
                Log.d("preferences_fragment", "onSharedPreferenceChanged: notification = " + newValue);
                if(newValue.toString().toLowerCase() == "true") {
                    MyFireBaseServices.getInstance().registerPredictionTopic(task -> {});
                } else {
                    MyFireBaseServices.getInstance().unregisterPredictionTopic();
                }
                //notificationPreference.setSummary(notificationVal);
            }
            return true;
        });
        aboutUsBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity)getActivity()).openAboutUsFragment();
                return false;
            }
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
        String currentThemeValue = myPerf.getSharedPreferences().getString(getContext().getResources().getString(R.string.settings_theme_key),getContext().getResources().getString(R.string.settings_theme_default));
        Boolean currentNotificationValue = myPerf.getSharedPreferences().getBoolean(getContext().getResources().getString(R.string.settings_notification_key),true);
        setSummary(currentThemeValue,themePreference);
        setDefaultValue(currentThemeValue,themePreference);
        Log.d("preferences_fragment", "initListPreference: settings_notification= "+ currentNotificationValue);
        notificationPreference.setChecked(currentNotificationValue);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        // Define the settings file to use by this settings fragment
        myPerf = getPreferenceManager();
        myPerf.setSharedPreferencesName(SETTINGS_SHARED_PREFERENCES);
        initPreferences();
        //Log.d("preferences_fragment", "onCreatePreferences: settings_notification="+ myPerf.getSharedPreferences().getString(getContext().getResources().getString(R.string.settings_notification_key),"X"));
        //Log.d("preferences_fragment", "onCreatePreferences: settings_theme=" + myPerf.getSharedPreferences().getString(getContext().getResources().getString(R.string.settings_theme_key),"X"));
        Log.d("preferences_fragment", "onCreatePreferences: settings_notification=" + MyPreference.SettingsInspector.getInstance(getContext()).getNotification_mode());
        Log.d("preferences_fragment", "onCreatePreferences: settings_theme=" + MyPreference.SettingsInspector.getInstance(getContext()).getTheme_mode());
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
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) { }
}
