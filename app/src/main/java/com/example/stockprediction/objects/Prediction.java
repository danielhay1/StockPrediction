package com.example.stockprediction.objects;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class Prediction implements Serializable {
    private double actualValue;
    private ArrayList<Dependency> dependencies;
    //private HashMap<String,Integer> dependencies = new HashMap<String,Integer>();  // ("appl",0) , ("nvda",0) , ("symbol",1)   // not mandatory yet
    private double points;  // not mandatory yet
    private double precision;
    private Target target;


    public Prediction() {}

    public double getActualValue() {
        return actualValue;
    }

    public Prediction setActualValue(double actualValue) {
        this.actualValue = actualValue;
        return this;
    }

    public ArrayList<Dependency> getDependencies() {
        return dependencies;
    }

    public Prediction setDependencies(ArrayList<Dependency> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public Prediction setDependencies (DataSnapshot snapshot) {
        dependencies = new ArrayList<Dependency>();
        for (DataSnapshot snap: snapshot.getChildren()) {
            Dependency dependency = new Dependency();
            dependency.setIs_profit(Integer.parseInt(snap.child("is_profit").getValue().toString()));
            dependency.setName(snap.child("name").getValue().toString());
            dependency.setSymbol(snap.child("symbol").getValue().toString());
            dependencies.add(dependency);
        }
        return this;
    }

    public double getPoints() {
        return points;
    }

    public Prediction setPoints(double points) {
        this.points = points;
        return this;
    }

    public double getPrecision() {
        return precision;
    }

    public Prediction setPrecision(double precision) {
        this.precision = precision;
        return this;
    }

    public Target getTarget() {
        return target;
    }

    public Prediction setTarget(Target target) {
        this.target = target;
        return this;
    }

    public Prediction setTarget (DataSnapshot snapshot) {
        target = new Target();
        target.setName(snapshot.child("name").getValue().toString());
        target.setSymbol(snapshot.child("symbol").getValue().toString());
        return this;
    }

    public double getChange() {
        // TODO: to implement
        return -1;
    }

    public double getPercentChange() {
        // TODO: to implement
        return -1;
    }

    public String getTargetSymbol() {
        return target.getSymbol();
    }

    @NonNull
    @Override
    public String toString() {
        return "Prediction:\n-------------\n"+ target + "\nDependencies: " +this.dependencies +"\nPoints: "+this.points+"\nPrecision: "+this.precision+"\nActualValue: "+this.actualValue;
    }


    public class Dependency {
        private int is_profit;
        private String name;
        private String symbol;


        public Dependency() {}

        public int getIs_profit() {
            return is_profit;
        }

        public Dependency setIs_profit(int is_profit) {
            this.is_profit = is_profit;
            return this;
        }

        public String getName() {
            return name;
        }

        public Dependency setName(String name) {
            this.name = name;
            return this;
        }

        public String getSymbol() {
            return symbol;
        }

        public Dependency setSymbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        @NonNull
        @Override
        public String toString() {
            return "Dependency: (Is_profit= "+is_profit+",Name= "+name+",Symbol= "+symbol+")";
        }
    }

    public class Target {
        private String name;
        private String symbol;

        public Target(){}

        public String getName() {
            return name;
        }

        public Target setName(String name) {
            this.name = name;
            return this;
        }

        public String getSymbol() {
            return symbol;
        }

        public Target setSymbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        @NonNull
        @Override
        public String toString() {
            return "Target: (Name= "+name+",Symbol= "+symbol+")";
        }
    }
}
