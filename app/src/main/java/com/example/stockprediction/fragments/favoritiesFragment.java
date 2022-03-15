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

import com.example.stockprediction.R;
import com.example.stockprediction.activites.MainActivity;
import com.example.stockprediction.objects.BaseFragment;
import com.example.stockprediction.objects.MyLinkedHashSet;
import com.example.stockprediction.objects.Stock;
import com.example.stockprediction.objects.StockRecyclerViewAdapter;
import com.example.stockprediction.objects.User;
import com.example.stockprediction.utils.MyAsyncTask;
import com.example.stockprediction.utils.MyFireBaseServices;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;


public class favoritiesFragment extends BaseFragment {
    private StockRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private MyLinkedHashSet<Stock> favoriteStocksData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorities, container, false);
        findViews(view);
        initViews();
        return view;
    }

    private void findViews(View view) {
        this.recyclerView = view.findViewById(R.id.favorities_RV_stocks);
    }

    private void initViews() {
        initStockRecyclerView();
    }


    private void initStockRecyclerView() {
        new MyAsyncTask().executeBgTask(() -> { //Run on background thread.
            //this.favoriteStocksData = loadStocksData(); // Loading stocks data
            favoriteStocksData = (MyLinkedHashSet<Stock>) getUser().getFavStocks();
        },() -> { // Run on UI thread
            this.recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            adapter = new StockRecyclerViewAdapter(this.getContext(), favoriteStocksData, getUser().getFavStocks(), new StockRecyclerViewAdapter.OnStockLike_Callback() {
                @Override
                public void onStockLike(Stock stock) {
                    Log.d("pttt", "onStockLike: favoriteStocks="+favoriteStocksData+",userFavStocks="+getUser().getFavStocks());

                }

                @Override
                public void onStockDislike(Stock stock,int position) {
                    // Find index of stock
                    Log.d("pttt", "onStockDislike: favoriteStocks="+favoriteStocksData+"index="+position);
                    // Update user favStocks Set
                    MyLinkedHashSet<Stock> stocks = getUser().getFavStocks();
                    stocks.remove(position);
                    updateUser(getUser().setFavStocks(stocks));
                    // Update Adapter
                    //favoriteStocksData.remove(index);
                    //adapter.notifyItemChanged(index);
                    adapter.removeAt(position);
                    // Update user favStocks Set on firebase
                    MyFireBaseServices.getInstance().saveUserToFireBase(getUser());
                }
            });
            recyclerView.setAdapter(adapter);
            adapter.setClickListener(new StockRecyclerViewAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Log.d("pttt", "onItemClick: Selected item: "+position);
                }
            });
        });

    }

    private ArrayList<Stock> loadStocksData() {
        ArrayList<Stock> favoriteStocks = new ArrayList<>();
        // STUB IMPLEMENTATION
        favoriteStocks.add(new Stock("name", "NVDA", 0.0, 51, 33.5,Stock.StockStatus.DECREASE,"ic_launcher_background"));
        favoriteStocks.add(new Stock("name", "NVDA", 0.0, 5.2, 33.5, Stock.StockStatus.DECREASE, "ic_launcher_background"));
        // Load favorities stocks from shared-preference.
        return favoriteStocks;
    }

    private void addToFavStocks(Stock stock) {
        new MyAsyncTask().executeBgTask(() -> { // Run on background thread.
            favoriteStocksData.add(stock);
            updateUser(getUser().setFavStocks(favoriteStocksData));
            MyFireBaseServices.getInstance().saveUserToFireBase(getUser());
        },() -> { // Run on ui thread.
            adapter.notifyItemChanged(favoriteStocksData.size()-1);
        });
    }
}
