
package com.example.stockprediction.fragments.StockRecyclerFragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.example.stockprediction.R;
import com.example.stockprediction.activites.MainActivity;
import com.example.stockprediction.apis.RapidApi;
import com.example.stockprediction.fragments.StockFragment;
import com.example.stockprediction.objects.BaseFragment;
import com.example.stockprediction.objects.StockRecyclerViewAdapter;
import com.example.stockprediction.objects.stock.Stock;
import com.example.stockprediction.utils.MyAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.stockprediction.apis.RapidApi.*;

public class StockRecyclerBaseFragment<T extends Stock> extends BaseFragment {
    protected StockRecyclerViewAdapter<T> adapter;
    protected ArrayList<T> data;
    private HashMap<String, Integer> symbolMap = new HashMap<String, Integer>();

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
        Collections.sort(this.data,comparator); // TODO: check it
        adapter.notifyDataSetChanged();
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
            Collections.sort(this.data); // TODO: check it
            Log.e("pttt", "initStockRecyclerView: data="+data);
        },() -> { // Run on UI thread
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            adapter = initAdapter(recyclerView, data, onStockLike_callback);
            // Get stock from FireBaser

            getInstance().getQuotesRequest((List<Stock>) data, new CallBack_HttpTasks() {
                @Override
                public void onResponse(JSONObject json) {
                        Log.e("pttt", "StockJson found: "+json);
                    for (int position = 0; position < data.size(); position++) {
                        try {
                            parseQuotesResponse(adapter.getItemIndex(data.get(position).getSymbol()),adapter,json);
                        } catch (JSONException e) {
                            Log.e("pttt", "StockJson parsing error: "+e.getLocalizedMessage());
                        }
                    }
                }
                @Override
                public void onErrorResponse(VolleyError error) {
                        Log.e("pttt", "StockJson error: "+error);
                }
            });
            getInstance().getChartRequest((List<Stock>) data, new CallBack_HttpTasks() {
                @Override
                public void onResponse(JSONObject json) {
                    String symbol;
                    try {
                        symbol = json.getJSONObject("stocks").getString("symbol");
                        int index  = adapter.getItemIndex(symbol);
                        T stock = data.get(index);
                        stock.setChartData(json);
                        new MyAsyncTask().executeBgTask(()->{},()->{ // run on ui thread
                            Log.d("tag-test", "notifyItemChanged, stockSymbol="+stock.getSymbol()+", index="+index);
                            adapter.notifyItemChanged(index);
                        });

                        Log.d("stock_recycler_base_fragment", "StockJson found: " + symbol + ",stock to update: "+stock.getSymbol());
                    } catch (JSONException e) {
                        Log.e("pttt", "StockJson parsing error: "+e.getLocalizedMessage());
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("xop", "StockJson error: " + error);
                }
            });
        });
    }

    public void initStockRecyclerViewFromCache(RecyclerView recyclerView, initStockRecyclerData_Callback<T> initRecyclerData_callback, StockRecyclerViewAdapter.OnStockLike_Callback onStockLike_callback)  {
        new MyAsyncTask().executeBgTask(() -> { //Run on background thread.
            data = initRecyclerData_callback.initRecyclerData();
            Log.e("pttt", "initStockRecyclerView: data="+data);
        },() -> { // Run on UI thread
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            adapter = initAdapter(recyclerView, data, onStockLike_callback);
            // Get stock from FireBaser
            getInstance().getQuotesRequestCacheOnly(new CallBack_HttpTasks() {
                @Override
                public void onResponse(JSONObject json) {
                    Log.e("pttt", "StockJson found: "+json);
                    for (int position = 0; position < data.size(); position++) {
                        try {
                            parseQuotesResponse(adapter.getItemIndex(data.get(position).getSymbol()),adapter,json);
                        } catch (JSONException e) {
                            Log.e("pttt", "StockJson parsing error: "+e.getLocalizedMessage());
                        }
                    }
                }
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("pttt", "StockJson error: "+error);
                }
            });
        });
    }

    public boolean isDataEmpty() {
        return data.isEmpty();
    }

    private void parseQuotesResponse(int position, StockRecyclerViewAdapter<T> adapter, JSONObject json) throws JSONException {
        Stock stock = data.get(position);
        JSONObject result = json.getJSONObject("stocks").getJSONObject(stock.getSymbol().toUpperCase());
        stock.setValue(Double.parseDouble(result.getString("regularMarketPrice")));
        stock.setChangeAmount(Double.parseDouble(result.getString("regularMarketChange")));
        stock.setChangePercent(Double.parseDouble(result.getString("regularMarketChangePercent")));
        stock.setPredictionStatus();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemChanged(position);
            }
        });
    }

    private void parseChartResponse(int position, StockRecyclerViewAdapter<T> adapter, JSONObject json) {
        Stock stock = data.get(position);
        stock.setChartData(json);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemChanged(position);
            }
        });
    }


    public void goToStockFragment(Stock stock) {
        String jsonStock = stock.stockToJson();
        ((MainActivity)getActivity()).openStockFragment(StockFragment.ARG_PARAM,jsonStock);
    }
}

