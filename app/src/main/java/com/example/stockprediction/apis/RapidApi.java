package com.example.stockprediction.apis;

import android.app.ActivityManager;
import android.app.MediaRouteActionProvider;
import android.content.Context;
import android.util.Log;
import com.example.stockprediction.R;
import com.example.stockprediction.objects.Stock;
import com.google.firebase.database.core.persistence.PruneForest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Hashtable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class RapidApi {
    private  static RapidApi instance;  // Singleton Class
    private Context appContext;
    private final String HOST = "yh-finance.p.rapidapi.com";
    private static String api_key;
    private OkHttpClient client;
    // Stock hash collection
    public final Hashtable<String,String> MY_STOCKS =  new Hashtable<String,String>() {
        {
            MY_STOCKS.put("Nvidia", "NVDA");
        }
    };

    private final Hashtable<String,String> STOCK_OPERATIONS =  new Hashtable<String,String>() {
        {
            STOCK_OPERATIONS.put("Historical_data", "/stock/v3/get-historical-data");
            STOCK_OPERATIONS.put("Summary", "/stock/v2/get-summary");

        }
    };

    // Callback interface
    public interface CallBack_HttpTasks {
        void onResponse(JSONObject response);
        void onErrorResponse(JSONException error);
    }


    public static RapidApi getInstance() {
        return instance;
    }

    private RapidApi(Context context) {
        this.appContext = context.getApplicationContext();
        this.api_key = this.appContext.getString(R.string.rapidapi);
    }

    public static void Init(Context context){
        if(instance == null) {
            Log.d("pttt", "Init: RapidApi");
            instance = new RapidApi(context);
        }
    }

    private void getHistoricalData(String symbol) {
        try {
             String requestedJson = this.httpGetJson(symbol,this.STOCK_OPERATIONS.get("Historical_data"));
             // Convert json to stock
             Stock.JsonToStock("");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (httpGetRequestException e) {
            e.printStackTrace();
            Log.e("pttt", "httpGetRequest: Response Failed!");
        }
    }

    private String rapidUrlBuilder(String symbol, String operation, String region) {
        /**
         *  Optional symbols -> this.MY_STOCKS
         *  Optional operations -> this.STOCK_OPERATIONS
         *  Optional regions -> US|
         */
        String apiUrl = "https://yh-finance.p.rapidapi.com";
        String url = apiUrl + operation + "?symbol=" + symbol + "&region=" + region;
        return url;
    }
    private String httpGetJson(String symbol, String operation) throws IOException, httpGetRequestException {
        String url = this.rapidUrlBuilder(symbol,operation,"US");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-host", this.HOST)
                .addHeader("x-rapidapi-key", this.api_key)
                .build();

        Response response = client.newCall(request).execute();
        if(response.isSuccessful()) {
            return response.body().string();
        }   else {
            throw new httpGetRequestException("Response failed.");
        }
    }

    class httpGetRequestException extends RuntimeException {
        public httpGetRequestException(String msg) {
            super("HttpGetRequest Exception: " + msg);
        }
    }
}
