package com.example.stockprediction.business_logic_layer.objects.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockprediction.R;
import com.example.stockprediction.business_logic_layer.objects.stock.Stock;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class BaseStockRecyclerViewAdapter<T extends Stock> extends RecyclerView.Adapter<BaseStockRecyclerViewAdapter<T>.ViewHolder> {
    protected List<T> stocksData;
    protected List<T> filteredStockData;
    private HashMap<String,Integer> symbolIndexMap = new HashMap<String, Integer>();
    protected LayoutInflater mInflater;
    protected ItemClickListener mClickListener;
    protected Context context;

    // data is passed into the constructor
    public BaseStockRecyclerViewAdapter(Context context, List<T> filteredStockData) {
        this.mInflater = LayoutInflater.from(context);
        this.filteredStockData = filteredStockData;
        this.context = context;
        this.stocksData = new ArrayList<T>(filteredStockData);
        initSymbolIndexMap(stocksData);
    }

    private void initSymbolIndexMap(List<T> stockList) {
        for (int i = 0; i < stockList.size() ; i++) {
            this.symbolIndexMap.put(stockList.get(i).getSymbol(),i);
        }
    }

    public List<T> getStocksData() {
        return stocksData;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("tag-test", "onCreateViewHolder");
        View view = mInflater.inflate(R.layout.recycle_prediction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T stock = filteredStockData.get(position);
        fillStockData(stock,holder);
        setStockChart(stock,holder);
        setTextViewColor(holder.RVROW_LBL_StockStatusDetails);
        //setTextViewColor(holder.RVROW_LBL_StockPredictionDetails);

    }

    protected void setPredcitionPercent(double points,TextView textView) {
        String sign = (points> 0) ? "+" : (points< 0) ? "-": "";
        if(!sign.equals("")) {
            textView.setText(sign);
            setTextViewColor(textView);
            textView.setText(String.format("%.2f", Math.abs(points)*100)+"%");
        } else {
            textView.setText("None");
        }

    }

    protected void fillStockData(T stock, ViewHolder holder){
        holder.RVROW_LBL_StockValue.setText("$" + stock.getValue());
        holder.RVROW_LBL_StockStatusDetails.setText(getStockChangeDetails(stock.getChangeAmount(),stock.getChangePercent(), holder.RVROW_LBL_StockStatusDetails));
        //holder.RVROW_LBL_StockPredictionDetails.setText(getStockChangeDetails(stock.getPredictionValue(),stock.calcPercentageChange(stock.getPredictionValue(), stock.getValue()), holder.RVROW_LBL_StockPredictionDetails));
        setPredcitionPercent(stock.getPredictionValue(),holder.RVROW_LBL_StockPredictionDetails);
        setStockStatusImg(holder.RVROW_IMG_predictionStatus,stock.getPredictionStatus(),"prediction_status");
        holder.RVROW_LBL_StockName.setText(stock.getName());
        holder.RVROW_LBL_StockSymbol.setText(stock.getSymbol());
    }

    private void setStockChart(T stock, ViewHolder holder) {
        initChart(stock,holder.RVROW_CHART);
    }

    protected void setTextViewColor (TextView textView) {
        char sign =  textView.getText().charAt(0);
        if (sign == '-') {
            textView.setTextColor(context.getColor(R.color.red_200));
        }   else if (sign == '+') {
            textView.setTextColor(context.getColor(R.color.green_200));
        } else {
            textView.setTextColor(context.getColor(R.color.text));
        }

    }
    protected String getStockChangeDetails(double stockChangeAmount, double stockChangePercent,TextView textView){
        //String sign = (stock.getChangeAmount() > 0) ? "+" : (stock.getChangeAmount() < 0) ? "-" : "";
        if (stockChangeAmount == 0) {
            return "None.";
        } else{
            String sign = (stockChangeAmount> 0) ? "+" : "";
            return sign+String.format("%.2f", stockChangeAmount) + "(" + String.format("%.2f", stockChangePercent)+ "%)";
        }
    }


    @Override
    public int getItemCount() {
        return filteredStockData.size();
    }


    protected void setImg(String imgName,ImageView img) {
        Log.d("stock_recycler", "Setting img: img="+imgName);
        if(!imgName.equalsIgnoreCase("")) {
            Context context = mInflater.getContext();
            int resourceId = context.getResources().getIdentifier(imgName, "drawable",context.getPackageName());//initialize res and context in adapter's contructor
            img.setImageResource(resourceId);
        }
    }

    public void sortData() {
        Log.d("stock_recycler", "Sorting...");
        Collections.sort(filteredStockData);
        notifyAdapterDataSetChanged(filteredStockData);
    }

    public void sortData(Comparator<T> comparator) {
        Collections.sort(filteredStockData,comparator);
        notifyAdapterDataSetChanged(filteredStockData);
    }

    protected void setStockStatusImg(ImageView img, T.StockStatus status, String type) {
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

    protected void initChart(T stock, com.github.mikephil.charting.charts.LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);
        chart.setNoDataTextColor(context.getColor(R.color.purple_900));
        if(stock.getChartData()!=null)
            setChartData(chart,stock.getChartData());
    }

    protected void setChartData(com.github.mikephil.charting.charts.LineChart chart, List<Float> data) {
        ArrayList<Entry> lineEntries = new ArrayList<Entry>();
        for (int i = 0; i < data.size() ; i++) {
            lineEntries.add(new Entry(i, data.get(i)));
        }
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Stock Price");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setLineWidth(2);
        lineDataSet.setColor(context.getColor(R.color.purple_200));

        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setHighLightColor(Color.RED);
        lineDataSet.setValueTextSize(0);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        LineData lineData = new LineData(lineDataSet);
// usage on whole data object
        lineData.setValueFormatter(new BaseStockRecyclerViewAdapter.MyValueFormatter());

// usage on individual dataset object
        lineDataSet.setValueFormatter(new BaseStockRecyclerViewAdapter.MyValueFormatter());
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularityEnabled(true);
        chart.getXAxis().setGranularity(1.0f);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.setData(lineData);
        chart.getAxisLeft().setTextColor(context.getColor(R.color.purple_200));
        chart.getLegend().setEnabled(false);   // Hide the legend
        chart.invalidate();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        protected ImageView RVROW_IMG_predictionStatus;
        protected TextView RVROW_LBL_StockName;
        protected TextView RVROW_LBL_StockSymbol;
        protected TextView RVROW_LBL_StockValue;
        protected TextView RVROW_LBL_StockStatusDetails;
        protected TextView RVROW_LBL_StockPredictionDetails;
        protected com.github.mikephil.charting.charts.LineChart RVROW_CHART;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            RVROW_IMG_predictionStatus = itemView.findViewById(R.id.PREDROW_IMG_predictionStatus);
            RVROW_LBL_StockName = itemView.findViewById(R.id.PREDROW_LBL_StockName);
            RVROW_LBL_StockSymbol = itemView.findViewById(R.id.PREDROW_LBL_StockSymbol);
            RVROW_LBL_StockValue = itemView.findViewById(R.id.PREDROW_LBL_StockValue);
            RVROW_LBL_StockStatusDetails = itemView.findViewById(R.id.PREDROW_LBL_StockStatusDetails);
            RVROW_LBL_StockPredictionDetails = itemView.findViewById(R.id.PREDROW_LBL_StockPredictionDetails);
            RVROW_CHART = itemView.findViewById(R.id.PREDROW_CHART);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getBindingAdapterPosition());
        }
    }

    public void notifyAdapterDataSetChanged(List<T> stocks) {
        filteredStockData = stocks;
        initSymbolIndexMap(filteredStockData);
        notifyDataSetChanged();
    }

    public int getItemIndex(String symbol) { return symbolIndexMap.get(symbol); }

    // convenience method for getting data at click position
    public T getItem(int id) {
        return filteredStockData.get(id);
    }


    public void removeAt(int position) {
        if(position >= 0) {
            symbolIndexMap.remove(filteredStockData.get(position).getSymbol());
            stocksData.remove(position);
            filteredStockData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, filteredStockData.size());

            Log.d("stock_recycler", "removeAt: "+position + ", stocks ="+filteredStockData);
        } else {
            Log.d("stock_recycler", "removeAt: no element found");
        }
    }

    public void addItem(int position) {
        notifyItemInserted(filteredStockData.size()-1);
        notifyItemRangeChanged(filteredStockData.size()-1, filteredStockData.size());
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    protected class MyValueFormatter extends ValueFormatter implements IValueFormatter {

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
