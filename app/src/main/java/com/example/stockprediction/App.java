package com.example.stockprediction;

import android.app.Application;

import com.example.stockprediction.apis.RapidApi;
import com.example.stockprediction.apis.firebase.MyFireBaseServices;
import com.example.stockprediction.utils.MyPreference;
import com.example.stockprediction.utils.MySignal;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // App singleton-classes initiate
        MySignal.Init(this);
        RapidApi.Init(this);
        MyFireBaseServices.getInstance().Init();
        //MyPreference.getInstance(this).clearStockCache();
        //List<String> list = new ArrayList<String>(RapidApi.MY_STOCKS.values());
        /*RapidApi.getInstance().getChartRequest(list, new RapidApi.CallBack_HttpTasks() {
            @Override
            public void onResponse(JSONObject json) {
            }
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });*/
    }
}
