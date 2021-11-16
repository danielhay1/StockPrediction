package com.example.stockprediction.objects;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.stockprediction.apis.RapidApi;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import okhttp3.Call;

public class Stock {

    /**
     *  YH stock(single day):
     *  "chart" : "result":
     *  0 :
     *  - "meta" : "regularMarketPrice" -> will bring current price
     *  - "timestamp" : time in long
     *  - "indicators" : "quote"[0]:
     *  - open: long value
     *  - close: Long value
     *
     *  YH stock(multiple day):
     *  "chart" : "result"[0]:
     *  - "timestamp" : time in long
     *  - "indicators" : "quote"[0]:
     *  - open: long value
     *  - close: Long value
     */
    public enum StockStatus {
        INCREASE,
        DECREASE,
        UNCHANGED,
        NO_DATA
    }
    private String name;
    private String symbol; // Use also as stock identifier
    private double value;
    private StockStatus currentStatus;
    private StockStatus predictionStatus;
    private String stockStatusDetails;
    private String stockImg;

    public Stock() {

    }

    public Stock(String name, String symbol, double value, StockStatus currentStatus, StockStatus predictionStatus, String stockStatusDetails, String stockImg) {
        this.name = name;
        this.symbol = symbol;
        this.value = value;
        this.currentStatus = currentStatus;
        if(predictionStatus != null) {
            this.predictionStatus = predictionStatus;
        } else {
            this.predictionStatus = StockStatus.NO_DATA;
        }
        if(currentStatus != null) {
            this.predictionStatus = predictionStatus;
        } else {
            this.predictionStatus = StockStatus.NO_DATA;
        }
        this.stockStatusDetails = stockStatusDetails;
        this.stockImg = stockImg;
    }

    private void setValueFromAPI() {

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
        return this;
    }

    public double getValue() {
        return value;
    }

    public Stock setValue(double value) {
        this.value = value;
        return this;
    }

    public StockStatus getCurrentStatus() {
        return currentStatus;
    }

    public Stock setCurrentStatus(StockStatus currentStatus) {
        this.currentStatus = currentStatus;
        return this;
    }

    public StockStatus getPredictionStatus() {
        return predictionStatus;
    }

    public Stock setPredictionStatus(StockStatus predictionStatus) {
        this.predictionStatus = predictionStatus;
        return this;
    }

    public String getStockStatusDetails() {
        return stockStatusDetails;
    }

    public Stock setStockStatusDetails(String stockStatusDetails) {
        this.stockStatusDetails = stockStatusDetails;
        return this;
    }

    public String getStockImg() {
        return stockImg;
    }

    public Stock setStockImg(String stockImg) {
        this.stockImg = stockImg;
        return this;
    }

    public static Stock JsonToStock(String jsonStock) {
        Gson gson = new Gson();
        return gson.fromJson(jsonStock,Stock.class);
    }

    public String stockToJson() {
        Gson gson = new Gson();
        return gson.toJson(this,Stock.class);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        else {
            final Stock stock = (Stock) obj;
            return this.symbol == stock.symbol;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
