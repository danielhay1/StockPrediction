package com.example.stockprediction.fragments.StockRecyclerFragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SearchViewModel extends ViewModel {
    private MutableLiveData<String> textMutableLiveData;

    public void init()
    {
        textMutableLiveData=new MutableLiveData<>();
    }

    public void sendData(String msg)
    {
        textMutableLiveData.setValue(msg);
    }

    public LiveData<String> getMessage()
    {
        return textMutableLiveData;
    }
}
