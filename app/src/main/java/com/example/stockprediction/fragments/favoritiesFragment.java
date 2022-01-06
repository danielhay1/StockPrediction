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
import com.example.stockprediction.objects.Stock;
import com.example.stockprediction.objects.StockRecyclerViewAdapter;

import java.io.IOException;
import java.util.ArrayList;

public class favoritiesFragment extends Fragment {
    private StockRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<Stock> favoriteStocksData;
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
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        this.favoriteStocksData = loadStocksData(); // Loading stocks data
        adapter = new StockRecyclerViewAdapter(this.getContext(), favoriteStocksData);
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

    private ArrayList<Stock> loadStocksData() {
        ArrayList<Stock> favoriteStocks = new ArrayList<>();
        // STUB IMPLEMENTATION
        favoriteStocks.add(new Stock("name", "NVDA", 0.0, 51, 33.5,Stock.StockStatus.DECREASE,"ic_launcher_background"));
        favoriteStocks.add(new Stock("name", "NVDA", 0.0, 5.2, 33.5, Stock.StockStatus.DECREASE, "ic_launcher_background"));
        // Load favorities stocks from shared-preference.
        return favoriteStocks;
    }
}
