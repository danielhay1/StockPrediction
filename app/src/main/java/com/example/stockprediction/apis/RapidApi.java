package com.example.stockprediction.apis;

import android.app.ActivityManager;
import android.app.MediaRouteActionProvider;
import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.example.stockprediction.R;
import com.example.stockprediction.utils.HttpTasksClasses.BitmapLruCache;
import com.example.stockprediction.utils.HttpTasksClasses.HttpTasks;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

public class RapidApi {
    private  static RapidApi instance;  // Singleton Class
    private Context appContext;
    private final String HOST = "yh-finance.p.rapidapi.com";
    private static String api_key;
    private OkHttpClient client;
    // Stock hash collection
    private final Hashtable<String,String> MY_STOCKS =  new Hashtable<String,String>() {
        {
            MY_STOCKS.put("Nvidia", "NVDA");
        }
    };

    // Callback interface
    public interface CallBack_HttpTasks {
        void onResponse(JSONObject response);
        void onErrorResponse(VolleyError error);
        void onErrorResponse(JSONException error);
    }


    public static RapidApi getInstance() {
        return instance;
    }

    private RapidApi(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static void Init(Context appContext){
        if(instance == null) {
            Log.d("pttt", "Init: HttpTasks");
            instance = new RapidApi(appContext);
            api_key = appContext.getString(R.string.rapidapi);
        }
    }
}
