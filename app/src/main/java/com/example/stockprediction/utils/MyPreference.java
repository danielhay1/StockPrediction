package com.example.stockprediction.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.stockprediction.R;
import com.example.stockprediction.apis.RapidApi;
import com.example.stockprediction.objects.stock.Stock;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.example.stockprediction.fragments.PreferencesFragment.SETTINGS_SHARED_PREFERENCES;

public class MyPreference {
    private static MyPreference instance;
    private SharedPreferences sharedPreferences;
    private static final Object lock = new Object();


    private final String PREFERENCE_ROOT = "preferences_root";
    // Application Preference Public Keys:
    public interface KEYS {
        public final String CACHED_STOCKS = "cached_stocks";
        public final String FAV_STOCKS = "favorities_stocks";
        public final String  SETTINGS = "settings";
        public final String  USER_DETAILS = "user_details";
    }

    public static MyPreference getInstance(Context appContext) {
        //Singleton design pattern
        Init(appContext);
        return instance;
    }

    private MyPreference(Context appContext) {
        sharedPreferences = appContext.getSharedPreferences(PREFERENCE_ROOT,Context.MODE_PRIVATE);
    }

    public static void Init(Context appContext) {
        if (instance == null) {
            synchronized (lock) {
                Log.d("pttt", "Init: MyPreference");
                instance = new MyPreference(appContext);
                instance.putFavStockArrayList(new ArrayList<Stock>());
                Log.d("my_preference", "json= "+instance.getStocksData(StockCacheManager.CACHE_KEYS.STOCKS_DATA_JSON));
            }
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

    private JSONObject putJsonObject(String key, JSONObject jsonObject, String jsonKey) {
        JSONObject customJsonObject = StockCacheManager.generateCustomObject(jsonObject,jsonKey);
        this.putString(key,customJsonObject.toString());
        return customJsonObject;
    }

    public JSONObject getJsonObject(String key) {
        try {
            return new JSONObject(getString(key));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
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

    public void clearStockCache() {
        Log.d("pttt", "clearStockCache()");
        for (String symbol: RapidApi.MY_STOCKS.values()) {
            deleteKey(StockCacheManager.CACHE_KEYS.CHARTS_DATA_JSON+symbol);
        }
        deleteKey(StockCacheManager.CACHE_KEYS.STOCKS_DATA_JSON);
    }

    public String prefernceToString() {
        return this.sharedPreferences.getAll().toString();
    }

    public void deleteAllData() {
        Log.d("pttt", "deleteAll" +
                "Data");
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    // Preference fragment - Preferences:
    public SettingsInspector getSettingsInspector(Context context) {
        return new SettingsInspector(context);
    }

    // RapidAPI cache store & load:
    public JSONObject putStocksData(JSONObject jsonObject, String jsonKey, String cacheKey) {
        return this.putJsonObject(cacheKey,jsonObject,jsonKey);
    }

    public JSONObject getStocksData(String cacheKey) {
        return this.getJsonObject(cacheKey);
    }

    public JSONObject addJsonToStocksData(JSONObject jsonObject, String jsonKey, String cacheKey) throws JSONException {
        JSONObject json;
        try{
            json = this.getJsonObject(cacheKey);
        } catch (NullPointerException e) {
            json = new JSONObject();
        }
        Log.e("pttt", "addJsonToStocksData: run after catch");
        this.putJsonObject(cacheKey,jsonObject,jsonKey);
        return json;
    }

    public static class StockCacheManager {
        public interface CACHE_KEYS {
            public final String STOCKS_DATA_JSON = "stocks_data_json";
            public final String CHARTS_DATA_JSON = "CHART_";

        }
        public interface REFRESH_INTERVAL {
            public final static int STOCK_DATA = 24 * 30; // In hours // TODO: change to 24 hour
            public final static int CHARTS_DATA = 24 * 30; // In hours // TODO: change to 24 hour
        }

        public static Boolean shouldRefreshCache(JSONObject json,int refreshInterval) throws JSONException {
            long hourInSec = 60 * 60;
            Long currentTimeStamp = System.currentTimeMillis()/1000;
            Long refreshTimeStamp = currentTimeStamp - (hourInSec * refreshInterval);
            Long cacheTimeStamp = json.getLong("request_timestamp");
            Log.d("rapid_api", "shouldRefreshCache: "+(cacheTimeStamp < refreshTimeStamp));
            return cacheTimeStamp < refreshTimeStamp;
        }

        private static String getCurrentDay() {
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            // full name form of the day
            return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
        }

        private static JSONObject generateCustomObject(JSONObject json, String jsonKey) {
            JSONObject customJson = new JSONObject();
            try {
                customJson.put("request_day",getCurrentDay());
                customJson.put("request_timestamp",System.currentTimeMillis()/1000);
                //customJson.put("stocks",json);
                customJson.put(jsonKey,json);
            } catch (JSONException e) {
                Log.e("MyPreference", "Cache: putJson error: "+ e.getLocalizedMessage());
            }
            return customJson;
        }
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
