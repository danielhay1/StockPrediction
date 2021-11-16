package com.example.stockprediction.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.example.stockprediction.R;
import com.example.stockprediction.apis.RapidApi;
import com.example.stockprediction.objects.Stock;
import com.example.stockprediction.objects.StockRecyclerViewAdapter;
import com.example.stockprediction.utils.HttpTasksClasses.HttpTasks;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;

public class mainFragment extends Fragment {
    private StockRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<Stock> stocksData;

    private interface DataReadyCallback
    {
        void dataReady(ArrayList data);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        findViews(view);
        initViews();
        return view;
    }

    private void findViews(View view) {
        this.recyclerView = view.findViewById(R.id.main_RV_stocks);
    }

    private void initViews() {
        initStockRecyclerView();
    }

    private void initStockRecyclerView()  {
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        loadStocksData(new DataReadyCallback() {
            @Override
            public void dataReady(ArrayList data) {
                stocksData = data;
                Log.d("pttt", "dataReady: "+data);
//                adapter = new StockRecyclerViewAdapter(getContext(), stocksData);
//                adapter.setClickListener(new StockRecyclerViewAdapter.ItemClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
//                        Log.d("pttt", "onItemClick: Selected item: "+position);
//                    }
//                });
//                recyclerView.setAdapter(adapter);
//                addDivider();
            }
        }); // Loading stocks data
    }
    private void addDivider(){
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this.recyclerView.getContext(),
                new LinearLayoutManager(this.getContext()).getOrientation());
        this.recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void buildStockQuery() {

    }

    private void loadStocksData(DataReadyCallback dataReadyCallback) {
        ArrayList<Stock> stocks = new ArrayList<>();
        // Option1:
        // Load stocks data from YAHOO-API\FIREBASE
        Iterator it = RapidApi.MY_STOCKS.entrySet().iterator();
        boolean islastElement = false;
        while (it.hasNext()) {
            // x -> y -> null
            Map.Entry<String,String> pair = (Map.Entry<String,String>)it.next();
            if(!it.hasNext()) { // Check if this is the last element.
                islastElement = true;
            }
            boolean finalIslastElement = islastElement;
            RapidApi.getInstance().httpGetJson(pair.getValue(), RapidApi.STOCK_OPERATIONS.GET_CHART, new RapidApi.CallBack_HttpTasks() {
                @Override
                public void onResponse(Call call, JSONObject json) {
                    // it.remove(); // avoids a ConcurrentModificationException
                    Log.e("pttt", "StockJson: "+json);
                    stocks.add(new Stock(pair.getKey(),pair.getValue(), 0.0, Stock.StockStatus.INCREASE, Stock.StockStatus.DECREASE, "+184.84(+0.71%)","ic_launcher_background"));
                    //Iterator temp = (Iterator) it.next();
                   if(finalIslastElement) {
                       Log.d("pttt", "onResponse: ************: "+ pair);
                       dataReadyCallback.dataReady(stocks);
                   }
                }

                @Override
                public void onErrorResponse(Call call, IOException error) {
                    Log.e("pttt", "StockJson: "+error);
                }
            });
        }
       /* // Option2:
        // Load stocks data from YAHOO-API\FIREBASE
        for (Map.Entry<String, String> entry : RapidApi.MY_STOCKS.entrySet()) {
            RapidApi.getInstance().httpGetJson(entry.getValue(), RapidApi.STOCK_OPERATIONS.GET_CHART, new RapidApi.CallBack_HttpTasks() {
                @Override
                public void onResponse(Call call, JSONObject json) {
                    Log.e("pttt", "StockJson: "+json);
                    stocks.add(new Stock(entry.getKey(), entry.getValue(), 0.0, Stock.StockStatus.INCREASE, Stock.StockStatus.DECREASE, "+184.84(+0.71%)","ic_launcher_background"));

                }

                @Override
                public void onErrorResponse(Call call, IOException error) {
                    Log.e("pttt", "StockJson: "+error);
                }
            });
        }*/
       /* // STUB IMPLEMENTATION
        RapidApi.getInstance().httpGetJson("NVDA", RapidApi.STOCK_OPERATIONS.GET_CHART, new RapidApi.CallBack_HttpTasks() {
            @Override
            public void onResponse(Call call, JSONObject json) {
                Log.e("pttt", "StockJson: "+json);
            }

            @Override
            public void onErrorResponse(Call call, IOException error) {
                Log.e("pttt", "StockJson: "+error);
            }
        });

        stocks.add(new Stock("name", "NVDA", 0.0, Stock.StockStatus.INCREASE, Stock.StockStatus.DECREASE, "+184.84(+0.71%)","ic_launcher_background"));
        stocks.add(new Stock("name", "NVDA", 0.0, Stock.StockStatus.INCREASE, Stock.StockStatus.DECREASE, "+184.84(+0.71%)","ic_launcher_background"));*/
    }
}
