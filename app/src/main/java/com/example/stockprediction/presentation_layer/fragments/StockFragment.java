package com.example.stockprediction.presentation_layer.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.example.stockprediction.R;
import com.example.stockprediction.data_access_layer.apis.RapidApi;
import com.example.stockprediction.data_access_layer.apis.RapidApi.CallBack_HttpTasks;
import com.example.stockprediction.presentation_layer.BaseFragment;
import com.example.stockprediction.business_logic_layer.objects.Prediction;
import com.example.stockprediction.business_logic_layer.objects.stock.Stock;
import com.example.stockprediction.data_access_layer.firebase.MyFireBaseServices;
import com.example.stockprediction.data_access_layer.MyPreference;
import com.example.stockprediction.utils.MyTimeStamp;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import co.ankurg.expressview.ExpressView;
import co.ankurg.expressview.OnCheckListener;
// implement RapidApi.CallBack_HttpTasks()
public class StockFragment extends BaseFragment implements CallBack_HttpTasks{

    public static final String ARG_PARAM = "stock";
    private Stock stock;
    private TextView stockFrag_TV_name;
    private TextView stockFrag_TV_symbol;
    private TextView stockFrag_TV_value;
    private TextView stockFrag_TV_StockStatusDetails;
    private TextView stockFrag_TV_predictionValue;
    private ImageView stockFrag_IMG_stockImg;
    private ImageView stockFrag_IMG_predictionStatus;
    private ImageView stockFrag_BTN_expandButton;
    private LineChart stockFrag_BarChart;
    private co.ankurg.expressview.ExpressView stockFrag_EV_likeButton;
    private com.github.aakira.expandablelayout.ExpandableLinearLayout stockFrag_EL_expandableLayout;

    // Additional data
    private TextView stockFrag_TV_open;
    private TextView stockFrag_TV_prevClose;
    private TextView stockFrag_TV_high;
    private TextView stockFrag_TV_low;
    private TextView stockFrag_TV_vol;
    private TextView stockFrag_TV_yRange;

    private List<MaterialButton> buttonList; // segmentedControl implementation
    private List<Float> originalStockChart;
    private CallBack_HttpTasks callBack_httpTasks;

    // Prediction Additional details:
    private TextView stockFrag_TV_mondayTitle;
    private TextView stockFrag_TV_tuesdayTitle;
    private TextView stockFrag_TV_wednesdayTitle;
    private TextView stockFrag_TV_thursdayTitle;
    private TextView stockFrag_TV_fridayTitle;
    private TextView stockFrag_TV_mondayPrediction;
    private TextView stockFrag_TV_tuesdayPrediction;
    private TextView stockFrag_TV_wednesdayPrediction;
    private TextView stockFrag_TV_thursdayPrediction;
    private TextView stockFrag_TV_fridayPrediction;
    private TextView stockFrag_TV_mondayActual;
    private TextView stockFrag_TV_tuesdayActual;
    private TextView stockFrag_TV_wednesdayActual;
    private TextView stockFrag_TV_thursdayActual;
    private TextView stockFrag_TV_fridayActual;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Gson gson = new Gson();
            String jsonStock = getArguments().getString(ARG_PARAM);
            stock = gson.fromJson(jsonStock, Stock.class);
            originalStockChart = stock.getChartData();
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
        Log.d("stock_fragment", "onCreate: stock = "+stock.getValue());
        setStockData(stock);
        try {
            setExtraData();
            getHistoricPredictionRatio();
        } catch (JSONException e) {
            Log.e("stock_fragment", "JSONException error = " + e);
        }
        initSegmentButtons(view);
        return view;
    }

    private void updateStockData(JSONObject json) throws JSONException {
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
        stockFrag_BarChart = view.findViewById(R.id.stockFrag_BarChart);
        stockFrag_IMG_predictionStatus = view.findViewById(R.id.stockFrag_IMG_predictionStatus);
        stockFrag_EV_likeButton = view.findViewById(R.id.stockFrag_EV_likeButton);
        stockFrag_TV_open = view.findViewById(R.id.stockFrag_TV_open);
        stockFrag_TV_prevClose = view.findViewById(R.id.stockFrag_TV_prevClose);
        stockFrag_TV_high = view.findViewById(R.id.stockFrag_TV_high);
        stockFrag_TV_low = view.findViewById(R.id.stockFrag_TV_low);
        stockFrag_TV_vol = view.findViewById(R.id.stockFrag_TV_vol);
        stockFrag_TV_yRange = view.findViewById(R.id.stockFrag_TV_yRange);
        stockFrag_TV_predictionValue = view.findViewById(R.id.stockFrag_TV_predictionValue);
        stockFrag_EL_expandableLayout = view.findViewById(R.id.stockFrag_EL_expandableLayout);
        stockFrag_BTN_expandButton = view.findViewById(R.id.stockFrag_BTN_expandButton);

        // Prediction Additional details:
        this.stockFrag_TV_mondayPrediction = view.findViewById(R.id.stockFrag_TV_mondayPrediction);
        this.stockFrag_TV_tuesdayPrediction = view.findViewById(R.id.stockFrag_TV_tuesdayPrediction);
        this.stockFrag_TV_wednesdayPrediction = view.findViewById(R.id.stockFrag_TV_wednesdayPrediction);
        this.stockFrag_TV_thursdayPrediction = view.findViewById(R.id.stockFrag_TV_thursdayPrediction);
        this.stockFrag_TV_fridayPrediction = view.findViewById(R.id.stockFrag_TV_fridayPrediction);
        this.stockFrag_TV_mondayActual = view.findViewById(R.id.stockFrag_TV_mondayActual);
        this.stockFrag_TV_tuesdayActual = view.findViewById(R.id.stockFrag_TV_tuesdayActual);
        this.stockFrag_TV_wednesdayActual = view.findViewById(R.id.stockFrag_TV_wednesdayActual);
        this.stockFrag_TV_thursdayActual = view.findViewById(R.id.stockFrag_TV_thursdayActual);
        this.stockFrag_TV_fridayActual = view.findViewById(R.id.stockFrag_TV_fridayActual);
        this.stockFrag_TV_mondayTitle = view.findViewById(R.id.stockFrag_TV_mondayTitle);
        this.stockFrag_TV_tuesdayTitle = view.findViewById(R.id.stockFrag_TV_tuesdayTitle);
        this.stockFrag_TV_wednesdayTitle = view.findViewById(R.id.stockFrag_TV_wednesdayTitle);
        this.stockFrag_TV_thursdayTitle = view.findViewById(R.id.stockFrag_TV_thursdayTitle);
        this.stockFrag_TV_fridayTitle = view.findViewById(R.id.stockFrag_TV_fridayTitle);


    }

    private void initViews() {
        RapidApi.getInstance().observe(this);
        stockFrag_EV_likeButton.setOnCheckListener(new OnCheckListener() {
            @Override
            public void onChecked(@org.jetbrains.annotations.Nullable ExpressView expressView) {
                ArrayList<Stock> stocks = getUser().getFavStocks();
                stock = stock.setChartData(originalStockChart);
                Log.d("stock_fragment", "onStockLike: stocks = "+stocks);
                if(stocks.add(stock)) {
                    updateUser(getUser().setFavStocks(stocks));
                    Log.e("stock_fragment", "onStockLike: user="+getUser());
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
        stockFrag_BTN_expandButton.setOnClickListener(v -> {
            stockFrag_EL_expandableLayout.toggle();
            if(stockFrag_EL_expandableLayout.isExpanded()) {
                //stockFrag_EL_expandableLayout.setExpanded(false);
                setImg("down_arrow",stockFrag_BTN_expandButton);
            } else {
                //stockFrag_EL_expandableLayout.setExpanded(true);
                setImg("up_arrow",stockFrag_BTN_expandButton);
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
                        Log.d("stock_fragment", "markAsLiked: symbol="+stock.getSymbol() + ", likedStocks="+getUser().getFavStocks());
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
    private String getStockChangeDetails(double stockChangeAmount, double stockChangePercent){
        if(stockChangeAmount == 0) {
            return "None.";

        }
        String sign = (stockChangeAmount> 0) ? "+" : "";
        return sign+String.format("%.2f", stockChangeAmount) + "(" + String.format("%.2f", stockChangePercent)+ "%)";
    }
    private void setPredcitionPercent(double points,TextView textView) {
        String sign = (points> 0) ? "+" : (points< 0) ? "-": "";
        if(!sign.equals("")) {
            textView.setText(sign);
            setTextViewColor(textView);
            textView.setText(String.format("%.2f", Math.abs(points)*100)+"%");
        } else {
            textView.setText("None");
        }

    }

    private String getStringSignedValue(double value) {
        if(value == 0) {
            return "None.";
        }
        String sign = (value> 0) ? "+" : "";
        return sign+String.format("%.2f", value);
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

    private void initSegmentButtons(View view) {

        buttonList = new ArrayList<>();
        MaterialButton weekBtn = (MaterialButton) view.findViewById(R.id.stockFrag_BTN_week);
        MaterialButton monthBtn = (MaterialButton) view.findViewById(R.id.stockFrag_BTN_month);
        MaterialButton yearBtn = (MaterialButton) view.findViewById(R.id.stockFrag_BTN_year);

        weekBtn.setOnClickListener(v -> {
            onSegmentionSelect(v);
        });
        monthBtn.setOnClickListener(v-> {onSegmentionSelect(v);});
        yearBtn.setOnClickListener(v->{onSegmentionSelect(v);});

        buttonList.add(weekBtn);
        buttonList.add(monthBtn);
        buttonList.add(yearBtn);
    }

    private void onSegmentionSelect(View v) {
        String range = "";
        switch (v.getId()) {
            case R.id.stockFrag_BTN_week:
                buttonList.get(0).setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.light_gray)));
                buttonList.get(0).setTextColor(getResources().getColor(R.color.light_gray));
                buttonList.get(1).setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.purple_800)));
                buttonList.get(1).setTextColor(getResources().getColor(R.color.purple_800));
                buttonList.get(2).setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.purple_800)));
                buttonList.get(2).setTextColor(getResources().getColor(R.color.purple_800));
                range = "5d";
                break;
            case R.id.stockFrag_BTN_month:
                buttonList.get(0).setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.purple_800)));
                buttonList.get(0).setTextColor(getResources().getColor(R.color.purple_800));
                buttonList.get(1).setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.light_gray)));
                buttonList.get(1).setTextColor(getResources().getColor(R.color.light_gray));
                buttonList.get(2).setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.purple_800)));
                buttonList.get(2).setTextColor(getResources().getColor(R.color.purple_800));
                range = "1mo";
                break;
            case R.id.stockFrag_BTN_year:
                buttonList.get(0).setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.purple_800)));
                buttonList.get(0).setTextColor(getResources().getColor(R.color.purple_800));
                buttonList.get(1).setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.purple_800)));
                buttonList.get(1).setTextColor(getResources().getColor(R.color.purple_800));
                buttonList.get(2).setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.light_gray)));
                buttonList.get(2).setTextColor(getResources().getColor(R.color.light_gray));
                range = "1y";
                break;
            default:
                range = "5d";
                break;
        }
        refreshChart(range);
    }

    private void refreshChart(String range) {
        RapidApi.getInstance().httpGetChartCustomRange(stock.getSymbol(), range, new CallBack_HttpTasks() {
            @Override
            public void onResponse(JSONObject json) {
                Stock displayStock = stock;
                displayStock.setChartData(json);
                initChart(stock,stockFrag_BarChart);
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }



    private void initChart(Stock stock, com.github.mikephil.charting.charts.LineChart chart) {
        Log.d("stock_fragment", "setStockChart:, stockSymbol= " + stock.getSymbol());

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
        Log.d("stock_fragment", "setStockChart:, data= " + data);

        ArrayList<Entry> lineEntries = new ArrayList<Entry>();
        for (int i = 0; i < data.size() ; i++) {
            lineEntries.add(new Entry(i, data.get(i)));
        }

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Stock Price");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setLineWidth(2);
        lineDataSet.setColor(getContext().getColor(R.color.light_gray));
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setHighLightColor(Color.RED);
        if(lineEntries.size()< 7) {
            lineDataSet.setValueTextSize(8);
            lineDataSet.setCircleRadius(3);
            lineDataSet.setCircleHoleRadius(2);
            lineDataSet.setCircleColor(getContext().getColor(R.color.light_gray));
            lineDataSet.setCircleHoleColor(getContext().getColor(R.color.purple_900));
            lineDataSet.setValueTextColor(getContext().getColor(R.color.light_gray));
        } else {
            lineDataSet.setValueTextSize(0);
            lineDataSet.setDrawCircleHole(false);
            lineDataSet.setDrawCircles(false);
        }

        LineData lineData = new LineData(lineDataSet);
// usage on whole data object
        lineData.setValueFormatter(new MyValueFormatter());

// usage on individual dataset object
        lineDataSet.setValueFormatter(new MyValueFormatter());
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.animateY(1000);
        chart.getXAxis().setGranularityEnabled(true);
        chart.getXAxis().setGranularity(1.0f);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(true);
        chart.getXAxis().setEnabled(false);
        chart.setData(lineData);
        chart.setNoDataTextColor(getContext().getColor(R.color.light_gray));
        chart.getAxisLeft().setTextColor(getContext().getColor(R.color.light_gray));
        chart.getLegend().setTextColor(getContext().getColor(R.color.light_gray));

        chart.invalidate();
    }

    @Override
    public void onResponse(JSONObject json) {
        try {
            String operation = json.getString("operation");
            if(operation.equalsIgnoreCase(RapidApi.STOCK_OPERATION.GET_QUOTES.name()))
            {
                updateStockData(json);
                setStockData(stock);
                setExtraData();
            } else if (operation.equalsIgnoreCase(RapidApi.STOCK_OPERATION.GET_CHART.name())) {
                setStockChart(stock);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e("stock_fragment", "onErrorResponse: error= "+ error);
    }

    private void setStockData(Stock stock){
        stockFrag_TV_value.setText("$" + stock.getValue());
        stockFrag_TV_StockStatusDetails.setText(getStockChangeDetails(stock.getChangeAmount(),stock.getChangePercent()));
        setTextViewColor(stockFrag_TV_StockStatusDetails);
        //.setText(getStockChangeDetails(stock.getPredictionValue(),stock.calcPercentageChange(stock.getPredictionValue(), stock.getValue()), holder.RVROW_LBL_StockPredictionDetails));
        setImg(stock.getStockImg(),stockFrag_IMG_stockImg);
        setStockStatusImg(stockFrag_IMG_predictionStatus,stock.getPredictionStatus(),"prediction_status");
        stockFrag_TV_name.setText(stock.getName());
        stockFrag_TV_symbol.setText(stock.getSymbol());
        //stockFrag_TV_predictionValue.setText(getStockChangeDetails(stock.getPredictionValue(),stock.calcPercentageChange(stock.getPredictionValue(), stock.getValue())));
        setPredcitionPercent(stock.getPredictionValue(),stockFrag_TV_predictionValue);
        setStockChart(stock);
        markLikedStocks(stock);
    }

    private void setExtraData() throws JSONException {
        JSONObject jsonStockData = null;
        final DecimalFormat df = new DecimalFormat("0.000");
        try {
            jsonStockData = MyPreference.getInstance(getContext()).getStocksData(MyPreference.StockCacheManager.CACHE_KEYS.STOCKS_DATA_JSON).getJSONObject("stocks").getJSONObject(stock.getSymbol());
        } catch (NullPointerException e) {}
        if(jsonStockData!=null) {
            Log.d("stock_fragment", "setExtraData: " + "Open:\t" + jsonStockData.getString("open"));
            stockFrag_TV_open.setText("Open: \t" + df.format(Double.parseDouble(jsonStockData.getString("open"))));
            stockFrag_TV_prevClose.setText("Previous close: \t" + df.format(Double.parseDouble(jsonStockData.getString("prev_close"))));
            stockFrag_TV_high.setText("High: \t" + df.format(Double.parseDouble(jsonStockData.getString("high"))));
            stockFrag_TV_low.setText("Low: \t" + df.format(Double.parseDouble(jsonStockData.getString("low"))));
            stockFrag_TV_vol.setText("Vol \t" + df.format(Double.parseDouble(jsonStockData.getString("vol"))));
            String[] range = jsonStockData.getString("year_range").trim().split("-");
            stockFrag_TV_yRange.setText("Yearly range: \t" + df.format(Double.parseDouble(range[0])) + " - " + df.format(Double.parseDouble(range[1])));
        }
    }

    private void getHistoricPredictionRatio() throws JSONException {
        /**
         * Data to display:
         * day
         * value
         * pridiction (if there was a prediction)
         * actual (if there was a prediction)
         */
        MyFireBaseServices.getInstance().listenPredictions(new MyFireBaseServices.FB_Request_Callback<HashMap<String, ArrayList<Prediction>>>() {
            @Override
            public void OnSuccess(HashMap<String, ArrayList<Prediction>> result) {

                try {
                    JSONObject jsonStockData = MyPreference.getInstance(getContext()).getStocksData(MyPreference.StockCacheManager.CACHE_KEYS.CHARTS_DATA_JSON+stock.getSymbol()).getJSONObject("stocks").getJSONObject(stock.getSymbol());
                    JSONArray times = jsonStockData.getJSONArray("timestamp");
                    JSONArray values = jsonStockData.getJSONArray("values");
                    Log.d("stock_fragment", "Current day= " + result);

                    if(times != null) {
                        for (int i = 0; i < times.length(); i++) {
                            String day = MyTimeStamp.timeStampToDay(Long.parseLong(times.getString(i)));
                            TextView predictionTV;
                            TextView actualTV;
                            TextView dayTv;

                            switch(day) {
                                case "Monday":
                                    predictionTV = stockFrag_TV_mondayPrediction;
                                    actualTV = stockFrag_TV_mondayActual;
                                    dayTv = stockFrag_TV_mondayTitle;
                                    break;
                                case "Tuesday":
                                    predictionTV = stockFrag_TV_tuesdayPrediction;
                                    actualTV = stockFrag_TV_tuesdayActual;
                                    dayTv = stockFrag_TV_tuesdayTitle;
                                    break;
                                case "Wednesday":
                                    predictionTV = stockFrag_TV_wednesdayPrediction;
                                    actualTV = stockFrag_TV_wednesdayActual;
                                    dayTv = stockFrag_TV_wednesdayTitle;
                                    break;
                                case "Thursday":
                                    predictionTV = stockFrag_TV_thursdayPrediction;
                                    actualTV = stockFrag_TV_thursdayActual;
                                    dayTv = stockFrag_TV_thursdayTitle;
                                    break;
                                case "Friday":
                                    predictionTV = stockFrag_TV_fridayPrediction;
                                    actualTV = stockFrag_TV_fridayActual;
                                    dayTv = stockFrag_TV_fridayTitle;
                                    break;
                                default:
                                    throw new IllegalStateException("Unexpected value: day= " + day);
                            }
                            if(day.equalsIgnoreCase("Thursday")) {
                            //if(day.equalsIgnoreCase(MyTimeStamp.getCurrentDay())) {
                                Log.d("stock_fragment", "ccccccccc Current day= " + MyTimeStamp.getCurrentDay());
                                dayTv.setTypeface(dayTv.getTypeface(), Typeface.BOLD);
                                dayTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                            }
                            double value = Double.parseDouble(values.getString(i));
                            double predictionValue = 0;
                            double actualValue = 0;
                            if(result.get(day) != null) {
                                for (Prediction p: result.get(day)) {
                                    if(stock.getSymbol().equalsIgnoreCase(p.getTargetSymbol())) {
                                        predictionValue = p.getPoints();
                                        actualValue = p.getActualValue();
                                        break;
                                    }
                                }
                            }
                            Log.d("stock_fragment", "day= " + day + ", value= "+ value + ", predictionValue= " + predictionValue + ", actualValue= " + actualValue+", index="+i);
                            // TODO: Normal values to %.2f, color the values by +/-.
                            String predictionStrVal = getStringSignedValue(predictionValue);
                            if(!predictionStrVal.equals("None."))
                                predictionStrVal = predictionStrVal+"%";
                            predictionTV.setText(predictionStrVal);
                            actualTV.setText(getStringSignedValue(actualValue));
                            setTextViewColor(predictionTV);
                            setTextViewColor(actualTV);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void OnFailure(Exception e) {

            }
        });
    }

    private class PredictionRatio {

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

    @Override
    public void onPause() {
        super.onPause();
        RapidApi.getInstance().RemoveObserver(this);
    }
}