package com.example.stockprediction.fragments.StockRecyclerFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockprediction.R;
import com.example.stockprediction.objects.stock.Stock;
import com.example.stockprediction.objects.adapter.StockRecyclerViewAdapter;

import java.util.ArrayList;


public class FavoritiesFragment extends StockRecyclerBaseFragment<Stock> {
    private RecyclerView recyclerView;
    private TextView favorities_TV_noStocks;
    private ImageView favorities_IMG_noStocks; //nodata_icon

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
        this.favorities_TV_noStocks = view.findViewById(R.id.favorities_TV_noStocks);
        this.favorities_IMG_noStocks = view.findViewById(R.id.favorities_IMG_noStocks);
    }

    private void initViews() {
        if(!getUser().getFavStocks().isEmpty()) {
            super.initStockRecyclerViewFromCache(recyclerView, () -> getUser().getFavStocks(), new StockRecyclerViewAdapter.OnStockLike_Callback() {
                @Override
                public void onStockLike(Stock stock) {
                    if(favorities_TV_noStocks.getVisibility() == View.VISIBLE) { favorities_TV_noStocks.setVisibility(View.INVISIBLE); }
                }

                @Override
                public void onStockDislike(Stock stock, int position) {
                    Log.d("FavStock", "onStockDislike: favoriteStocks="+ FavoritiesFragment.super.data+",index="+position+",stock="+stock.getName());
                    // Update user stock list
                    ArrayList<Stock> stocks = getUser().getFavStocks();
                    //ArrayList<Stock> stocks =new ArrayList<Stock>(adapter.getLikedStocks());

                    //stocks.remove(position);
                    Log.e("FavStock", "sonStockDislike: onStockDislike: "+stocks);
                    // Update Adapter
                    FavoritiesFragment.super.adapter.removeAt(position);
                    // Update user favStocks Set on firebase
                    updateUser(getUser().setFavStocks(stocks));
                    if(stocks.isEmpty()) {
                        favorities_TV_noStocks.setVisibility(View.VISIBLE);
                        favorities_IMG_noStocks.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            favorities_TV_noStocks.setVisibility(View.VISIBLE);
            favorities_IMG_noStocks.setVisibility(View.VISIBLE);
        }
    }
}
