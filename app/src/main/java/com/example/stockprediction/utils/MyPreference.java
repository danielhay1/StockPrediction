package com.example.stockprediction.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.stockprediction.objects.Stock;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MyPreference {
    private static MyPreference instance;
    private SharedPreferences sharedPreferences;

    private final String PREFERENCE_ROOT = "preferences_root";
    // private final String FAV_STOCKS = "favorities_stocks";

    public interface KEYS {
        public final String FAV_STOCKS = "favorities_stocks";
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

    public ArrayList<Stock> getStockArrayList(String key) {
        // TODO: debug method
        Log.d("pttt", "getStock, key= "+key);
        Gson gson = new Gson();
        ArrayList<Stock> stocks = gson.fromJson(getString(key), ArrayList.class);
        return stocks;
    }

    public ArrayList<Stock> getUserFavStocks() {
        return this.getStockArrayList(KEYS.FAV_STOCKS);
    }

    public void putStockArrayList(ArrayList<Stock> stocks) {
        this.putObject(KEYS.FAV_STOCKS,stocks);
    }

    public void removeFavStock() {
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
}
