package com.example.stockprediction;

import android.app.Application;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.stockprediction.apis.RapidApi;
import com.example.stockprediction.utils.firebase.MyFireBaseServices;
import com.example.stockprediction.utils.MyPreference;
import com.example.stockprediction.utils.MySignal;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // App singleton-classes initiate
        MySignal.Init(this);
        RapidApi.Init(this);
        MyFireBaseServices.getInstance().Init();
        //MyPreference.getInstance(this).clearStockCache();
        setTheme();
    }

    private void setTheme() {
        Log.d("app_delegate", "setTheme: "+MyPreference.SettingsInspector.getInstance(this).getTheme_mode());
        String themeVal = MyPreference.SettingsInspector.getInstance(this).getTheme_mode();
        if (themeVal != null) {
            Log.d("app_delegate", "theme = " + themeVal);
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
        }
    }
}
