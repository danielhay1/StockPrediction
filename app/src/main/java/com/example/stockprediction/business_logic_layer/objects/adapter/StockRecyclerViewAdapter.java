package com.example.stockprediction.business_logic_layer.objects.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import androidx.annotation.NonNull;

import com.example.stockprediction.R;
import com.example.stockprediction.business_logic_layer.objects.stock.Stock;
import com.example.stockprediction.data_access_layer.MyPreference;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import co.ankurg.expressview.ExpressView;
import co.ankurg.expressview.OnCheckListener;


public class StockRecyclerViewAdapter <T extends Stock> extends BaseStockRecyclerViewAdapter<T> implements Filterable {
    private List<T> likedStocks;
    private HashMap<String,Integer> symbolIndexMap = new HashMap<String, Integer>();
    private OnStockLike_Callback onStockLikeCallback;
    private JSONObject jsonStockData;

    public interface OnStockLike_Callback {
        void onStockLike(Stock stock);
        void onStockDislike(Stock stock, int position);
    }


    // data is passed into the constructor
    public StockRecyclerViewAdapter (Context context, List<T> stocksData, List<T> likedStocks, OnStockLike_Callback onStockLikeCallback) {
        super(context,stocksData);
        this.likedStocks = likedStocks;
        this.onStockLikeCallback = onStockLikeCallback;
        jsonStockData = MyPreference.getInstance(context).getStocksData(MyPreference.StockCacheManager.CACHE_KEYS.STOCKS_DATA_JSON);
    }

    public List<T> getLikedStocks() {
        return likedStocks;
    }

    public StockRecyclerViewAdapter setLikedStocks(List<T> likedStocks) {
        this.likedStocks = likedStocks;
        return this;
    }

    @NonNull
    @Override
    public BaseStockRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = super.mInflater.inflate(R.layout.recycle_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseStockRecyclerViewAdapter<T>.ViewHolder holder, int position) {
        T stock = filteredStockData.get(position);
        ViewHolder v = (ViewHolder) holder;
        if(jsonStockData != null) {
            setDataFromCache(stock, (ViewHolder) holder);
        } else {
            fillStockData(stock, v);
            setImg(stock.getStockImg(),((ViewHolder) holder).RVROW_IMG_StockImg);

        }
        setStockChart(stock, v);
        super.setTextViewColor(holder.RVROW_LBL_StockStatusDetails);
        //super.setTextViewColor(holder.RVROW_LBL_StockPredictionDetails);
        markLikedStocks(stock, v);
        v.RVROW_EV_likeButton.setOnCheckListener(new OnCheckListener() {
            @Override
            public void onChecked(@Nullable ExpressView expressView) {
                onStockLikeCallback.onStockLike(stock);
            }

            @Override
            public void onUnChecked(@Nullable ExpressView expressView) {
                onStockLikeCallback.onStockDislike(stock,position);
            }
        });
    }

    @Override
    public Filter getFilter() {
        return stockFilter;
    }

    private Filter stockFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<T> filteredList = new ArrayList<T>();
            if(constraint == null || constraint.length() == 0) {
                filteredList.addAll(stocksData);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (T item: stocksData) {
                    if(isMatchPrefix(item,filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredStockData.clear();
            symbolIndexMap.clear();
            List<T> res = (List<T>) results.values;
            for (int i = 0; i < res.size(); i++) {
                filteredStockData.add(res.get(i));
                symbolIndexMap.put(res.get(i).getSymbol(),i);
            }
            notifyDataSetChanged();
        }
    };


    private boolean isMatchPrefix(T stock, String prefix) {
        return stock.getName().toLowerCase().startsWith(prefix) || stock.getSymbol().toLowerCase().startsWith(prefix);
    }


    private void setDataFromCache(T stock, ViewHolder holder){
        JSONObject cacheStock;
        try {
            cacheStock = jsonStockData.getJSONObject("stocks").getJSONObject(stock.getSymbol());
            stock.setChangeAmount(Double.parseDouble(cacheStock.getString("regularMarketChange")));
            stock.setChangePercent(Double.parseDouble(cacheStock.getString("regularMarketChangePercent")));
            holder.RVROW_LBL_StockValue.setText("$" + Double.parseDouble(cacheStock.getString("regularMarketPrice")));
            holder.RVROW_LBL_StockStatusDetails.setText(super.getStockChangeDetails(stock.getChangeAmount(),stock.getChangePercent(), holder.RVROW_LBL_StockStatusDetails));
            //holder.RVROW_LBL_StockPredictionDetails.setText(super.getStockChangeDetails(stock.getPredictionValue(),stock.calcPercentageChange(stock.getPredictionValue(), stock.getValue()), holder.RVROW_LBL_StockPredictionDetails));
            setPredcitionPercent(stock.getPredictionValue(),holder.RVROW_LBL_StockPredictionDetails);
            setImg(stock.getStockImg(),holder.RVROW_IMG_StockImg);
            setStockStatusImg(holder.RVROW_IMG_predictionStatus,stock.getPredictionStatus(),"prediction_status");
            holder.RVROW_LBL_StockName.setText(stock.getName());
            holder.RVROW_LBL_StockSymbol.setText(stock.getSymbol());

        } catch (JSONException e) {
            Log.e("stock_recycler", "parseQuotesResponse: jsonException = "+e);
            fillStockData(stock,holder);
        }
    }

    private void setStockChart(T stock, ViewHolder holder) {
        if(stock.getChartData() == null) {
            JSONObject jsonObject = MyPreference.getInstance(context).getStocksData(MyPreference.StockCacheManager.CACHE_KEYS.CHARTS_DATA_JSON+stock.getSymbol()); // trying to get stock chart from cache
            if(jsonObject != null) {
                stock.setChartData(jsonObject);
                initChart(stock,holder.RVROW_CHART);
            }
        } else {
            initChart(stock,holder.RVROW_CHART);
        }
    }


    private void markLikedStocks(T stock, ViewHolder holder) {
        if(likedStocks != null) {
            if(!likedStocks.isEmpty()){
                if (likedStocks.contains(stock)) {
                    if(!holder.RVROW_EV_likeButton.isChecked()) {
                        holder.RVROW_EV_likeButton.setChecked(true);
                        Log.d("stock_recycler", "markAsLiked: symbol="+likedStocks + ", likedStocks="+likedStocks);
                    }
                } else {
                    holder.RVROW_EV_likeButton.setChecked(false);
                }
            }
        }
    }

    @Override
    protected void setChartData(LineChart chart, List<Float> data) {
        ArrayList<Entry> lineEntries = new ArrayList<Entry>();
        for (int i = 0; i < data.size() ; i++) {
            lineEntries.add(new Entry(i, data.get(i)));
        }
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Stock Price");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setLineWidth(2);
        lineDataSet.setColor(context.getColor(R.color.purple_500));
        lineDataSet.setCircleColor(context.getColor(R.color.purple_500));
        lineDataSet.setCircleRadius(3);
        lineDataSet.setCircleHoleRadius(2);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setHighLightColor(Color.RED);
        lineDataSet.setValueTextSize(8);
        lineDataSet.setValueTextColor(context.getColor(R.color.text));
        lineDataSet.setCircleHoleColor(context.getColor(R.color.theme_bgcolor));

        LineData lineData = new LineData(lineDataSet);
        lineData.setValueFormatter(new MyValueFormatter());
        lineDataSet.setValueFormatter(new MyValueFormatter());
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularityEnabled(true);
        chart.getXAxis().setGranularity(1.0f);
        chart.getXAxis().setLabelCount(lineDataSet.getEntryCount());
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.setData(lineData);
        chart.setNoDataTextColor(context.getColor(R.color.text));
        chart.getLegend().setTextColor(context.getColor(R.color.text));
        chart.setNoDataTextColor(context.getColor(R.color.purple_900));
        chart.invalidate();
    }

    public class ViewHolder extends BaseStockRecyclerViewAdapter.ViewHolder {
        private ImageView RVROW_IMG_StockImg;
        private ExpressView RVROW_EV_likeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            RVROW_IMG_StockImg = itemView.findViewById(R.id.RVROW_IMG_StockImg);
            super.RVROW_IMG_predictionStatus = itemView.findViewById(R.id.RVROW_IMG_predictionStatus);
            super.RVROW_LBL_StockName = itemView.findViewById(R.id.RVROW_LBL_StockName);
            super.RVROW_LBL_StockSymbol = itemView.findViewById(R.id.RVROW_LBL_StockSymbol);
            super.RVROW_LBL_StockValue = itemView.findViewById(R.id.RVROW_LBL_StockValue);
            super.RVROW_LBL_StockStatusDetails = itemView.findViewById(R.id.RVROW_LBL_StockStatusDetails);
            super.RVROW_LBL_StockPredictionDetails = itemView.findViewById(R.id.RVROW_LBL_StockPredictionDetails);
            RVROW_EV_likeButton = itemView.findViewById(R.id.RVROW_EV_likeButton);
            super.RVROW_CHART = itemView.findViewById(R.id.RVROW_CHART);
            itemView.setOnClickListener(this);
        }
    }
}
