package com.example.stockprediction.objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.util.Date;

public class Stock2 {

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
    public enum Stock2Status {
        INCREASE,
        DECREASE,
        UNCHANGED,
        NO_DATA
    }
    private String name;
    private String symbol; // Use also as stock identifier
    private long chart;
    private double value;
    private Stock2Status currentStatus;
    private Stock2Status predictionStatus;
    private String stockStatusDetails;
    private String stockImg;

    public Stock2() {

    }

    public Stock2(String name, String symbol, double value, Stock2Status currentStatus, Stock2Status predictionStatus, String stockStatusDetails, String stockImg) {
        this.name = name;
        this.symbol = symbol;
        this.value = value;
        this.currentStatus = currentStatus;
        if(predictionStatus != null) {
            this.predictionStatus = predictionStatus;
        } else {
            this.predictionStatus = Stock2Status.NO_DATA;
        }
        if(currentStatus != null) {
            this.predictionStatus = predictionStatus;
        } else {
            this.predictionStatus = Stock2Status.NO_DATA;
        }
        this.stockStatusDetails = stockStatusDetails;
        this.stockImg = stockImg;
    }

    private String timeMillisToDate(long timeMillis) {
        return String.valueOf(new Date(timeMillis));
    }

    public String getName() {
        return name;
    }

    public Stock2 setName(String name) {
        this.name = name;
        return this;
    }

    public String getSymbol() {
        return symbol;
    }

    public Stock2 setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public double getValue() {
        return value;
    }

    public Stock2 setValue(double value) {
        this.value = value;
        return this;
    }

    public Stock2Status getCurrentStatus() {
        return currentStatus;
    }

    public Stock2 setCurrentStatus(Stock2Status currentStatus) {
        this.currentStatus = currentStatus;
        return this;
    }

    public Stock2Status getPredictionStatus() {
        return predictionStatus;
    }

    public Stock2 setPredictionStatus(Stock2Status predictionStatus) {
        this.predictionStatus = predictionStatus;
        return this;
    }

    public String getStockStatusDetails() {
        return stockStatusDetails;
    }

    public Stock2 setStockStatusDetails(String stockStatusDetails) {
        this.stockStatusDetails = stockStatusDetails;
        return this;
    }

    public String getStockImg() {
        return stockImg;
    }

    public Stock2 setStockImg(String stockImg) {
        this.stockImg = stockImg;
        return this;
    }

    public static Stock2 JsonToStock(String jsonStock) {
        Gson gson = new Gson();
        return gson.fromJson(jsonStock,Stock2.class);
    }

    public String stockToJson() {
        Gson gson = new Gson();
        return gson.toJson(this,Stock2.class);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        else {
            final Stock2 stock = (Stock2) obj;
            return this.symbol == stock.symbol;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}