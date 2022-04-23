package com.example.stockprediction.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.example.stockprediction.R;
import com.example.stockprediction.apis.RapidApi;
import com.example.stockprediction.objects.BaseFragment;
import com.example.stockprediction.objects.stock.Stock;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class StockFragment extends BaseFragment {

    public static final String ARG_PARAM = "stock";
    private Stock stock;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Gson gson = new Gson();
            String jsonStock = getArguments().getString(ARG_PARAM);
            stock = gson.fromJson(jsonStock, Stock.class);
            RapidApi.getInstance().getQuotesRequestCacheOnly(new RapidApi.CallBack_HttpTasks() {
                @Override
                public void onResponse(JSONObject json) {
                    try {
                        updateDataFromAPI(json);
                    } catch (JSONException e) {
                        Log.e("StockFragment", "onCreate: stockUpdateFromAPI error= "+e);
                    }
                }
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            Log.d("StockFragment", "onCreate: stock = ");

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stock, container, false);
        findViews(view);
        return view;
    }

    private void updateDataFromAPI(JSONObject json) throws JSONException {
        JSONObject result = json.getJSONObject("stocks").getJSONObject(stock.getSymbol().toUpperCase());
        stock.setValue(Double.parseDouble(result.getString("regularMarketPrice")));
        stock.setChangeAmount(Double.parseDouble(result.getString("regularMarketChange")));
        stock.setChangePercent(Double.parseDouble(result.getString("regularMarketChangePercent")));
        stock.setPredictionStatus();
    }

    private void findViews(View view) {

    }

    private void initViews() {

    }

}