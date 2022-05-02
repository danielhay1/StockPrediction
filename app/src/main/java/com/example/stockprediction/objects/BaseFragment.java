package com.example.stockprediction.objects;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.example.stockprediction.activites.MainActivity;
import com.example.stockprediction.apis.firebase.MyFireBaseServices;
import com.google.gson.Gson;

public class BaseFragment extends Fragment {
    private static User user;
    private User.OnUserUpdate onUserUpdate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUser(getUserFromActivity());
    }

    private User getUserFromActivity() {
        Gson gson = new Gson();
        String jsonUser = getArguments().getString(MainActivity.USER);
        return gson.fromJson(jsonUser,User.class);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onUserUpdate = (User.OnUserUpdate) context;
    }

    public User getUser() {
        return user;
    }

    private void setUser(User user) {
        this.user = user;
    }

    public void updateUser(User user) {
        setUser(user);
        MyFireBaseServices.getInstance().saveUserToFireBase(user);
        onUserUpdate.onUserUpdate(user);
    }
}
