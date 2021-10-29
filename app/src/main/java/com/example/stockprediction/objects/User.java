package com.example.stockprediction.objects;

import com.example.stockprediction.utils.MyPreference;

import java.util.ArrayList;

public class User {
    private String uid;
    private String userName;
    private String imageUrl;
    private ArrayList<Stock> favStocks = new ArrayList<Stock>();    // init arraylist on constactor

    public User() {}

    public String getUid() {
        return uid;
    }

    public User setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public User setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public User setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public User setFavStocks(ArrayList<Stock> favStocks) {
        this.favStocks = favStocks;
        return this;
    }

    private void addStock(Stock stock) {
        this.favStocks.add(stock);
        MyPreference.getInstance().putStockArrayList(this.favStocks);
    }

    private void removeStocks(Stock stock) {
        this.favStocks.remove(stock);
        MyPreference.getInstance().putStockArrayList(this.favStocks);
    }

    private void removeAllStocks() {
        this.favStocks.clear();
        MyPreference.getInstance().putStockArrayList(this.favStocks);
    }

    private void loadFavStocks() {
        this.favStocks =  MyPreference.getInstance().getUserFavStocks();
    }

    public ArrayList<Stock> getFavStocks() {
        return this.favStocks;
    }

    private void login() {
        // After user is loged in:

        // Load user details.
        // Load user favorite stocks.
    }
}
