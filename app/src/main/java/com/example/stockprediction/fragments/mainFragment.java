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
import com.example.stockprediction.objects.Stock;
import com.example.stockprediction.objects.StockRecyclerViewAdapter;
import com.example.stockprediction.utils.HttpTasksClasses.HttpTasks;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class mainFragment extends Fragment {
    private StockRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<Stock> stocksData;

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
        this.stocksData = loadStocksData(); // Loading stocks data
        adapter = new StockRecyclerViewAdapter(this.getContext(), stocksData);
        adapter.setClickListener(new StockRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d("pttt", "onItemClick: Selected item: "+position);
            }
        });
        recyclerView.setAdapter(adapter);
        addDivider();
    }
    private void addDivider(){
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this.recyclerView.getContext(),
                new LinearLayoutManager(this.getContext()).getOrientation());
        this.recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void buildStockQuery() {

    }

    private ArrayList<Stock> loadStocksData() {
        ArrayList<Stock> stocks = new ArrayList<>();
        // STUB IMPLEMENTATION
        stocks.add(new Stock("name", "symbol", 0.0, Stock.StockStatus.INCREASE, Stock.StockStatus.DECREASE, "+184.84(+0.71%)","ic_launcher_background"));
        stocks.add(new Stock("name", "symbol", 0.0, Stock.StockStatus.INCREASE, Stock.StockStatus.DECREASE, "+184.84(+0.71%)","ic_launcher_background"));
        // Load stocks data from YAHOO-API\FIREBASE

        String url="";
        HttpTasks.getInstance().httpGetRequest(url, new HttpTasks.CallBack_HttpTasks() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("pttt", "loadStocksData: data="+response);
                //Log.d("pttt", "loadStocksData: data="+stocks);
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }

            @Override
            public void onErrorResponse(JSONException error) {

            }
        });
        return stocks;
    }
}
