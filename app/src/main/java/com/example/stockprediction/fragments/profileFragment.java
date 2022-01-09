package com.example.stockprediction.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import com.example.stockprediction.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class profileFragment extends Fragment {
    MaterialButton profile_BTN_edit;
    MaterialButton profile_BTN_Settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        findViews(view);
        initViews();
        return view;
    }

    public void findViews(View view) {
        this.profile_BTN_edit = view.findViewById(R.id.profile_BTN_edit);
        this.profile_BTN_Settings = view.findViewById(R.id.profile_BTN_Settings);
    }

    public void initViews() {
        profile_BTN_edit.setOnClickListener(v -> {
            Fragment fragment = new EditProfileFragment();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.profile_BTN_edit, fragment);
            fragmentTransaction.commit();
        });
    }

    public void changeFragment(Fragment newFragment,int layout) {
        getActivity().getSupportFragmentManager().beginTransaction().replace(layout, newFragment).commit();
    }


}