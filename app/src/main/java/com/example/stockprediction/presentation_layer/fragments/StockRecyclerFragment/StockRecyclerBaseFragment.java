
package com.example.stockprediction.presentation_layer.fragments.StockRecyclerFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.example.stockprediction.data_access_layer.apis.RapidApi;
import com.example.stockprediction.data_access_layer.firebase.MyFireBaseServices;
import com.example.stockprediction.presentation_layer.activites.MainActivity;
import com.example.stockprediction.business_logic_layer.objects.adapter.BaseStockRecyclerViewAdapter;
import com.example.stockprediction.presentation_layer.fragments.StockFragment;
import com.example.stockprediction.presentation_layer.BaseFragment;
import com.example.stockprediction.business_logic_layer.objects.Prediction;
import com.example.stockprediction.business_logic_layer.objects.adapter.StockRecyclerViewAdapter;
import com.example.stockprediction.business_logic_layer.objects.stock.Stock;
import com.example.stockprediction.utils.MyAsyncTask;
import com.example.stockprediction.utils.MyTimeStamp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.stockprediction.data_access_layer.apis.RapidApi.*;

public class StockRecyclerBaseFragment<T extends Stock> extends BaseFragment {
    protected StockRecyclerViewAdapter<T> adapter;
    protected ArrayList<T> data;
    private SearchViewModel searchViewModel;
    private PredictionReady_callback predictionReady_callback;


    protected interface initStockRecyclerData_Callback<T extends Stock> {
        ArrayList<T> initRecyclerData();
    }

    protected interface PredictionReady_callback<T extends Stock> {
        void onPredictionUpdate(List<Stock> stocks);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        Log.e("stock_recycler_base_fragment", "initAdapter: favStocks = "+getUser().getFavStocks());
        Log.e("stock_recycler_base_fragment", "initAdapter: data = "+stocksData);

        adapter = new StockRecyclerViewAdapter(getContext(), stocksData, getUser().getFavStocks(), onStockLike_callback);
        adapter.setClickListener((view, position) -> {
            Log.d("stock_recycler_base_fragment", "onItemClick: Selected item: "+position + ",stocks="+stocksData);
            Stock stock = stocksData.get(position);
            goToStockFragment(stock);
        });
        recyclerView.setAdapter(adapter);
        return adapter;
    }

    public void initStockRecyclerView(RecyclerView recyclerView,initStockRecyclerData_Callback<T> initRecyclerData_callback, StockRecyclerViewAdapter.OnStockLike_Callback onStockLike_callback)  {
        new MyAsyncTask().executeBgTask(() -> { //Run on background thread.
            data = initRecyclerData_callback.initRecyclerData();
            Log.e("stock_recycler_base_fragment", "initStockRecyclerView: data="+data);
        },() -> { // Run on UI thread
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            adapter = initAdapter(recyclerView, data, onStockLike_callback);
            // Get stocks data from api
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
            getInstance().getChartsRequest((List<Stock>) data, new CallBack_HttpTasks() {
                @Override
                public void onResponse(JSONObject json) {
                    String symbol;
                    try {
                        symbol = json.getJSONObject("stocks").getString("symbol");
                        int index  = adapter.getItemIndex(symbol);
                        T stock = data.get(index);
                        stock.setChartData(json);
                        updateFavStocksChartData(stock);
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
            initDailyPredictions();
            observeModelView();
        });
    }

    public void initStockRecyclerView(RecyclerView recyclerView,initStockRecyclerData_Callback<T> initRecyclerData_callback, StockRecyclerViewAdapter.OnStockLike_Callback onStockLike_callback, PredictionReady_callback predictionReady_callback)  {
        initStockRecyclerView(recyclerView,initRecyclerData_callback,onStockLike_callback);
        this.predictionReady_callback = predictionReady_callback;
    }

    public void initStockRecyclerViewFromCache(RecyclerView recyclerView, initStockRecyclerData_Callback<T> initRecyclerData_callback, StockRecyclerViewAdapter.OnStockLike_Callback onStockLike_callback)  {
        new MyAsyncTask().executeBgTask(() -> { //Run on background thread.
            data = initRecyclerData_callback.initRecyclerData();
            Log.e("pttt", "initStockRecyclerView: data="+data);
        },() -> { // Run on UI thread
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            adapter = initAdapter(recyclerView, data, onStockLike_callback);
        });
        observeModelView();
    }

    private void initDailyPredictions() {
        for (Stock stock: getUser().getFavStocks()) {
            stock.setPredictionValue(0);
        }
        updateUser(getUser());
        MyFireBaseServices.getInstance().listenPredictions(new MyFireBaseServices.FB_Request_Callback<HashMap<String, ArrayList<Prediction>>>() {
            @Override
            public void OnSuccess(HashMap<String, ArrayList<Prediction>> result) {
                // Hashmap looks like -> {Thursday=[PredictionList],Wednesday=[PredictionList]}
                String day = MyTimeStamp.getDisplayDay();
                if(day.equalsIgnoreCase("Saturday") || day.equalsIgnoreCase("Sunday")) // on weekend days display Friday predictions.
                    day = "Friday";
                //String day = "Friday";
                //String day = "Monday";
                HashMap<String,Double> temp = new HashMap<String,Double>();
                ArrayList<Prediction> predictions = result.get(day); // all predictions for today
                if(predictions != null) {
                    List<T> predictionStocks = new ArrayList<T>();
                    for (Prediction prediction: predictions) {
                        int index = adapter.getItemIndex(prediction.getTargetSymbol());
                        T stock = data.get(index);
                        if(temp.getOrDefault(prediction.getTargetSymbol(),-10.0) < prediction.getPoints()) { // handle predictions conflict
                            temp.put(prediction.getTargetSymbol(),prediction.getPoints());
                            stock.setPredictionValue(prediction.getPoints());
                            predictionStocks.add(stock);
                            updateFavStocksPrediction(stock); // added
                            adapter.notifyItemChanged(index);
                        }
                    }

                    updateUser(getUser());
                    if(!predictions.isEmpty() && predictionReady_callback!=null)
                        predictionReady_callback.onPredictionUpdate(predictionStocks);
                    Log.d("data_data", "OnSuccess: data = "+predictionStocks);
                    //adapter.sortData();
                }
            }
            @Override
            public void OnFailure(Exception e) {
                Log.e("data_data", "initDailyPredictions: error= "+e);
            }
        });
    }

    private void updateFavStocksChartData(T stock) {
        for (int i = 0; i < getUser().getFavStocks().size() ; i++) {
            if(getUser().getFavStocks().get(i).getSymbol().equalsIgnoreCase(stock.getSymbol())) {
                getUser().getFavStocks().get(i).setChartData(stock.getChartData());
                break;
            }
        }
    }

    private void updateFavStocksPrediction(T predictionStocks) {
        for (int i = 0; i < getUser().getFavStocks().size() ; i++) {
            if(getUser().getFavStocks().get(i).getSymbol().equalsIgnoreCase(predictionStocks.getSymbol())) {
                getUser().getFavStocks().get(i).setPredictionValue(predictionStocks.getPredictionValue());
                break;
            }
        }
    }

    protected void parseQuotesResponse(int position, BaseStockRecyclerViewAdapter<T> adapter, JSONObject json) throws JSONException {
        Stock stock = data.get(position);
        JSONObject result = json.getJSONObject("stocks").getJSONObject(stock.getSymbol().toUpperCase());
        stock.setValue(Double.parseDouble(result.getString("regularMarketPrice")));
        stock.setChangeAmount(Double.parseDouble(result.getString("regularMarketChange")));
        stock.setChangePercent(Double.parseDouble(result.getString("regularMarketChangePercent")));
        stock.setPredictionStatus();
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyItemChanged(position);
                }
            });
        } catch (NullPointerException e) {

        }

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
        if(stock != null) {
            String jsonStock = stock.stockToJson();
            ((MainActivity)getActivity()).openStockFragment(StockFragment.ARG_PARAM,jsonStock);
        }
    }
}

