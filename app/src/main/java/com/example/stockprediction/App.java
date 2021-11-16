package com.example.stockprediction;

import android.app.Application;

import com.example.stockprediction.apis.RapidApi;
import com.example.stockprediction.utils.HttpTasksClasses.HttpTasks;
import com.example.stockprediction.utils.MySignal;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // App singleton-classes initiate
        MySignal.Init(this);
        HttpTasks.Init(this);
        RapidApi.Init(this);
    }
}
