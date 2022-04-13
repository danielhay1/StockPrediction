package com.example.stockprediction.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.example.stockprediction.R;
import com.example.stockprediction.objects.stock.Stock;
import com.google.gson.Gson;

import java.util.ArrayList;

import static com.example.stockprediction.fragments.PreferencesFragment.SETTINGS_SHARED_PREFERENCES;

public class MyPreference {
    private static MyPreference instance;
    private SharedPreferences sharedPreferences;

    private final String PREFERENCE_ROOT = "preferences_root";
    // Application Preference Public Keys:
    public interface KEYS {
        public final String CACHED_STOCKS = "cached_stocks";
        public final String FAV_STOCKS = "favorities_stocks";
        public final String STOCKS_DATA = "stocks_data";
        public final String  SETTINGS = "settings";
        public final String  USER_DETAILS = "user_details";
    }

    public static MyPreference getInstance() {
        //Singleton design pattern
        return instance;
    }

    private MyPreference(Context appContext) {
        sharedPreferences = appContext.getSharedPreferences(PREFERENCE_ROOT,Context.MODE_PRIVATE);
    }

    public static void Init(Context appContext) {
        if (instance == null) {
            Log.d("pttt", "Init: MyPreference");
            instance = new MyPreference(appContext);
            instance.putFavStockArrayList(new ArrayList<Stock>());
        }
    }

    public void putBoolean(String key, boolean value) {
        Log.d("pttt", "putBoolean \tkey= "+key+", Value= "+value);
        SharedPreferences.Editor editor  = this.sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        Log.d("pttt", "getBoolean ,key= "+key);
        return this.sharedPreferences.getBoolean(key,false);
    }

    public void putString(String key, String value) {
        Log.d("pttt", "putString \tkey= "+key+", Value= "+value);
        SharedPreferences.Editor editor  = this.sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        Log.d("pttt", "getString ,key= "+key);
        return this.sharedPreferences.getString(key,"");
    }

    public void putObject(String key, Object object) {
        Log.d("pttt", "putObject \tkey= "+key+", Object= "+object);
        Gson gson = new Gson();
        String jsonElement = gson.toJson(object);
        this.putString(key, jsonElement);
        Log.d("pttt", "putObject: show all " + prefernceToString());
    }

    public Object getObject(String key) {
        Log.d("pttt", "getObject, key= "+key);
        Gson gson = new Gson();
        return gson.fromJson(getString(key),Object.class);
    }

    public Object getStock(String key) {
        Log.d("pttt", "getStock, key= "+key);
        Gson gson = new Gson();
        return gson.fromJson(getString(key), Stock.class);
    }

    // Favorite stocks methods:

    private ArrayList<Stock> getStockArrayList(String key) {
        // TODO: debug method
        Log.d("pttt", "getStock, key= "+key);
        Gson gson = new Gson();
        ArrayList<Stock> stocks = gson.fromJson(getString(key), ArrayList.class);
        return stocks;
    }
    
    public Stock getStockFromFav(String symbol) {
        for (Stock stock: getUserFavStocks()) {
            if(stock.getSymbol().equalsIgnoreCase(symbol)) {
                return stock;
            }
        }
        return null;
    } 

    public ArrayList<Stock> getUserFavStocks() {
        return this.getStockArrayList(KEYS.FAV_STOCKS);
    }

    public void putFavStockArrayList(ArrayList<Stock> stocks) {
        this.putObject(KEYS.FAV_STOCKS,stocks);
    }

    public void removeFavStock(Stock stock) {
        ArrayList<Stock> favStocks = getUserFavStocks();
        favStocks.remove(stock);
        putFavStockArrayList(favStocks);
    }

    public void removeFavStocks() {
        this.deleteKey(KEYS.FAV_STOCKS);
    }

    public void deleteKey(String key) {
        Log.d("pttt", "deleteKey, key= "+key);
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
        Log.d("pttt", "putObject: show all " + prefernceToString());
    }

    public String prefernceToString() {
        return this.sharedPreferences.getAll().toString();
    }

    public void deleteAllData() {
        Log.d("pttt", "deleteAllData");
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    // Preference fragment - Preferences:
    public SettingsInspector getSettingsInspector(Context context) {
        return new SettingsInspector(context);
    }

    private static class SettingsInspector {
        private Context context;
        SharedPreferences sharedPreferences;
        private final int PRIVATE_MODE = 0;
        private final String FIRST_RUN = "first_run";
        // Preference Keys:
        private String notification_mode;
        private String theme_mode;

        public SettingsInspector(Context context) {
            this.context = context;
            notification_mode = context.getString( R.string.settings_notification_key);
            theme_mode = context.getString(R.string.settings_theme_key);
            sharedPreferences  = context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, PRIVATE_MODE);
        }

        //Function returns if the app was run for the first time or not
        public boolean firstRun(){
            return checkPref(FIRST_RUN);
        }
        private boolean checkPref(String preferenceName) {
            return sharedPreferences.getBoolean(preferenceName, false);
        }
        public String getTheme_mode() {
            return sharedPreferences.getString(notification_mode,context.getString(R.string.settings_notification_default));
        }
        public String getNotification_mode() {
            return sharedPreferences.getString(theme_mode,context.getString(R.string.settings_theme_default));
        }

    }
}
