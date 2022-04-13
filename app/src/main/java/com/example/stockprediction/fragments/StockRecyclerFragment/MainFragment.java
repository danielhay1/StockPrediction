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
import com.example.stockprediction.apis.RapidApi;
import com.example.stockprediction.objects.stock.Stock;
import com.example.stockprediction.objects.StockRecyclerViewAdapter;
import com.example.stockprediction.apis.firebase.MyFireBaseServices;
import java.util.ArrayList;
import java.util.Map;


public class MainFragment extends StockRecyclerBaseFragment<Stock> {
    private RecyclerView recyclerView;

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
        super.initStockRecyclerView(recyclerView,new initStockRecyclerData_Callback<Stock>() {
            @Override
            public ArrayList<Stock> initRecyclerData() {
                ArrayList<Stock> stocksData = new ArrayList<Stock>();
                for (Map.Entry<String, String> entry : RapidApi.MY_STOCKS.entrySet()) {
                    stocksData.add(new Stock(entry.getKey(), entry.getValue()));
                }
                return stocksData;
            }
        }, new StockRecyclerViewAdapter.OnStockLike_Callback() {
            @Override
            public void onStockLike(Stock stock) {
                ArrayList<Stock> stocks = getUser().getFavStocks();
                Log.d("pttt", "onStockLike: stocks = "+stocks);
                if(stocks.add(stock)) {
                    updateUser(getUser().setFavStocks(stocks));
                    Log.e("pttt", "onStockLike: user="+getUser());
                    MyFireBaseServices.getInstance().saveUserToFireBase(getUser());
                }
            }

            @Override
            public void onStockDislike(Stock stock, int position) {
                ArrayList<Stock> stocks = getUser().getFavStocks();
                if(stocks.remove(stock)) {
                    updateUser(getUser().setFavStocks(stocks));
                    MyFireBaseServices.getInstance().saveUserToFireBase(getUser());
                }
            }
        });
    }
}
