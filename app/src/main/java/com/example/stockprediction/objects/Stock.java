package com.example.stockprediction.objects;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.stockprediction.apis.RapidApi;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

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
    private String name = "";
    private String symbol = ""; // Use also as stock identifier
    private double value = 0.0;
    private StockStatus predictionStatus = StockStatus.NO_DATA;
    private double predictionValue = 0.0;
    private double changeAmount = 0.0;
    private double changePercent = 0.0;
    private String stockImg = "";

    public Stock() {

    }

/*
    public Stock(String jsonStock) {
        Stock stock = JsonToStock(jsonStock);
    }
*/

    public Stock(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public Stock(String name, String symbol, double value, double previousValue, double predictionValue, StockStatus predictionStatus, String stockImg) {
        this.name = name;
        this.symbol = symbol;
        this.value = value;
        this.predictionStatus = (predictionStatus != null) ? predictionStatus : StockStatus.NO_DATA;
        this.predictionValue = predictionValue;
        this.stockImg = stockImg;
        this.changeAmount = calcStockChangeAmount(previousValue);
        this.changePercent = calcStockChangePercent(previousValue);
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

    public double getPredictionValue() {
        return predictionValue;
    }

    public Stock setPredictionValue(double predictionValue) {
        this.predictionValue = predictionValue;
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

    public Stock JsonToStock(String jsonStock) {
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
