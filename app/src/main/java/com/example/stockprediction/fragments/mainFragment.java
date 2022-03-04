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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

interface DataReadyCallback
{
    void dataReady(ArrayList data, int position);
}

public class mainFragment extends Fragment {
    private StockRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<Stock> stocksData = new ArrayList<>();
    private Fragment stockFragment;

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

    private StockRecyclerViewAdapter initAdapter(RecyclerView recyclerView, ArrayList<Stock> stocksData) {
        adapter = new StockRecyclerViewAdapter(getContext(), stocksData);
        adapter.setClickListener(new StockRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d("pttt", "onItemClick: Selected item: "+position);
                Stock stock = stocksData.get(position);
                goToStockFragment(stock);
            }
        });
        recyclerView.setAdapter(adapter);
        return adapter;
    }

    private void initStockRecyclerView()  {
        for (Map.Entry<String, String> entry : RapidApi.MY_STOCKS.entrySet()) {
            stocksData.add(new Stock(entry.getKey(),entry.getValue()));
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        adapter = initAdapter(recyclerView, stocksData);

        new Thread() {

        };
        for (int i = 0; i < stocksData.size(); i++) {
            Stock stock = stocksData.get(i);
            int finalI = i;
            RapidApi.getInstance().httpGetJson(stock.getSymbol(), RapidApi.STOCK_OPERATIONS.GET_CHART, new RapidApi.CallBack_HttpTasks() {
                @Override
                public void onResponse(JSONObject json) {
                    // it.remove(); // avoids a ConcurrentModificationException
                    Log.e("pttt", "StockJson found: "+json);
                    // TODO: figure out what data is needed for stock class and parse the relevant data.
                    updateRecycleView(stocksData,finalI,adapter);
                }
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("pttt", "StockJson error: "+error);
                }
            });
        }
    }

    private void updateRecycleView(ArrayList<Stock> stockList, int position, StockRecyclerViewAdapter adapter) { // Fix method
        stockList.get(position)
                .setPredictionStatus()
                .setValue(10.0);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemChanged(position);
            }
        });
    }

    private void addDivider(){
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this.recyclerView.getContext(),
                new LinearLayoutManager(this.getContext()).getOrientation());
        this.recyclerView.addItemDecoration(dividerItemDecoration);
    }

    public void goToStockFragment(Stock stock) {
//        getActivity().getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container, (Fragment) stockFragment, "findThisFragment")
//                .addToBackStack(null)  
//                .commit();
        String jsonStock = stock.stockToJson();
        stockFragment stockFragment = new stockFragment();
        Bundle bundle = new Bundle();
        bundle.putString(stockFragment.ARG_PARAM, jsonStock);
        stockFragment.setArguments(bundle);
    }
}
