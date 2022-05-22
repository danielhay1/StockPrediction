package com.example.stockprediction.business_logic_layer.objects.stock.comparators;

import com.example.stockprediction.business_logic_layer.objects.stock.Stock;

import java.util.Comparator;

public class StockNameComparator implements Comparator<Stock> {

    @Override
    public int compare(Stock o1, Stock o2) {
        return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
    }
}
