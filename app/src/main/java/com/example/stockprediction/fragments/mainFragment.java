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
import com.example.stockprediction.objects.BaseFragment;
import com.example.stockprediction.objects.MyLinkedHashSet;
import com.example.stockprediction.objects.Stock;
import com.example.stockprediction.objects.StockRecyclerViewAdapter;
import com.example.stockprediction.objects.User;
import com.example.stockprediction.utils.MyAsyncTask;
import com.example.stockprediction.utils.MyFireBaseServices;
import java.util.ArrayList;
import java.util.Map;


public class mainFragment extends BaseFragment {
    private StockRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<Stock> stocksData = new ArrayList<>();
    private Fragment stockFragment;


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

    }

    private void initViews() {
        initStockRecyclerView();
    }

    private StockRecyclerViewAdapter initAdapter(RecyclerView recyclerView, ArrayList<Stock> stocksData) {
        Log.e("pttt", "initAdapter: favStocks = "+getUser().getFavStocks());
        adapter = new StockRecyclerViewAdapter(getContext(), stocksData, getUser().getFavStocks(), new StockRecyclerViewAdapter.OnStockLike_Callback() {
            @Override
            public void onStockLike(Stock stock) {
                MyLinkedHashSet<Stock> stocks = getUser().getFavStocks();
                Log.d("pttt", "onStockLike: stocks = "+stocks);
                stocks.add(stock);
                updateUser(getUser().setFavStocks(stocks));
                MyFireBaseServices.getInstance().saveUserToFireBase(getUser());
            }

            @Override
            public void onStockDislike(Stock stock, int position) {
                MyLinkedHashSet<Stock> stocks = getUser().getFavStocks();
                stocks.remove(stock);
                updateUser(getUser().setFavStocks(stocks));
                MyFireBaseServices.getInstance().saveUserToFireBase(getUser());
            }
        });
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
        new MyAsyncTask().executeBgTask(() -> { //Run on background thread.
            for (Map.Entry<String, String> entry : RapidApi.MY_STOCKS.entrySet()) {
                stocksData.add(new Stock(entry.getKey(),entry.getValue()));
            }
        },() -> { // Run on UI thread
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            adapter = initAdapter(recyclerView, stocksData);
            for (int i = 0; i < stocksData.size(); i++) {
                Stock stock = stocksData.get(i);
                int finalI = i;
                // TODO: set this code back to run
               /* RapidApi.getInstance().httpGetJson(stock.getSymbol(), RapidApi.STOCK_OPERATIONS.GET_CHART, new RapidApi.CallBack_HttpTasks() {
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
                });*/
            }
        });
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
