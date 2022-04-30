package com.example.stockprediction.objects;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stockprediction.R;
import com.example.stockprediction.objects.stock.Stock;
import com.example.stockprediction.utils.MyPreference;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import co.ankurg.expressview.ExpressView;
import co.ankurg.expressview.OnCheckListener;

public class StockRecyclerViewAdapter <T extends Stock> extends RecyclerView.Adapter<StockRecyclerViewAdapter<T>.ViewHolder> implements Filterable {
    private List<T> stocksData;
    private List<T> filteredStockData;
    private List<T> likedStocks;
    private HashMap<String,Integer> symbolIndexMap = new HashMap<String, Integer>();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private OnStockLike_Callback onStockLikeCallback;
    private JSONObject jsonStockData;

    public interface OnStockLike_Callback {
        void onStockLike(Stock stock);
        void onStockDislike(Stock stock, int position);
    }


    // data is passed into the constructor
    public StockRecyclerViewAdapter (Context context, List<T> filteredStockData, List<T> likedStocks, OnStockLike_Callback onStockLikeCallback) {
        this.mInflater = LayoutInflater.from(context);
        this.filteredStockData = filteredStockData;
        this.context = context;
        this.likedStocks = likedStocks;
        this.onStockLikeCallback = onStockLikeCallback;
        this.stocksData = new ArrayList<T>(filteredStockData);
        initSymbolIndexMap(stocksData);
        jsonStockData = MyPreference.getInstance(context).getStocksData(MyPreference.StockCacheManager.CACHE_KEYS.STOCKS_DATA_JSON);
    }

    private void initSymbolIndexMap(List<T> stockList) {
        for (int i = 0; i < stockList.size() ; i++) {
            this.symbolIndexMap.put(stockList.get(i).getSymbol(),i);
        }
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycle_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String ValSign = "";
        T stock = filteredStockData.get(position);
        Log.d("tag-test", "notifyItemChanged adapter:, stockSymbol= " + stock.getSymbol() + ",index="+position+", chart= "+stock.getChartData());

        if(jsonStockData != null) {
            setDataFromCache(stock,holder);
            Log.d("tag-test", "onBindViewHolder:  "+ stock.getChartData());

        } else {
            initStubData(stock,holder);
        }
        setStockChart(stock,holder);
        setTextViewColor(holder.RVROW_LBL_StockStatusDetails);
        setTextViewColor(holder.RVROW_LBL_StockPredictionDetails);
        markLikedStocks(stock,holder);
        holder.RVROW_EV_likeButton.setOnCheckListener(new OnCheckListener() {
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

    private void setDataFromCache(T stock, ViewHolder holder){
        JSONObject cacheStock;
        try {
            cacheStock = jsonStockData.getJSONObject("stocks").getJSONObject(stock.getSymbol());
            stock.setChangeAmount(Double.parseDouble(cacheStock.getString("regularMarketChange")));
            stock.setChangePercent(Double.parseDouble(cacheStock.getString("regularMarketChangePercent")));
            holder.RVROW_LBL_StockValue.setText("$" + Double.parseDouble(cacheStock.getString("regularMarketPrice")));
            holder.RVROW_LBL_StockStatusDetails.setText(getStockChangeDetails(stock.getChangeAmount(),stock.getChangePercent(), holder.RVROW_LBL_StockStatusDetails));
            holder.RVROW_LBL_StockPredictionDetails.setText(getStockChangeDetails(stock.getPredictionValue(),stock.calcPercentageChange(stock.getPredictionValue(), stock.getValue()), holder.RVROW_LBL_StockPredictionDetails));

            setImg(stock.getStockImg(),holder.RVROW_IMG_StockImg);
            setStockStatusImg(holder.RVROW_IMG_predictionStatus,stock.getPredictionStatus(),"prediction_status");
            holder.RVROW_LBL_StockName.setText(stock.getName());
            holder.RVROW_LBL_StockSymbol.setText(stock.getSymbol());
            if(stock.getPredictionStatus() == Stock.StockStatus.NO_DATA) {
                //holder.
            }

        } catch (JSONException e) {
            Log.e("stock_recycler", "parseQuotesResponse: jsonException = "+e);
            initStubData(stock,holder);
        }
    }

    private void initStubData(T stock, ViewHolder holder) {
        setImg(stock.getStockImg(),holder.RVROW_IMG_StockImg);
        setStockStatusImg(holder.RVROW_IMG_predictionStatus,stock.getPredictionStatus(),"prediction_status");
        holder.RVROW_LBL_StockName.setText(stock.getName());
        holder.RVROW_LBL_StockSymbol.setText(stock.getSymbol());
        holder.RVROW_LBL_StockValue.setText("$" + String.valueOf(stock.getValue()));
        holder.RVROW_LBL_StockStatusDetails.setText(getStockChangeDetails(stock.getChangeAmount(),stock.getChangePercent(), holder.RVROW_LBL_StockStatusDetails));
        holder.RVROW_LBL_StockPredictionDetails.setText(getStockChangeDetails(stock.getPredictionValue(),stock.calcPercentageChange(stock.getPredictionValue(), stock.getValue()), holder.RVROW_LBL_StockPredictionDetails));
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

    private void setTextViewColor (TextView textView) {
        char sign =  textView.getText().charAt(0);
        if (sign == '-') {
            textView.setTextColor(context.getColor(R.color.red_200));
        }   else if (sign == '+') {
            textView.setTextColor(context.getColor(R.color.green_200));
        }
    }
    private String getStockChangeDetails(double stockChangeAmount, double stockChangePercent,TextView textView){
        //String sign = (stock.getChangeAmount() > 0) ? "+" : (stock.getChangeAmount() < 0) ? "-" : "";
        String sign = (stockChangeAmount> 0) ? "+" : "";
        return sign+String.format("%.2f", stockChangeAmount) + "(" + String.format("%.2f", stockChangePercent)+ "%)";
    }

    @Override
    public int getItemCount() {
        return filteredStockData.size();
    }

    private void setImg(String imgName,ImageView img) {
        if(!imgName.equalsIgnoreCase("")) {
            Context context = mInflater.getContext();
            int resourceId = context.getResources().getIdentifier(imgName, "drawable",context.getPackageName());//initialize res and context in adapter's contructor
            img.setImageResource(resourceId);
        }
    }

    private void setStockStatusImg(ImageView img, T.StockStatus status, String type) {
        String imgName = "";
        switch (status) {
            case INCREASE:
                if(type.equalsIgnoreCase("prediction_status")) {
                    imgName="prediction_status_increase";
                }
                break;
            case DECREASE:
                if(type.equalsIgnoreCase("prediction_status")) {
                    imgName="prediction_status_decrease";
                }
                break;
            case NO_DATA:
            case UNCHANGED:
                if(type.equalsIgnoreCase("prediction_status")) {
                    imgName="prediction_status_unchanged";
                }
                break;
        }
        setImg(imgName,img);
    }

    private void initChart(T stock, com.github.mikephil.charting.charts.LineChart chart) {
        Log.d("tag-test", "setStockChart:, stockSymbol= " + stock.getSymbol());

        // no description text
        chart.getDescription().setEnabled(false);
        // enable touch gestures
        chart.setTouchEnabled(false);
        //chart.setDragDecelerationFrictionCoef(0.9f);
        // if disabled, scaling can be done on x- and y-axis separately
        //chart.setPinchZoom(true);
        // enable scaling and dragging
        chart.setDragEnabled(false);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);
        setData(chart,stock.getChartData());
    }

    private void setData(com.github.mikephil.charting.charts.LineChart chart, List<Float> data) {
        Log.d("tag-test", "setStockChart:, data= " + data);

        ArrayList<Entry> lineEntries = new ArrayList<Entry>();
        for (int i = 0; i < data.size() ; i++) {
            lineEntries.add(new Entry(i, data.get(i)));
        }
//        lineEntries.add(new Entry(0, 422.5f));
//        lineEntries.add(new Entry(1, 400.52f));
//        lineEntries.add(new Entry(2, 413.354f));
//        lineEntries.add(new Entry(3, 489.2f));
//        lineEntries.add(new Entry(4, 499.52f));


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
        lineDataSet.setValueTextColor(context.getColor(R.color.black));

        LineData lineData = new LineData(lineDataSet);
// usage on whole data object
        lineData.setValueFormatter(new MyValueFormatter());

// usage on individual dataset object
        lineDataSet.setValueFormatter(new MyValueFormatter());
        //chart.setDrawMarkers(true);
        //chart.setMarker(markerView(context));
        //chart.getAxisLeft().addLimitLine(lowerLimitLine(2,"Lower Limit",2,12,getColor("defaultOrange"),getColor("defaultOrange")));
        //chart.getAxisLeft().addLimitLine(upperLimitLine(5,"Upper Limit",2,12,getColor("defaultGreen"),getColor("defaultGreen")));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        //chart.animateX(200);
        chart.getXAxis().setGranularityEnabled(true);
        chart.getXAxis().setGranularity(1.0f);
        chart.getXAxis().setLabelCount(lineDataSet.getEntryCount());
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.setData(lineData);
        chart.invalidate();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        private ImageView RVROW_IMG_StockImg;
        private ImageView RVROW_IMG_currentStatus;
        private ImageView RVROW_IMG_predictionStatus;
        private TextView RVROW_LBL_StockName;
        private TextView RVROW_LBL_StockSymbol;
        private TextView RVROW_LBL_StockValue;
        private TextView RVROW_LBL_StockStatusDetails;
        private TextView RVROW_LBL_StockPredictionDetails;
        private ExpressView RVROW_EV_likeButton;
        private com.github.mikephil.charting.charts.LineChart RVROW_CHART;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            RVROW_IMG_StockImg = itemView.findViewById(R.id.RVROW_IMG_StockImg);
            RVROW_IMG_predictionStatus = itemView.findViewById(R.id.RVROW_IMG_predictionStatus);
            RVROW_LBL_StockName = itemView.findViewById(R.id.RVROW_LBL_StockName);
            RVROW_LBL_StockSymbol = itemView.findViewById(R.id.RVROW_LBL_StockSymbol);
            RVROW_LBL_StockValue = itemView.findViewById(R.id.RVROW_LBL_StockValue);
            RVROW_LBL_StockStatusDetails = itemView.findViewById(R.id.RVROW_LBL_StockStatusDetails);
            RVROW_LBL_StockPredictionDetails = itemView.findViewById(R.id.RVROW_LBL_StockPredictionDetails);
            RVROW_EV_likeButton = itemView.findViewById(R.id.RVROW_EV_likeButton);
            RVROW_CHART = itemView.findViewById(R.id.RVROW_CHART);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getBindingAdapterPosition());
        }
    }

/*    private void sortBy(Comparator<T> comparator) {
        this.filteredStockData.sort(comparator);
        this.symbolIndexMap.clear();
        for (int i = 0; i <filteredStockData.size() ; i++) {
            symbolIndexMap.put(filteredStockData.get(i).getSymbol(), i);
        }
        notifyDataSetChanged();
    }*/


    // convenience method for getting data at click position
    public T getItem(int id) {
        return filteredStockData.get(id);
    }

    public int getItemIndex(String symbol) { return symbolIndexMap.get(symbol); }

    public void removeAt(int position) {
        if(position >= 0) {
            notifyItemRemoved(position);
            notifyItemChanged(position);
        } else {
            Log.d("stock_recycler", "removeAt: no element found");
        }
    }

    public void addItem(int position) {
        notifyItemInserted(filteredStockData.size()-1);
        notifyItemChanged(filteredStockData.size()-1);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public class MyValueFormatter extends ValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0.000"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            return mFormat.format(value) + " $"; // e.g. append a dollar-sign
        }
    }

}
