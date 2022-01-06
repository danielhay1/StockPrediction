package com.example.stockprediction.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.stockprediction.R;
import com.example.stockprediction.objects.Stock;

public class stockFragment extends Fragment {

    public static final String ARG_PARAM = "stock";
    private Stock stock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String jsonStock = getArguments().getString(ARG_PARAM);
            // Convert String to Stock
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stock, container, false);
        findViews(view);
        return view;
    }

    private void findViews(View view) {

    }

    private void initViews() {

    }
}