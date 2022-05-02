package com.example.stockprediction.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stockprediction.R;
import com.example.stockprediction.apis.firebase.MyFireBaseServices;
import com.example.stockprediction.objects.BaseFragment;
import com.example.stockprediction.objects.StockRecyclerViewAdapter;
import com.example.stockprediction.objects.stock.Stock;
import com.example.stockprediction.utils.MyPreference;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import co.ankurg.expressview.ExpressView;
import co.ankurg.expressview.OnCheckListener;

public class StockFragment extends BaseFragment {

    public static final String ARG_PARAM = "stock";
    private Stock stock;

    private TextView stockFrag_TV_name;
    private TextView stockFrag_TV_symbol;
    private TextView stockFrag_TV_value;
    private TextView stockFrag_TV_StockStatusDetails;
    private TextView stockFrag_TV_date;
    private ImageView stockFrag_IMG_stockImg;
    private ImageView stockFrag_IMG_predictionStatus;
    private LineChart stockFrag_BarChart;
    private co.ankurg.expressview.ExpressView stockFrag_EV_likeButton;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Gson gson = new Gson();
            String jsonStock = getArguments().getString(ARG_PARAM);
            stock = gson.fromJson(jsonStock, Stock.class);
/*            RapidApi.getInstance().getQuotesRequestCacheOnly(new RapidApi.CallBack_HttpTasks() {
                @Override
                public void onResponse(JSONObject json) {
                    try {
                        updateDataFromAPI(json);
                    } catch (JSONException e) {
                        Log.e("StockFragment", "onCreate: stockUpdateFromAPI error= "+e);
                    }
                }
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });*/


        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stock, container, false);
        findViews(view);
        initViews();
        Log.d("StockFragment", "onCreate: stock = "+stock.getValue());
        setStockData(stock);
        return view;
    }

    private void updateDataFromAPI(JSONObject json) throws JSONException {
        JSONObject result = json.getJSONObject("stocks").getJSONObject(stock.getSymbol().toUpperCase());
        stock.setValue(Double.parseDouble(result.getString("regularMarketPrice")));
        stock.setChangeAmount(Double.parseDouble(result.getString("regularMarketChange")));
        stock.setChangePercent(Double.parseDouble(result.getString("regularMarketChangePercent")));
        stock.setPredictionStatus();
    }

    private void findViews(View view) {
        stockFrag_TV_name = view.findViewById(R.id.stockFrag_TV_name);
        stockFrag_TV_symbol = view.findViewById(R.id.stockFrag_TV_symbol);
        stockFrag_IMG_stockImg = view.findViewById(R.id.stockFrag_IMG_stockImg);
        stockFrag_TV_value = view.findViewById(R.id.stockFrag_TV_value);
        stockFrag_TV_StockStatusDetails = view.findViewById(R.id.stockFrag_TV_StockStatusDetails);
        stockFrag_TV_date = view.findViewById(R.id.stockFrag_TV_date);
        stockFrag_BarChart = view.findViewById(R.id.stockFrag_BarChart);
        stockFrag_IMG_predictionStatus = view.findViewById(R.id.stockFrag_IMG_predictionStatus);
        stockFrag_EV_likeButton = view.findViewById(R.id.stockFrag_EV_likeButton);
    }

    private void initViews() {
        stockFrag_EV_likeButton.setOnCheckListener(new OnCheckListener() {
            @Override
            public void onChecked(@org.jetbrains.annotations.Nullable ExpressView expressView) {
                ArrayList<Stock> stocks = getUser().getFavStocks();
                Log.d("pttt", "onStockLike: stocks = "+stocks);
                if(stocks.add(stock)) {
                    updateUser(getUser().setFavStocks(stocks));
                    Log.e("pttt", "onStockLike: user="+getUser());
                }
            }

            @Override
            public void onUnChecked(@org.jetbrains.annotations.Nullable ExpressView expressView) {
                ArrayList<Stock> stocks = getUser().getFavStocks();
                if(stocks.remove(stock)) {
                    updateUser(getUser().setFavStocks(stocks));
                }
            }
        });
    }

    private void setImg(String imgName,ImageView img) {
        if(!imgName.equalsIgnoreCase("")) {
            int resourceId = getContext().getResources().getIdentifier(imgName, "drawable",getContext().getPackageName());//initialize res and context in adapter's contructor
            img.setImageResource(resourceId);
        }
    }

    private void setStockStatusImg(ImageView img, Stock.StockStatus status, String type) {
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

    private void markLikedStocks(Stock stock) {
        if(getUser().getFavStocks() != null) {
            if(!getUser().getFavStocks().isEmpty()){
                if (getUser().getFavStocks().contains(stock)) {
                    if(!stockFrag_EV_likeButton.isChecked()) {
                        stockFrag_EV_likeButton.setChecked(true);
                        Log.d("stock_recycler", "markAsLiked: symbol="+stock.getSymbol() + ", likedStocks="+getUser().getFavStocks());
                    }
                } else {
                    stockFrag_EV_likeButton.setChecked(false);
                }
            }
        }
    }

    private void setTextViewColor (TextView textView) {
        char sign =  textView.getText().charAt(0);
        if (sign == '-') {
            textView.setTextColor(getContext().getColor(R.color.red_200));
        }   else if (sign == '+') {
            textView.setTextColor(getContext().getColor(R.color.green_200));
        }
    }
    private String getStockChangeDetails(double stockChangeAmount, double stockChangePercent,TextView textView){
        String sign = (stockChangeAmount> 0) ? "+" : "";
        return sign+String.format("%.2f", stockChangeAmount) + "(" + String.format("%.2f", stockChangePercent)+ "%)";
    }

    private void setStockChart(Stock stock) {
        if(stock.getChartData() == null) {
            JSONObject jsonObject = MyPreference.getInstance(getContext()).getStocksData(MyPreference.StockCacheManager.CACHE_KEYS.CHARTS_DATA_JSON+stock.getSymbol()); // trying to get stock chart from cache
            if(jsonObject != null) {
                stock.setChartData(jsonObject);
                initChart(stock,stockFrag_BarChart);
            }
        } else {
            initChart(stock,stockFrag_BarChart);
        }
    }

    private void initChart(Stock stock, com.github.mikephil.charting.charts.LineChart chart) {
        Log.d("tag-test", "setStockChart:, stockSymbol= " + stock.getSymbol());

        // no description text
        chart.getDescription().setEnabled(false);
        // enable touch gestures
        chart.setTouchEnabled(true);
        //chart.setDragDecelerationFrictionCoef(0.9f);
        // if disabled, scaling can be done on x- and y-axis separately
        //chart.setPinchZoom(true);
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        //chart.setDrawGridBackground(true);
        chart.setHighlightPerDragEnabled(true);
        setData(chart,stock.getChartData());
    }

    private void setData(com.github.mikephil.charting.charts.LineChart chart, List<Float> data) {
        Log.d("tag-test", "setStockChart:, data= " + data);

        ArrayList<Entry> lineEntries = new ArrayList<Entry>();
        for (int i = 0; i < data.size() ; i++) {
            lineEntries.add(new Entry(i, data.get(i)));
        }

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Stock Price");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setLineWidth(2);
        lineDataSet.setColor(getContext().getColor(R.color.black));
        lineDataSet.setCircleColor(getContext().getColor(R.color.black));
        lineDataSet.setCircleRadius(3);
        lineDataSet.setCircleHoleRadius(2);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setHighLightColor(Color.RED);
        lineDataSet.setValueTextSize(8);
        lineDataSet.setValueTextColor(getContext().getColor(R.color.white));

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

        chart.animateY(1000);
        chart.getXAxis().setGranularityEnabled(true);
        chart.getXAxis().setGranularity(1.0f);
        chart.getXAxis().setLabelCount(lineDataSet.getEntryCount());
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(true);
        chart.getXAxis().setEnabled(false);
        chart.setData(lineData);
        chart.invalidate();
    }

    private class MyValueFormatter extends ValueFormatter implements IValueFormatter {

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
    private void setStockData(Stock stock){
        stockFrag_TV_value.setText("$" + stock.getValue());
        stockFrag_TV_StockStatusDetails.setText(getStockChangeDetails(stock.getChangeAmount(),stock.getChangePercent(), stockFrag_TV_StockStatusDetails));
        setTextViewColor(stockFrag_TV_StockStatusDetails);
        //.setText(getStockChangeDetails(stock.getPredictionValue(),stock.calcPercentageChange(stock.getPredictionValue(), stock.getValue()), holder.RVROW_LBL_StockPredictionDetails));
        setImg(stock.getStockImg(),stockFrag_IMG_stockImg);
        setStockStatusImg(stockFrag_IMG_predictionStatus,stock.getPredictionStatus(),"prediction_status");
        stockFrag_TV_name.setText(stock.getName());
        stockFrag_TV_symbol.setText(stock.getSymbol());
        setStockChart(stock);
        markLikedStocks(stock);
    }

}