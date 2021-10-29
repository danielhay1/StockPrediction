package com.example.stockprediction.utils.HttpTasksClasses;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request.Method;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class HttpTasks {
    private  static HttpTasks instance;
    private Context appContext;
    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;
    private static JSONObject requestedObject;
    private CallBack_HttpTasks callBack_httpTasks;

    public interface CallBack_HttpTasks {
        void onResponse(JSONObject response);
        void onErrorResponse(VolleyError error);
        void onErrorResponse(JSONException error);

    }

    public static HttpTasks getInstance() {
        return instance;
    }

    private HttpTasks(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static void Init(Context appContext){
        if(instance == null) {
            Log.d("pttt", "Init: HttpTasks");
            instance = new HttpTasks(appContext);
            mRequestQueue = Volley.newRequestQueue(appContext);
            int memClass = ((ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE))
                    .getMemoryClass();
            int cacheSize = 1024 * 1024 * memClass / 8;
            mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache(cacheSize));
        }
    }

    private static RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    /**
     * Returns instance of ImageLoader initialized with {@see FakeImageCache} which effectively means
     * that no memory caching is used. This is useful for images that you know that will be show
     * only once.
     *
     * @return
     */
    private static ImageLoader getImageLoader() {
        if (mImageLoader != null) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }

    private void setCallBack(CallBack_HttpTasks callBack_httpTasks) {
        this.callBack_httpTasks = callBack_httpTasks;
    }

    // PUBLIC METHODS:
    public void httpGetRequest(String url, CallBack_HttpTasks callBack_httpTasks) {
        this.setCallBack(callBack_httpTasks);
        RequestQueue queue = this.getRequestQueue();
        JsonObjectRequest myReq = new JsonObjectRequest(Method.GET, url,
                null,
                createMyReqSuccessListener(),
                createMyReqErrorListener());

        queue.add(myReq);
    }

/*    public void stop() {
        if (this.mRequestQueue != null) {
            mRequestQueue.cancelAll();
        }
    }*/
    // CALLBACKS:
    private Response.Listener<JSONObject> createMyReqSuccessListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("pttt", "onResponse: " + response.getString("one"));
                    callBack_httpTasks.onResponse(response);
                } catch (JSONException e) {
                    Log.e("pttt", "onResponse: Parse error");
                    callBack_httpTasks.onErrorResponse(e);
                }
            }
        };
    }


    private Response.ErrorListener createMyReqErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("pttt", "onErrorResponse: "+ error.getMessage());
                callBack_httpTasks.onErrorResponse(error);
            }
        };
    }

}
