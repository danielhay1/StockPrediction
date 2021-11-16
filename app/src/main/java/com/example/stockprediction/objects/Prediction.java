package com.example.stockprediction.objects;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.HashMap;


public class Prediction {
    private String date;
    private String stockSymbol;
    private HashMap<String,Integer> stockScenario = new HashMap<String,Integer>();  // ("appl",0) , ("nvda",0) , ("symbol",1)   // not mandatory yet
    private double chance;  // not mandatory yet
    private double predictionValue;
    private double actualValue;

    public Prediction(String date, String stockSymbol, HashMap<String, Integer> stockScenario, double chance, double predictionValue, double actualValue) {
        this.date = java.text.DateFormat.getDateTimeInstance().format(new Date());
        this.stockSymbol = stockSymbol;
        this.stockScenario = stockScenario;
        this.chance = chance;
        this.predictionValue = predictionValue;
        this.actualValue = actualValue;
    }

    public String getDate() {
        return date;
    }

    public Prediction setDate(String date) {
        this.date = date;
        return this;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public Prediction setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
        return this;
    }

    public HashMap<String, Integer> getStockScenario() {
        return stockScenario;
    }

    public Prediction setStockScenario(HashMap<String, Integer> stockScenario) {
        this.stockScenario = stockScenario;
        return this;
    }

    public double getChance() {
        return chance;
    }

    public Prediction setChance(double chance) {
        this.chance = chance;
        return this;
    }

    public double getPredictionValue() {
        return predictionValue;
    }

    public Prediction setPredictionValue(double predictionValue) {
        this.predictionValue = predictionValue;
        return this;
    }

    public double getActualValue() {
        return actualValue;
    }

    public Prediction setActualValue(double actualValue) {
        this.actualValue = actualValue;
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return "Prediction:\n-------------\ndate: "+this.date+"\nstockSymbol: "+this.stockScenario+"\nchance: "+this.chance+"\npredictionValue: "+this.predictionValue+"\nactualValue: "+this.actualValue;
    }
}
