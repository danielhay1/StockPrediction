package com.example.stockprediction.fragments.StockRecyclerFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.example.stockprediction.apis.RapidApi;
import com.example.stockprediction.fragments.StockFragment;
import com.example.stockprediction.objects.BaseFragment;
import com.example.stockprediction.objects.StockRecyclerViewAdapter;
import com.example.stockprediction.objects.stock.Stock;
import com.example.stockprediction.utils.MyAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StockRecyclerBaseFragment<T extends Stock> extends BaseFragment {
    protected StockRecyclerViewAdapter<T> adapter;
    protected ArrayList<T> data;
    private SearchViewModel searchViewModel;

    protected interface initStockRecyclerData_Callback<T extends Stock> {
        ArrayList<T> initRecyclerData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeModelView();
    }

    private void updateFilteredData(String text) {
        Log.e("pttt", "onTextChanged: text:"+text);
        if(adapter != null) {
            adapter.getFilter().filter(text);
        }
    }

    private void observeModelView() {
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        searchViewModel.getMessage().observe(getViewLifecycleOwner(),s -> {
            Log.d("pttt", "observed text: " + s);
            updateFilteredData(s);
        });
    }

    private void sortBy(Comparator<T> comparator) {

    }

    private StockRecyclerViewAdapter initAdapter(RecyclerView recyclerView, ArrayList<T> stocksData, StockRecyclerViewAdapter.OnStockLike_Callback onStockLike_callback) {
        Log.e("pttt", "initAdapter: favStocks = "+getUser().getFavStocks());
        adapter = new StockRecyclerViewAdapter(getContext(), stocksData, getUser().getFavStocks(), onStockLike_callback);
        adapter.setClickListener((view, position) -> {
            Log.d("pttt", "onItemClick: Selected item: "+position);
            Stock stock = stocksData.get(position);
            goToStockFragment(stock);
        });
        recyclerView.setAdapter(adapter);
        return adapter;
    }

    public void initStockRecyclerView(RecyclerView recyclerView,initStockRecyclerData_Callback<T> initRecyclerData_callback, StockRecyclerViewAdapter.OnStockLike_Callback onStockLike_callback)  {
        new MyAsyncTask().executeBgTask(() -> { //Run on background thread.
            data = initRecyclerData_callback.initRecyclerData();
            Log.e("pttt", "initStockRecyclerView: data="+data);
        },() -> { // Run on UI thread
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            adapter = initAdapter(recyclerView, data, onStockLike_callback);
            // Get stock from FireBaser

            RapidApi.getInstance().getQuotesRequest((List<Stock>) data, new RapidApi.CallBack_HttpTasks() {
                @Override
                public void onResponse(JSONObject json) {
                        Log.e("pttt", "StockJson found: "+json);

                }

                @Override
                public void onErrorResponse(VolleyError error) {
                        Log.e("pttt", "StockJson error: "+error);

                }
            });
//            for (int i = 0; i < data.size(); i++) {
//                T stock = data.get(i);
//                int finalI = i;
//                // Download stock details from YahooAPI
//                // TODO: set this code back to run
//                RapidApi.getInstance().getChartRequest(stock.getSymbol(), new RapidApi.CallBack_HttpTasks() {
//                    @Override
//                    public void onResponse(JSONObject json) {
//                        // it.remove(); // avoids a ConcurrentModificationException
//                        Log.e("pttt", "StockJson found: "+json);
//                        // TODO: figure out what data is needed for stock class and parse the relevant data.
//                        try {
//                            updateRecycleView(data,finalI,adapter,json);
//                        } catch (JSONException e) {
//                            Log.e("pttt", "initStockRecyclerView: jsonParsingError, error: "+e);
//                        }
//                    }
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e("pttt", "StockJson error: "+error);
//                    }
//                });
//            }
        });
    }

    private void updateRecycleView(ArrayList<T> stockList, int position, StockRecyclerViewAdapter<T> adapter, JSONObject json) throws JSONException { // Fix method
        // consider using "Historic Stock Prices" api
        String price = json.getString("");
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

    public void goToStockFragment(Stock stock) {
        String jsonStock = stock.stockToJson();
        StockFragment stockFragment = new StockFragment();
        Bundle bundle = new Bundle();
        bundle.putString(stockFragment.ARG_PARAM, jsonStock);
        stockFragment.setArguments(bundle);
    }
}

