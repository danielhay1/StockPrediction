package com.example.stockprediction.apis;


import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.stockprediction.R;
import com.example.stockprediction.utils.HttpServices.HttpRequestQueue;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;



public class RapidApi {
    private  static RapidApi instance;  // Singleton Class
    private Context appContext;
    private final String HOST = "yh-finance.p.rapidapi.com";
    private static String api_key;
    private HttpRequestQueue httpRequestQueue;


    //private OkHttpClient client;
    // Stock hash collection
    public static final HashMap<String,String> MY_STOCKS =  new HashMap<String,String>();
    static  {
        MY_STOCKS.put("Nvidia", "NVDA");
        MY_STOCKS.put("Intel", "INTC");
        MY_STOCKS.put("AMD", "AMD");
        MY_STOCKS.put("Siemens", "SIEGY");
        MY_STOCKS.put("TSMC", "TSMC");
    }


    public enum STOCK_OPERATIONS {
        GET_HISTORICAL_DATA,
        GET_SUMMARY,
        GET_CHART
    }


    // Callback interface
    public interface CallBack_HttpTasks {
        void onResponse(JSONObject json);
        void onErrorResponse(VolleyError error);
    }


    public static RapidApi getInstance() {
        return instance;
    }

    private RapidApi(Context context) {
        this.appContext = context.getApplicationContext();
        this.api_key = this.appContext.getString(R.string.rapidapi);
        httpRequestQueue = HttpRequestQueue.getInstance(appContext);
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

    private void getDataFromCache(String key) {

    }

    private void getHistoricalData(String symbol) {
        httpGetJson(symbol, STOCK_OPERATIONS.GET_HISTORICAL_DATA, new CallBack_HttpTasks() {
            @Override
            public void onResponse(JSONObject json) {
                /**
                 * send response to activity
                 */
                Log.d("pttt", "onResponse: "+ json);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("pttt", "httpGetRequest: VolleyError: "+error);
                error.printStackTrace();
            }
        });
        // TODO: Convert json to stock
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
        String url = this.rapidUrlBuilder(getOperationStringVal(operation),symbol,"10m","5d","US");
        Log.d("rapid_api", "httpGetJson: url= "+url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (com.android.volley.Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("pttt", "onResponse: "+ response);
                        callBack_httpTasks.onResponse(response);

                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("pttt", "onResponse: VolleyError: "+ error);
                        callBack_httpTasks.onErrorResponse(error);

                    }
                })  {
            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("x-rapidapi-host", HOST);
                headers.put("x-rapidapi-key", api_key);
                headers.put("key", "Value");
                return headers;
            }
        };
        httpRequestQueue.addToRequestQueue(jsonObjectRequest);
    }
}
