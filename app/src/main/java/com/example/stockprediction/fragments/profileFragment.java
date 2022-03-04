package com.example.stockprediction.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.stockprediction.R;
import com.example.stockprediction.objects.User;
import com.example.stockprediction.utils.MyFireBaseServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class profileFragment extends Fragment {
    private MaterialButton profile_BTN_edit;
    private MaterialButton profile_BTN_Settings;
    private MaterialButton profile_BTN_Logout;
    private TextView profile_TV_username;
    private TextView profile_TV_emails;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        findViews(view);
        setUserDetails();
        initViews();
        return view;
    }

    public void findViews(View view) {
        this.profile_BTN_edit = view.findViewById(R.id.profile_BTN_edit);
        this.profile_BTN_Settings = view.findViewById(R.id.profile_BTN_Settings);
        this.profile_BTN_Logout = view.findViewById(R.id.profile_BTN_Logout);
        this.profile_TV_username = view.findViewById(R.id.profile_TV_username);
        this.profile_TV_emails = view.findViewById(R.id.profile_TV_email);
    }

    public void initViews() {
        profile_BTN_edit.setOnClickListener(v -> {
            Fragment fragment = new EditProfileFragment();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.profile_BTN_edit, fragment);
            fragmentTransaction.commit();
        });
        profile_BTN_Logout.setOnClickListener(v-> MyFireBaseServices.getInstance().signOut(getActivity()));
    }

    private void setUserDetails() {
        MyFireBaseServices.getInstance().loadUserFromFireBase(MyFireBaseServices.getInstance().getFirebaseUser().getUid(),
                new MyFireBaseServices.CallBack_LoadUser() {
                    @Override
                    public void userDetailsUpdated(User result) {
                        profile_TV_username.setText("Username: " + result.getName());
                        profile_TV_emails.setText("Email: " + result.getEmail());
                        if(result.getImageUrl() != null) {

                        }
                    }

                    @Override
                    public void loadFailed(Exception e) {
                        Log.e("pttt", "loadFailed: setUserDetails-exception: "+e);
                    }
                });
    }

    public void changeFragment(Fragment newFragment,int layout) {
        getActivity().getSupportFragmentManager().beginTransaction().replace(layout, newFragment).commit();
    }


}