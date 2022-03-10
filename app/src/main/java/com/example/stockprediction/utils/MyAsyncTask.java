package com.example.stockprediction.utils;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MyAsyncTask {
    public interface OnCompleteCallback {
        void onComplete();
    }

    private final Executor executor = Executors.newSingleThreadExecutor(); // change according to your requirements
    private final Handler handler = new Handler(Looper.getMainLooper());

    public void executeBgTask(Runnable runOnBackground) {
        /**
         * Usage example:
         *         new MyAsyncTask().executeBgTask(() -> { //Run on background thread.
         *             user = getUserFromActivity();
         *         });
         */
        executor.execute(runOnBackground);
    }

    public void executeBgTask(Runnable runOnBackground, OnCompleteCallback onCompleteCallback) {
        /**
         * Usage example:
         *         new MyAsyncTask().executeBgTask(() -> { //Run on background thread.
         *             user = getUserFromActivity();
         *         },() -> { // Run on UI thread
         *
         *         });
         */
        executor.execute(new Runnable() {
            @Override
            public void run() {
                executor.execute(runOnBackground); // runOnBackgroundThread
                handler.post(() -> {    // runOnUiThread
                    onCompleteCallback.onComplete();
                });
            }
        });
    }


}
