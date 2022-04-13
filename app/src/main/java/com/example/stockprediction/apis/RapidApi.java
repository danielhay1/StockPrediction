package com.example.stockprediction.apis;


import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.stockprediction.R;
import com.example.stockprediction.objects.stock.Stock;
import com.example.stockprediction.utils.HttpServices.HttpRequestQueue;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;


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
        GET_CHART,
        GET_QUOTES
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
            Log.d("rapid_api", "Init: RapidApi");
            instance = new RapidApi(context);
        }
    }

    private String getOperationStringVal(STOCK_OPERATIONS operation) {
        String operationStringVal = "";
        String strParams = "";
        switch (operation) {
            case GET_HISTORICAL_DATA:
                operationStringVal = "/stock/v2/get-summary";
                break;
            case GET_SUMMARY:
                operationStringVal = "/stock/v3/get-historical-data";
                break;
            case GET_CHART:
                operationStringVal = "/market/get-charts";
                break;
            case GET_QUOTES:
                operationStringVal = "/market/v2/get-quotes";
                break;
        }
        return operationStringVal;
    }


    private void getDataFromCache(String key) {

    }

    private void storeJsonInCache(String key,JSONObject json) {

    }

    private void getHistoricalData(String symbol) {
        httpGetRequest(symbol, STOCK_OPERATIONS.GET_HISTORICAL_DATA, new CallBack_HttpTasks() {
            @Override
            public void onResponse(JSONObject json) {
                /**
                 * send response to activity
                 */
                Log.d("rapid_api", "onResponse: "+ json);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("rapid_api", "httpGetRequest: VolleyError: "+error);
                error.printStackTrace();
            }
        });
        // TODO: Convert json to stock
    }

    private String buildURL(String operation, String symbol, String interval, String range, String region) {
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

    private String buildURL(String operation, String symbol, String region) {
        /**
         *  Optional symbols -> this.MY_STOCKS
         *  Optional operations -> this.STOCK_OPERATIONS
         *  Optional regions -> US|
         *  Optional intervals -> intValue m=min|h=hour|d=day|w=week
         *  Optional range -> intValue m=min|h=hour|d=day|w=week
         */
        String apiUrl = "https://yh-finance.p.rapidapi.com";
        String url = apiUrl + operation + "?region=" + region+ "&symbols=" + symbol;
        return url;
    }

    public void getQuotesRequest(List<Stock> stocks, CallBack_HttpTasks callBack_httpTasks) {
        StringJoiner strSymbols = new StringJoiner("%2C");
        for (Stock stock: stocks) {
            strSymbols.add(stock.getSymbol().toUpperCase());
        }
        httpGetRequest(strSymbols.toString(), STOCK_OPERATIONS.GET_QUOTES, callBack_httpTasks);
    }

    public void getChartRequest(String symbol,  CallBack_HttpTasks callBack_httpTasks) {
        httpGetRequest(symbol, STOCK_OPERATIONS.GET_CHART, callBack_httpTasks);
    }

    private void httpGetRequest(String symbol, STOCK_OPERATIONS operation, CallBack_HttpTasks callBack_httpTasks) {
        String url = "";
        switch (operation) {
            case GET_CHART:
                url = this.buildURL(getOperationStringVal(operation),symbol,"1d","5d","US");
                break;
            case GET_QUOTES:
                url = this.buildURL(getOperationStringVal(operation),symbol,"US");
                break;
        }
        Log.d("rapid_api", "httpGetJson: url= "+url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (com.android.volley.Request.Method.GET, url, null, response -> {
                    Log.d("rapid_api", "onResponse: "+ response);
                    callBack_httpTasks.onResponse(response);

                }, error -> {
                    // TODO: Handle error
                    Log.d("rapid_api", "onResponse: VolleyError: "+ error);
                    callBack_httpTasks.onErrorResponse(error);
                })  {
            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-RapidAPI-Host", HOST);
                headers.put("X-RapidAPI-Key", api_key);
                return headers;
            }
        };
        httpRequestQueue.addToRequestQueue(jsonObjectRequest);
    }
}
