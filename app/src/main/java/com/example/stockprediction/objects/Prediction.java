package com.example.stockprediction.objects;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.HashMap;


public class Prediction {
    private String day;
    private String stockSymbol;
    private Dependency[] dependencies;
    //private HashMap<String,Integer> dependencies = new HashMap<String,Integer>();  // ("appl",0) , ("nvda",0) , ("symbol",1)   // not mandatory yet
    private double points;  // not mandatory yet
    private long precision;
    private double actualValue = -1;

    public Prediction(String day, String stockSymbol, Dependency[] dependencies, double chance, long precision) {
        this.day = day;
        this.stockSymbol = stockSymbol;
        this.dependencies = dependencies;
        this.points = chance;
        this.precision = precision;
    }

    public String getDay() {
        return day;
    }

    public Prediction setDay(String day) {
        this.day = day;
        return this;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public Prediction setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
        return this;
    }

    public Dependency[] getDependencies() {
        return dependencies;
    }

    public Prediction setDependencies(Dependency[] dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public double getPoints() {
        return points;
    }

    public Prediction setPoints(double chance) {
        this.points = chance;
        return this;
    }

    public double getPrecisionValue() {
        return precision;
    }

    public Prediction setPrecisionValue(double precisionValue) {
        this.precision = precision;
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
        return "Prediction:\n-------------\ndate: "+this.day+"\nstockSymbol: "+this.dependencies +"\nchance: "+this.points+"\npredictionValue: "+this.precision+"\nactualValue: "+this.actualValue;
    }

    public class Dependency {
        private int is_profit;
        private String name;
        private String symbol;

        public Dependency(int is_profit, String name, String symbol) {
            this.is_profit = is_profit;
            this.name = name;
            this.symbol = symbol;
        }

        @NonNull
        @Override
        public String toString() {
            return "Dependency: (Is_profit= "+is_profit+",Name= "+name+",Symbol= "+symbol+")";
        }
    }
}
