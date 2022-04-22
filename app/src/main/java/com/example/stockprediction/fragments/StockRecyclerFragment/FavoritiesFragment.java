package com.example.stockprediction.fragments.StockRecyclerFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockprediction.R;
import com.example.stockprediction.objects.stock.Stock;
import com.example.stockprediction.objects.StockRecyclerViewAdapter;
import com.example.stockprediction.apis.firebase.MyFireBaseServices;

import java.util.ArrayList;


public class FavoritiesFragment extends StockRecyclerBaseFragment<Stock> {
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorities, container, false);
        findViews(view);
        if(getUser()!=null) {
            initViews();
        }
        return view;
    }

    private void findViews(View view) {
        this.recyclerView = view.findViewById(R.id.favorities_RV_stocks);
    }

    private void initViews() {
        super.initStockRecyclerViewFromCache(recyclerView, () -> getUser().getFavStocks(), new StockRecyclerViewAdapter.OnStockLike_Callback() {
            @Override
            public void onStockLike(Stock stock) { }

            @Override
            public void onStockDislike(Stock stock, int position) {
                Log.d("pttt", "onStockDislike: favoriteStocks="+ FavoritiesFragment.super.data+"index="+position);
                // Update user stock list
                ArrayList<Stock> stocks = getUser().getFavStocks();
                stocks.remove(position);
                updateUser(getUser().setFavStocks(stocks));
                Log.e("pttt", "FavStocks: onStockDislike: "+stocks);
                // Update Adapter
                FavoritiesFragment.super.adapter.removeAt(position);
                // Update user favStocks Set on firebase
                MyFireBaseServices.getInstance().saveUserToFireBase(getUser());
            }
        });
    }
}
