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
        MY_STOCKS.put("Nvidia", "NVDA");
        MY_STOCKS.put("Intel", "INTC");
        //MY_STOCKS.put("Advanced Micro Devices", "AMD");
        MY_STOCKS.put("Siemens", "SIEGY");
        MY_STOCKS.put("Alphabet", "GOOG");
        MY_STOCKS.put("Meta Platform", "FB");
        MY_STOCKS.put("Microsoft Corporation", "MSFT");
        MY_STOCKS.put("Apple", "AAPL");

        //MY_STOCKS.put("TSMC", "TSMC");
    }


    public enum STOCK_OPERATION {
        GET_HISTORICAL_DATA,
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
            case GET_HISTORICAL_DATA:
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

    private void  generateQuotesHttpRequest(List<Stock> stocks, CallBack_HttpTasks callBack_httpTasks) {
        StringJoiner strSymbols = new StringJoiner("%2C");
        for (Stock stock: stocks) {
            strSymbols.add(stock.getSymbol().toUpperCase());
        }
        Log.d("rapid_api", "strSymbols= "+strSymbols.toString());
        httpGetRequest(strSymbols.toString(), STOCK_OPERATION.GET_QUOTES, MyPreference.StockCacheManager.CACHE_KEYS.STOCKS_DATA_JSON, callBack_httpTasks);
    }

    private void intervalHTTPRequest(List<String> symbols, STOCK_OPERATION operation, String cacheKey, CallBack_HttpTasks callBack_httpTasks) {
        /**
         * Method Requests ALLOWED_REQUEST_PER_SEC API requests then sleep for 1 sec.
         * Method handles API constraints.
         */
        final int ALLOWED_REQUEST_PER_SEC = 5;
        for (int i = 0; i <symbols.size() ; i+=ALLOWED_REQUEST_PER_SEC) {
            int finalI = i;
            new MyAsyncTask().executeDelayBgTask(() -> { // Sleep 1 sec then generate 5 API requests per sec
                int index = finalI;
                String stockSymbol = symbols.get(index);
                int requestCounter = 0;
                while (requestCounter<ALLOWED_REQUEST_PER_SEC && index < symbols.size()) { // Generates 5 API requests
                    httpGetRequest(stockSymbol, operation, cacheKey, new CallBack_HttpTasks() {
                        @Override
                        public void onResponse(JSONObject json) {
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("rapid_api", "intervalHTTPRequest: error= "+error);
                        }
                    });
                    requestCounter++;
                    index++;
                }
            }, (long) 1.0);
        }
    }

    public void getChartRequestCahceOnly(List<Stock> stocks, CallBack_HttpTasks callBack_httpTasks) {
        try {
            JSONObject jsonObject = MyPreference.getInstance(appContext).getStocksData(MyPreference.StockCacheManager.CACHE_KEYS.CHARTS_DATA_JSON); // could throw null pointer exception in case of no data in cache
            Log.d("rapid_api", "getting json from cache: json= "+jsonObject);
            callBack_httpTasks.onResponse(jsonObject);
        } catch (NullPointerException e) {
            Log.e("rapid_api", "getQuotesRequest:  error= "+e.getLocalizedMessage());
        }
    }

    public void getChartRequest(List<String> symbols, CallBack_HttpTasks callBack_httpTasks) {
        intervalRequest(symbols, STOCK_OPERATION.GET_CHART, callBack_httpTasks);
    }

    public void getHistoricalDataRequest(List<String> symbols, CallBack_HttpTasks callBack_httpTasks) {
        intervalRequest(symbols, STOCK_OPERATION.GET_HISTORICAL_DATA, callBack_httpTasks);
        // TODO: Convert json to stock
    }

    private synchronized void intervalRequest(List<String> symbols, STOCK_OPERATION operation, CallBack_HttpTasks callBack_httpTasks) {
        int refreshInterval = 0;
        String cacheKey = "";
        switch (operation) {
            case GET_HISTORICAL_DATA:
                refreshInterval = MyPreference.StockCacheManager.REFRESH_INTERVAL.HISTORICAL_DATA;
                cacheKey = MyPreference.StockCacheManager.CACHE_KEYS.HISTORICAL_DATA_JSON;
                break;
            case GET_CHART:
                refreshInterval = MyPreference.StockCacheManager.REFRESH_INTERVAL.CHARTS_DATA;
                cacheKey = MyPreference.StockCacheManager.CACHE_KEYS.CHARTS_DATA_JSON;
                break;
        }
        try {
            JSONObject jsonObject = MyPreference.getInstance(appContext).getStocksData(cacheKey); // could throw null pointer exception in case of no data in cache
            if(!MyPreference.StockCacheManager.shouldRefreshCache(jsonObject,refreshInterval)) {
                Log.d("rapid_api", "getting json from cache: json= "+jsonObject);
                callBack_httpTasks.onResponse(jsonObject);
            } else {
                intervalHTTPRequest(symbols, operation, cacheKey, new CallBack_HttpTasks() {
                    @Override
                    public void onResponse(JSONObject json) {
                        //MyPreference.getInstance(appContext).addJsonToStocksData()
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("rapid_api", "intervalRequest:  error= "+error.getLocalizedMessage());

                    }
                });

            }
        } catch (NullPointerException e) {
            Log.e("rapid_api", "intervalRequest:  error= "+e.getLocalizedMessage());
            intervalHTTPRequest(symbols, operation,cacheKey,callBack_httpTasks);
        } catch (JSONException e) {
            Log.e("rapid_api", "intervalRequest:  error= "+e.getLocalizedMessage());
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
            case GET_HISTORICAL_DATA:
            case GET_QUOTES:
                url = this.buildURL(getOperationStringVal(operation),symbol,"US");
                break;
        }
        Log.d("rapid_api", "httpGetJson: url= "+url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (com.android.volley.Request.Method.GET, url, null, response -> {
                    try {
                        JSONObject myResponse =  generateCustomJsonQuotes(response,operation);
                        if(operation == STOCK_OPERATION.GET_QUOTES) {
                            myResponse = MyPreference.getInstance(appContext).putStocksData(myResponse,"stocks",cacheKey);
                        } else {
                            myResponse = MyPreference.getInstance(appContext).addJsonToStocksData(myResponse,"stocks",cacheKey);

                        }
                        Log.d("rapid_api", "onResponse: "+ myResponse);
                        callBack_httpTasks.onResponse(myResponse);
                    } catch (JSONException e) {
                        Log.e("rapid_api", "httpGetRequest:  error= "+e.getLocalizedMessage());
                    }
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

    private JSONObject generateCustomJsonQuotes(JSONObject apiResponse, STOCK_OPERATION operation) throws JSONException {
        JSONObject stocks = new JSONObject();

        switch (operation) {
            case GET_CHART:
                // TODO: IMPLEMENT CUSTOM JSON
                JSONObject result = apiResponse.getJSONObject("chart").getJSONArray("result").getJSONObject(0);
                JSONObject stock = new JSONObject(); // create stock json
                stock.put("timestamp", result.getJSONArray("timestamp"));
                stock.put("values", result.getJSONObject("indicators").getJSONArray("quote").getJSONObject(0).getJSONArray("close"));
                stocks.put(result.getJSONObject("meta").getString("symbol"),stock);

                break;
                // TODO: IMPLEMENT CUSTOM JSON
            case GET_HISTORICAL_DATA:

                // TODO: IMPLEMENT CUSTOM JSON
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
