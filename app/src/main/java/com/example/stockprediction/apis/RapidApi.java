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
import java.util.HashMap;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class RapidApi {
    private  static RapidApi instance;  // Singleton Class
    private Context appContext;
    private final String HOST = "yh-finance.p.rapidapi.com";
    private static String api_key;
    //private OkHttpClient client;
    // Stock hash collection
    public static final HashMap<String,String> MY_STOCKS =  new HashMap<String,String>();
    static  {
        MY_STOCKS.put("Nvidia", "NVDA");
        MY_STOCKS.put("Intel", "INTC");
        MY_STOCKS.put("AMD", "AMD");
        MY_STOCKS.put("siemens", "SIEGY");
        MY_STOCKS.put("TSMC", "TSMC");
    }


    public enum STOCK_OPERATIONS {
        GET_HISTORICAL_DATA,
        GET_SUMMARY,
        GET_CHART
    }


    // Callback interface
    public interface CallBack_HttpTasks {
        void onResponse(Call call, JSONObject json);
        void onErrorResponse(Call call, IOException error);
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

    private String getOperationStringVal(STOCK_OPERATIONS operation) {
        String operationStringVal = "";
        switch (operation) {
            case GET_HISTORICAL_DATA:
                operationStringVal = "/stock/v2/get-summary";
            case GET_SUMMARY:
                operationStringVal = "/stock/v3/get-historical-data";
            case GET_CHART:
                operationStringVal = "/market/get-charts";
        }
        return operationStringVal;
    }

    private void getHistoricalData(String symbol) {
        this.httpGetJson(symbol, STOCK_OPERATIONS.GET_HISTORICAL_DATA, new CallBack_HttpTasks() {
            @Override
            public void onResponse(Call call, JSONObject json) {
                /**
                 * send response to activity
                 */
                Log.d("pttt", "onResponse: "+ json);
            }

            @Override
            public void onErrorResponse(Call call, IOException error) {
                Log.e("pttt", "httpGetRequest: Response Failed!");
                error.printStackTrace();
            }
        });
        // Convert json to stock
        Stock.JsonToStock("");
    }

    private String rapidUrlBuilder(String operation, String symbol, String interval, String range, String region) {
        /**
         *  Optional symbols -> this.MY_STOCKS
         *  Optional operations -> this.STOCK_OPERATIONS
         *  Optional regions -> US|
         *  Optional intervals -> intValue m=min|h=hour|d=day|w=week
         *  Optional range -> intValue m=min|h=hour|d=day|w=week
         */
        String apiUrl = "https://yh-finance.p.rapidapi.com";
        String url = apiUrl + operation + "?symbol=" + symbol + "&interval="+ interval + "&range="+ range + "&region=" + region;
        return url;
    }
    public void httpGetJson(String symbol, STOCK_OPERATIONS operation, CallBack_HttpTasks callBack_httpTasks) {
        String url = this.rapidUrlBuilder(getOperationStringVal(operation),symbol,"10m","1w","US");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-host", this.HOST)
                .addHeader("x-rapidapi-key", this.api_key)
                .build();

        //Response response = client.newCall(request).execute();
        //if(response.isSuccessful()) {
        //            return response.body().string();
        //        }   else {
        //            throw new httpGetRequestException("Response failed.");
        //        }
        //return call;
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack_httpTasks.onErrorResponse(call,e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
                    String jsonData = response.body().string();
                    try {
                        JSONObject Jobject = new JSONObject(jsonData);
                        callBack_httpTasks.onResponse(call, Jobject);
                    } catch (JSONException e) {
                        callBack_httpTasks.onErrorResponse(call,new httpGetRequestException(e.getMessage()));
                    }
                }   else {
                    callBack_httpTasks.onErrorResponse(call,new httpGetRequestException("Response failed."));
                }
            }
        });
    }



    class httpGetRequestException extends IOException {
        public httpGetRequestException(String msg) {
            super("HttpGetRequest Exception: " + msg);
        }
    }
}
