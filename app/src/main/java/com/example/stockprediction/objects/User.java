package com.example.stockprediction.objects;


import com.example.stockprediction.objects.stock.Stock;

import java.util.ArrayList;

public class User {
    private String uid = "";
    private String email = "";
    private String name = "";
    private String imageUrl = "";
    private ArrayList<Stock> favStocks = new ArrayList<Stock>();

    public interface OnUserUpdate {
        public void onUserUpdate(User updatedUser);
    }

    public User() { }

    public String getUid() {
        return uid;
    }

    public User setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
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
        //MyPreference.getInstance().putFavStockArrayList(this.favStocks);
    }

    private void removeStocks(Stock stock) {
        this.favStocks.remove(stock);
        //MyPreference.getInstance().putFavStockArrayList(this.favStocks);
    }

    private void removeAllStocks() {
        this.favStocks.clear();
        //MyPreference.getInstance().putFavStockArrayList(this.favStocks);
    }

    public ArrayList<Stock> getFavStocks() {
        return this.favStocks;
    }

}
