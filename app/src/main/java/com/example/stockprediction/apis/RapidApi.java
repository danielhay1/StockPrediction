package com.example.stockprediction.apis;


import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.stockprediction.R;
import com.example.stockprediction.objects.stock.Stock;
import com.example.stockprediction.utils.HttpServices.HttpRequestQueue;
import com.example.stockprediction.utils.MyAsyncTask;
import com.example.stockprediction.utils.MyPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;


public class RapidApi {
    /**
     * Constraints:
     * - API response only up to 5 requests per sec.
     * - Free version of API allow 500 requests per month.
     */
    private static RapidApi instance;  // Singleton Class
    private Context appContext;
    private final String HOST = "yh-finance.p.rapidapi.com";
    private static String api_key;
    private HttpRequestQueue httpRequestQueue;
    private static final Object lock = new Object();


    //private OkHttpClient client;
    // Stock hash collection
    public static final HashMap<String,String> MY_STOCKS =  new HashMap<String,String>();
    static  {
        MY_STOCKS.put("Apple", "AAPL");
        MY_STOCKS.put("Google", "GOOG");
        MY_STOCKS.put("Microsoft", "MSFT");
        MY_STOCKS.put("Meta", "FB");
        MY_STOCKS.put("Nvidia", "NVDA");
        MY_STOCKS.put("Amazon", "AMZN");
        MY_STOCKS.put("Verizon", "VZ");
        MY_STOCKS.put("Cisco", "CSCO");
        MY_STOCKS.put("Intel", "INTC");
        MY_STOCKS.put("T-Mobile", "TMUS");
        MY_STOCKS.put("Qualcomm", "QCOM");
        MY_STOCKS.put("at&t", "T");
        MY_STOCKS.put("Sony", "SONY");
        MY_STOCKS.put("Hp", "HPQ");
        MY_STOCKS.put("Motorola", "MSI");
        MY_STOCKS.put("Twitter", "TWTR");
        MY_STOCKS.put("Dell", "DELL");
        MY_STOCKS.put("Amdocs", "DOX");








        //MY_STOCKS.put("Advanced Micro Devices", "AMD");
        //MY_STOCKS.put("Siemens", "SIEGY");


        //MY_STOCKS.put("TSMC", "TSMC");
    }


    public enum STOCK_OPERATION {
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
            synchronized (lock) {
                Log.d("rapid_api", "Init: RapidApi");
                instance = new RapidApi(context);
            }
        }
    }

    private String getOperationStringVal(STOCK_OPERATION operation) {
        String operationStringVal = "";
        String strParams = "";
        switch (operation) {
            case GET_CHART:
                operationStringVal = "/stock/v3/get-chart";
                break;
            case GET_QUOTES:
                operationStringVal = "/market/v2/get-quotes";
                break;
        }
        return operationStringVal;
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

    private void generateQuotesHttpRequest(List<Stock> stocks, CallBack_HttpTasks callBack_httpTasks) {
        StringJoiner strSymbols = new StringJoiner("%2C");
        for (Stock stock: stocks) {
            strSymbols.add(stock.getSymbol().toUpperCase());
        }
        Log.d("rapid_api", "strSymbols= "+strSymbols.toString());
        httpGetRequest(strSymbols.toString(), STOCK_OPERATION.GET_QUOTES, MyPreference.StockCacheManager.CACHE_KEYS.STOCKS_DATA_JSON, callBack_httpTasks);
    }

    public synchronized void getChartRequest(List<Stock> stocks, CallBack_HttpTasks callBack_httpTasks) {
        //TODO: fix run for each stock
        int refreshInterval = MyPreference.StockCacheManager.REFRESH_INTERVAL.CHARTS_DATA;
        String cacheKey = MyPreference.StockCacheManager.CACHE_KEYS.CHARTS_DATA_JSON;
        for (int i = 0; i < stocks.size(); i ++) {
            int index = i;
            String stockSymbol = stocks.get(index).getSymbol();
            try {
                JSONObject jsonObject = MyPreference.getInstance(appContext).getStocksData(cacheKey + stockSymbol); // could throw null pointer exception in case of no data in cache
                if (!MyPreference.StockCacheManager.shouldRefreshCache(jsonObject, refreshInterval)) {
                    Log.d("rapid_api", "getting json from cache: json= " + jsonObject);
                    callBack_httpTasks.onResponse(jsonObject);
                } else {
                    new MyAsyncTask().executeBgTask(() -> { // Sleep 1 sec then generate ALLOWED_REQUEST_PER_SEC API requests per sec
                        httpGetRequest(stockSymbol, STOCK_OPERATION.GET_CHART, cacheKey + stockSymbol, callBack_httpTasks);
                    });
                }
            } catch (NullPointerException e) {
                Log.e("rapid_api", "intervalRequest:  error= " + e.getLocalizedMessage());
                new MyAsyncTask().executeBgTask(() -> { // Sleep 1 sec then generate ALLOWED_REQUEST_PER_SEC API requests per sec
                    httpGetRequest(stockSymbol, STOCK_OPERATION.GET_CHART, cacheKey + stockSymbol, callBack_httpTasks);
                });
            } catch (JSONException e) {
                Log.e("rapid_api", "intervalRequest:  symbol: " + stockSymbol + "not exists in cache");
            }
        }
    }

    public void getQuotesRequest(List<Stock> stocks, CallBack_HttpTasks callBack_httpTasks) {
        try {
            JSONObject jsonObject = MyPreference.getInstance(appContext).getStocksData(MyPreference.StockCacheManager.CACHE_KEYS.STOCKS_DATA_JSON); // could throw null pointer exception in case of no data in cache
            if(!MyPreference.StockCacheManager.shouldRefreshCache(jsonObject,MyPreference.StockCacheManager.REFRESH_INTERVAL.STOCK_DATA)) {
                Log.d("rapid_api", "getting json from cache: json= "+jsonObject);
                callBack_httpTasks.onResponse(jsonObject);
            } else {
                generateQuotesHttpRequest(stocks,callBack_httpTasks);
            }
        } catch (NullPointerException e) {
            Log.e("rapid_api", "getQuotesRequest:  error= "+e.getLocalizedMessage());
            generateQuotesHttpRequest(stocks,callBack_httpTasks);
        }  catch (JSONException e) {
            Log.e("rapid_api", "getQuotesRequest:  error= "+e.getLocalizedMessage());
        }
    }
    public void getQuotesRequestCacheOnly(CallBack_HttpTasks callBack_httpTasks) {
        try {
            JSONObject jsonObject = MyPreference.getInstance(appContext).getStocksData(MyPreference.StockCacheManager.CACHE_KEYS.STOCKS_DATA_JSON); // could throw null pointer exception in case of no data in cache
            Log.d("rapid_api", "getting json from cache: json= "+jsonObject);
            callBack_httpTasks.onResponse(jsonObject);
        } catch (NullPointerException e) {
            Log.e("rapid_api", "getQuotesRequest:  error= "+e.getLocalizedMessage());
        }
    }

    private void httpGetRequest(String symbol, STOCK_OPERATION operation, String cacheKey, CallBack_HttpTasks callBack_httpTasks) {
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
                    try {
                        JSONObject myResponse =  generateCustomJsonQuotes(response,operation);
                        myResponse = MyPreference.getInstance(appContext).putStocksData(myResponse,"stocks",cacheKey);
                        Log.d("rapid_api", "onResponse: "+ myResponse);
                        callBack_httpTasks.onResponse(myResponse);
                    } catch (JSONException e) {
                        Log.e("rapid_api", "httpGetRequest:  error= "+e.getLocalizedMessage());
                    }
                }, error -> {
                    // TODO: Handle error
                    if(error.networkResponse.statusCode == 429 && operation == STOCK_OPERATION.GET_CHART) {
                        Log.d("rapid_api", "onResponse: VolleyError = 429, retrying request");
                        new MyAsyncTask().executeDelayBgTask(() -> { // Sleep 1 sec then generate ALLOWED_REQUEST_PER_SEC API requests per sec
                            httpGetRequest(symbol, STOCK_OPERATION.GET_CHART, cacheKey + symbol, callBack_httpTasks);
                        }, 1000);
                    } else {
                        Log.d("rapid_api", "onResponse: VolleyError: "+ error);
                        callBack_httpTasks.onErrorResponse(error);
                    }
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

    private JSONObject generateCustomJsonQuotes(JSONObject apiResponse, STOCK_OPERATION operation) throws JSONException {
        JSONObject stocks = new JSONObject();
        switch (operation) {
            case GET_CHART:
                JSONObject result = apiResponse.getJSONObject("chart").getJSONArray("result").getJSONObject(0);
                JSONObject stock = new JSONObject(); // create stock json
                stock.put("timestamp", result.getJSONArray("timestamp"));
                stock.put("values", result.getJSONObject("indicators").getJSONArray("quote").getJSONObject(0).getJSONArray("close"));
                stocks.put("symbol",result.getJSONObject("meta").getString("symbol"));
                stocks.put(result.getJSONObject("meta").getString("symbol"),stock);
                break;
            case GET_QUOTES:
                for (int i = 0; i < RapidApi.MY_STOCKS.size(); i++) {
                    result = apiResponse.getJSONObject("quoteResponse").getJSONArray("result").getJSONObject(i);
                    stock = new JSONObject(); // create stock json
                    stock.put("regularMarketChange",result.getString("regularMarketChange"));
                    stock.put("regularMarketPrice",result.getString("regularMarketPrice"));
                    stock.put("regularMarketChangePercent",result.getString("regularMarketChangePercent"));
                    stocks.put(result.getString("symbol"),stock);
                }
                break;
        }
        return stocks;
    }

}
