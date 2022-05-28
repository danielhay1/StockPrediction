package com.example.stockprediction.data_access_layer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.stockprediction.R;
import com.example.stockprediction.data_access_layer.apis.RapidApi;
import com.example.stockprediction.business_logic_layer.objects.stock.Stock;
import com.example.stockprediction.utils.MyTimeStamp;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.stockprediction.presentation_layer.fragments.PreferencesFragment.SETTINGS_SHARED_PREFERENCES;

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
                Log.d("MyPreference", "Init: MyPreference");
                instance = new MyPreference(appContext);
                instance.putFavStockArrayList(new ArrayList<Stock>());
                Log.d("my_preference", "json= "+instance.getStocksData(StockCacheManager.CACHE_KEYS.STOCKS_DATA_JSON));
            }
        }
    }

    public void putBoolean(String key, boolean value) {
        Log.d("my_preference", "putBoolean \tkey= "+key+", Value= "+value);
        SharedPreferences.Editor editor  = this.sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        Log.d("my_preference", "getBoolean ,key= "+key);
        return this.sharedPreferences.getBoolean(key,false);
    }

    public void putString(String key, String value) {
        Log.d("my_preference", "putString \tkey= "+key+", Value= "+value);
        SharedPreferences.Editor editor  = this.sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        Log.d("my_preference", "getString ,key= "+key);
        return this.sharedPreferences.getString(key,"");
    }

    public void putObject(String key, Object object) {
        Log.d("my_preference", "putObject \tkey= "+key+", Object= "+object);
        Gson gson = new Gson();
        String jsonElement = gson.toJson(object);
        this.putString(key, jsonElement);
        Log.d("my_preference", "putObject: show all " + prefernceToString());
    }

    public Object getObject(String key) {
        Log.d("my_preference", "getObject, key= "+key);
        Gson gson = new Gson();
        return gson.fromJson(getString(key),Object.class);
    }

    public Object getStock(String key) {
        Log.d("my_preference", "getStock, key= "+key);
        Gson gson = new Gson();
        return gson.fromJson(getString(key), Stock.class);
    }

    private JSONObject putJsonObject(String key, JSONObject jsonObject, String jsonKey, String operation) {
        JSONObject customJsonObject = StockCacheManager.generateCustomObject(jsonObject,jsonKey,operation);
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
        Log.d("my_preference", "getStock, key= "+key);
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

    public void deleteKey(String key) {
        Log.d("my_preference", "deleteKey, key= "+key);
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
        Log.d("my_preference", "putObject: show all " + prefernceToString());
    }

    public void clearStockCache() {
        Log.d("my_preference", "clearStockCache()");
        for (String symbol: RapidApi.MY_STOCKS.values()) {
            deleteKey(StockCacheManager.CACHE_KEYS.CHARTS_DATA_JSON+symbol);
        }
        deleteKey(StockCacheManager.CACHE_KEYS.STOCKS_DATA_JSON);
    }

    public String prefernceToString() {
        return this.sharedPreferences.getAll().toString();
    }

    public void deleteAllData() {
        Log.d("my_preference", "deleteAll" +
                "Data");
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    // Preference fragment - Preferences:
    public SettingsInspector getSettingsInspector(Context context) {
        return SettingsInspector.getInstance(context);
    }

    // RapidAPI cache store & load:
    public JSONObject putStocksData(JSONObject jsonObject, String jsonKey, String cacheKey, String operation) {
        return this.putJsonObject(cacheKey,jsonObject,jsonKey,operation);
    }

    public JSONObject getStocksData(String cacheKey) {
        return this.getJsonObject(cacheKey);
    }

    public JSONObject addJsonToStocksData(JSONObject jsonObject, String jsonKey, String cacheKey, String operation) throws JSONException {
        JSONObject json;
        try{
            json = this.getJsonObject(cacheKey);
        } catch (NullPointerException e) {
            json = new JSONObject();
        }
        Log.e("my_preference", "addJsonToStocksData: run after catch");
        this.putJsonObject(cacheKey,jsonObject,jsonKey, operation);
        return json;
    }

    public static class StockCacheManager {
        public interface CACHE_KEYS {
            public final String STOCKS_DATA_JSON = "stocks_data_json";
            public final String CHARTS_DATA_JSON = "CHART_";

        }
        public interface REFRESH_INTERVAL {
            public final static int STOCK_DATA = 24;
            public final static int CHARTS_DATA = 24;
        }

        public static Boolean shouldRefreshCache(JSONObject json,int refreshInterval) throws JSONException {
            long hourInSec = 60 * 60;
            Long currentTimeStamp = System.currentTimeMillis()/1000;
            Long refreshTimeStamp = currentTimeStamp - (hourInSec * refreshInterval);
            Long cacheTimeStamp = json.getLong("request_timestamp");
            Log.d("rapid_api", "shouldRefreshCache: "+(cacheTimeStamp < refreshTimeStamp));
            return cacheTimeStamp < refreshTimeStamp;
        }

        public static JSONObject generateCustomObject(JSONObject json, String jsonKey, String operation) {
            JSONObject customJson = new JSONObject();
            try {
                customJson.put("request_day", MyTimeStamp.getCurrentDay());
                customJson.put("request_timestamp",System.currentTimeMillis()/1000);
                customJson.put("operation",operation);
                //customJson.put("stocks",json);
                customJson.put(jsonKey,json);
            } catch (JSONException e) {
                Log.e("my_preference", "Cache: putJson error: "+ e.getLocalizedMessage());
            }
            return customJson;
        }
    }

    public static class SettingsInspector {
        private Context context;
        SharedPreferences sharedPreferences;
        private final int PRIVATE_MODE = 0;
        private final String FIRST_RUN = "first_run";
        // Preference Keys:
        private String notification_mode;
        private String theme_mode;
        private static SettingsInspector instance;
        private static final Object lock = new Object();

        public static SettingsInspector getInstance(Context appContext) {
            //Singleton design pattern
            Init(appContext);
            return instance;
        }

        private SettingsInspector(Context context) {
            this.context = context;
            notification_mode = context.getString( R.string.settings_notification_key);
            theme_mode = context.getString(R.string.settings_theme_key);
            sharedPreferences  = context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES, PRIVATE_MODE);
        }

        public static void Init(Context appContext) {
            if (instance == null) {
                synchronized (lock) {
                    Log.d("SettingsInspector", "Init: SettingsInspector");
                    instance = new SettingsInspector(appContext);
                }
            }
        }

        //Function returns if the app was run for the first time or not
        public boolean firstRun(){
            return checkPref(FIRST_RUN);
        }
        private boolean checkPref(String preferenceName) {
            return sharedPreferences.getBoolean(preferenceName, false);
        }
        public Boolean getNotification_mode() {
            return sharedPreferences.getBoolean(notification_mode,true);
        }
        public String getTheme_mode() {
            return sharedPreferences.getString(theme_mode,context.getString(R.string.settings_theme_default));
        }
        public void cleanPreference() {
            Log.d("SettingsInspector", "deleteAll" +
                    "Data");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }
    }
}
