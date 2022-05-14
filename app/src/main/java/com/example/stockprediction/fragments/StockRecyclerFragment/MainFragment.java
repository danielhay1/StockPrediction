package com.example.stockprediction.fragments.StockRecyclerFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.example.stockprediction.R;
import com.example.stockprediction.apis.RapidApi;
import com.example.stockprediction.objects.adapter.BaseStockRecyclerViewAdapter;
import com.example.stockprediction.objects.stock.Stock;
import com.example.stockprediction.objects.adapter.StockRecyclerViewAdapter;
import com.example.stockprediction.utils.MyAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.example.stockprediction.apis.RapidApi.getInstance;


public class MainFragment extends StockRecyclerBaseFragment<Stock> {
    private RecyclerView recyclerView;
    private RecyclerView main_RV_predictions;
    private BaseStockRecyclerViewAdapter<Stock> adapter;


    public interface DataReadyCallback
    {
        void dataReady(ArrayList data, int position);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        findViews(view);
        if(getUser()!=null) {
            initViews();
        }
        return view;
    }

    private void findViews(View view) {
        this.recyclerView = view.findViewById(R.id.main_RV_stocks);
        this.main_RV_predictions = view.findViewById(R.id.main_RV_predictions);
    }

    private void initViews() {
        super.initStockRecyclerView(recyclerView, () -> {
            ArrayList<Stock> stocksData = new ArrayList<Stock>();
            for (Map.Entry<String, String> entry : RapidApi.MY_STOCKS.entrySet()) {
                stocksData.add(new Stock(entry.getKey(), entry.getValue()));
            }
            Collections.sort(stocksData);
            return stocksData;
        }, new StockRecyclerViewAdapter.OnStockLike_Callback() {
            @Override
            public void onStockLike(Stock stock) {
                ArrayList<Stock> stocks = getUser().getFavStocks();
                Log.d("pttt", "onStockLike: stocks = " + stocks);
                if (stocks.add(stock)) {
                    updateUser(getUser().setFavStocks(stocks));
                    Log.e("pttt", "onStockLike: user=" + getUser());
                }
            }

            @Override
            public void onStockDislike(Stock stock, int position) {
                ArrayList<Stock> stocks = getUser().getFavStocks();
                if (stocks.remove(stock)) {
                    updateUser(getUser().setFavStocks(stocks));
                }
            }
        }, list -> {
            initBaseStockRecyclerView(main_RV_predictions,list);
        });
    }

    public void initBaseStockRecyclerView(RecyclerView recyclerView,List<Stock> data) {
        new MyAsyncTask().executeBgTask(() -> { //Run on background thread.

            Log.e("pttt", "initStockRecyclerView-predictions: data="+data);

        },() -> { // Run on UI thread
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext(),RecyclerView.HORIZONTAL,true));
            adapter = initBaseAdapter(recyclerView,data);
            getInstance().getQuotesRequest((List<Stock>) data, new RapidApi.CallBack_HttpTasks() {
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
            getInstance().getChartsRequest((List<Stock>) data, new RapidApi.CallBack_HttpTasks() {
                @Override
                public void onResponse(JSONObject json) {
                    String symbol;
                    try {
                        symbol = json.getJSONObject("stocks").getString("symbol");
                        int index  = adapter.getItemIndex(symbol);
                        Stock stock = data.get(index);
                        stock.setChartData(json);
                        new MyAsyncTask().executeBgTask(()->{},()->{ // run on ui thread
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
    private BaseStockRecyclerViewAdapter<Stock> initBaseAdapter(RecyclerView recyclerView, List<Stock> stocksData) {
        Log.e("pttt", "initAdapter: favStocks = "+getUser().getFavStocks());
        adapter = new BaseStockRecyclerViewAdapter(getContext(), stocksData);
        adapter.setClickListener((view, position) -> {
            Log.d("pttt", "onItemClick: Selected item: "+position);
            Stock stock = stocksData.get(position);
            goToStockFragment(stock);
        });
        recyclerView.setAdapter(adapter);
        return adapter;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
