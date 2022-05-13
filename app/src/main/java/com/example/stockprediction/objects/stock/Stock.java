package com.example.stockprediction.objects.stock;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Stock implements Comparable<Stock>{

    @Override
    public int compareTo(Stock o) {
        if(this.getPredictionStatus().getValue() != o.getPredictionStatus().getValue()) {
            return Integer.compare(o.getPredictionStatus().getValue(),this.getPredictionStatus().getValue());
        } else {
            return this.name.compareTo(o.name);
        }
    }

    public enum StockStatus {
        NO_DATA(0),
        UNCHANGED(1),
        DECREASE(2),
        INCREASE(3);

        private final int value;
        private StockStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private String name = "";
    private String symbol = ""; // Use also as stock identifier
    private double value = 0.0;
    private StockStatus predictionStatus = StockStatus.NO_DATA;
    private double predictionValue = 0.0;
    private double changeAmount = 0.0;
    private double changePercent = 0.0;
    private String stockImg = "";
    private List<Float> chartData;



    public Stock() {

    }

    public Stock(String name, String symbol) {
        this.name = name;
        this.setSymbol(symbol);
    }

    public Stock(String symbol, double value, double previousValue, double predictionValue, StockStatus predictionStatus) {
        setSymbol(symbol);
        this.value = value;
        this.predictionStatus = (predictionStatus != null) ? predictionStatus : StockStatus.NO_DATA;
        this.predictionValue = predictionValue;
        this.stockImg = symbol.toLowerCase()+"_icon";
        this.changeAmount = calcStockChangeAmount(previousValue);
        this.changePercent = calcStockChangePercent(previousValue);
    }

    private String generateImgName(String name) {
        String returnName = name.toLowerCase()+"_icon";
        returnName = returnName.replace("&","_and_");
        returnName = returnName.replace("-","_");
        return returnName;
    }

    private String timeMillisToDate(long timeMillis) {
        return String.valueOf(new Date(timeMillis));
    }

    public String getName() {
        return name;
    }

    public Stock setName(String name) {
        this.name = name;
        return this;
    }

    public String getSymbol() {
        return symbol;
    }

    public Stock setSymbol(String symbol) {
        this.symbol = symbol;
        this.setStockImg(generateImgName(symbol));
        return this;
    }

    public double getValue() {
        return value;
    }

    public Stock setValue(double value) {
        this.value = value;
        return this;
    }

    public double getPredictionValue() {
        return predictionValue;
    }

    public Stock setPredictionValue(double predictionValue) {
        this.predictionValue = predictionValue;
        this.setPredictionStatus();
        return this;
    }

    public StockStatus getPredictionStatus() {
        return predictionStatus;
    }

    public Stock setPredictionStatus() {
        this.predictionStatus = predictionValue > 0 ? predictionStatus.INCREASE : predictionValue < 0 ? predictionStatus.DECREASE : predictionValue == 0 ? predictionStatus.UNCHANGED : predictionStatus.NO_DATA;
        return this;
    }

    public double getChangeAmount() {
        return changeAmount;
    }

    public Stock setChangeAmount(double changeAmount) {
        this.changeAmount = changeAmount;
        return this;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public Stock setChangePercent(double changePercent) {
        this.changePercent = changePercent;
        return this;
    }

    public double calcStockChangeAmount(double oldValue) {
        return value - oldValue;
    }

    public double calcStockChangePercent(double oldValue) {
        return calcStockChangeAmount(oldValue) / value*100;
    }

    public String getStockImg() {
        return stockImg;
    }

    public Stock setStockImg(String stockImg) {
        this.stockImg = stockImg;
        return this;
    }

    public List<Float> getChartData() {
        return chartData;
    }

    public Stock setChartData(List<Float> chartData) {
        this.chartData = chartData;
        return this;
    }
    public Stock setChartData(JSONObject json) {
        ArrayList<Float> resultList = new ArrayList<>();
        JSONArray values = new JSONArray();
        try {
            values = json.getJSONObject("stocks").getJSONObject(getSymbol().toUpperCase()).getJSONArray("values");
            for(int i = 0; i < values.length(); i++){
                resultList.add(BigDecimal.valueOf(values.getDouble(i)).floatValue());
            }
            Log.d("stock", "setChartData: ="+resultList);

        } catch (JSONException e) {
            Log.e("stock", "setChartData: error= "+e.getLocalizedMessage());
        }
        this.chartData = resultList;
        return this;
    }


    public Stock JsonToStock(String jsonStock) {
        Gson gson = new Gson();
        return gson.fromJson(jsonStock,Stock.class);
    }

    public String stockToJson() {
        Gson gson = new Gson();
        return gson.toJson(this,Stock.class);
    }
    // static methods
    public double calcPercentageChange(double change, double value) {
        double returnValue = 0.0;
        try {
            if(value != 0) {
                returnValue = (change/value)*100;
            }
            Log.d("stock", "value " + returnValue);
        } catch (ArithmeticException e) {
            Log.d("stock", "ArithmeticException, error= " + e);
        }
        return returnValue;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        else {
            final Stock stock = (Stock) obj;
            return this.symbol.equalsIgnoreCase(stock.symbol);
        }
    }

    @Override
    public int hashCode() {
        return symbol.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "Stock: symbol= " + symbol;
    }
}
