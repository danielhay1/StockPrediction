package com.example.stockprediction.activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;

import android.widget.TextView;

import com.example.stockprediction.R;
import com.example.stockprediction.fragments.StockRecyclerFragment.FavoritiesFragment;
import com.example.stockprediction.fragments.StockRecyclerFragment.MainFragment;
import com.example.stockprediction.fragments.StockRecyclerFragment.SearchViewModel;
import com.example.stockprediction.fragments.SettingsFragment;
import com.example.stockprediction.fragments.ProfileFragment;
import com.example.stockprediction.objects.User;
import com.example.stockprediction.utils.ImageTools;
import com.example.stockprediction.utils.MyFireBaseServices;
import com.example.stockprediction.utils.MySignal;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, User.OnUserUpdate {

    public interface SearchViewCallBack {
        void onTextChanged(String text);
    }

    public static final String USER = "user";
    private String currency = "USD";    // Will be enum currency and it would effect stock values.
    // Side menu
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private de.hdodenhof.circleimageview.CircleImageView nav_IMGVIEW_userImg;
    private TextView nav_LBL_userName;
    private TextView nav_LBL_userInfo;
    private SearchView searchView;
    // Fragments
    private MainFragment mainFragment;
    private FavoritiesFragment favoritiesFragment;
    private SettingsFragment SettingsFragment;
    // User
    private User user;

    private SearchViewCallBack searchViewCallBack;
    private SearchViewModel searchViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadAndSetNavBar(false,savedInstanceState);
        initListeners();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        // Open main fragment

        nav_IMGVIEW_userImg.setOnClickListener(v -> {
            replaceFragment(new ProfileFragment());
            drawer.closeDrawers();
            navigationView.setCheckedItem(R.id.nav_profile);
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Make View Holder Object
        searchViewModel= new ViewModelProvider(this).get(SearchViewModel.class);
        searchViewModel.init();


        getMenuInflater().inflate(R.menu.action_search,menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.e("pttt" ,"onQueryTextSubmit: textChanged");
                searchViewModel.sendData(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void findViews() {
        drawer = findViewById(R.id.drawer_layout);
        // nav header layout

        navigationView = findViewById(R.id.nav_view);
        View headerLayout = navigationView.getHeaderView(0);
        nav_IMGVIEW_userImg = headerLayout.findViewById(R.id.nav_IMGVIEW_userImg);
        nav_LBL_userName = headerLayout.findViewById(R.id.nav_LBL_userName);
        nav_LBL_userInfo = headerLayout.findViewById(R.id.nav_LBL_userInfo);
    }

    private void loadAndSetNavBar(boolean isImgUpdated,Bundle savedInstanceState) {
        MyFireBaseServices.getInstance().loadUserFromFireBase(new MyFireBaseServices.CallBack_LoadUser() {
                    @Override
                    public void OnSuccess(User result) {
                        setNavBar(result,isImgUpdated);
                        if(savedInstanceState == null) {
                            replaceFragment(new MainFragment());
                            navigationView.setCheckedItem(R.id.nav_main);
                        }
                    }

                    @Override
                    public void OnFailure(Exception e) {
                        Log.e("pttt", "loadFailed: setUserDetails-exception: "+e);
                    }
                });
    }

    private void setNavBar(User user, boolean isImgUpdated) {
        if(user!=null) {
            this.user = user;
            nav_LBL_userName.setText(user.getName());
            nav_LBL_userInfo.setText(user.getEmail());
            if(!isImgUpdated) {
                if(user.getImageUrl() != null && !user.getImageUrl().equalsIgnoreCase("")) {
                    ImageTools.glideSetImageByStrUrl(this,user.getImageUrl(),nav_IMGVIEW_userImg);
                }
            }
        }
    }

    private void initListeners() {
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setUserDetails() {
        Activity activity = this;
        MyFireBaseServices.getInstance().loadUserFromFireBase(new MyFireBaseServices.CallBack_LoadUser() {
            @Override
            public void OnSuccess(User result) {
                nav_LBL_userName.setText(result.getName());
                nav_LBL_userInfo.setText(result.getEmail());
                if(result.getImageUrl() != null) {
                    ImageTools.glideSetImageByStrUrl(activity,result.getImageUrl(),nav_IMGVIEW_userImg);
                }
            }

            @Override
            public void OnFailure(Exception e) {
                Log.e("pttt", "loadFailed: setUserDetails-exception: "+e);
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user,User.class);
        bundle.putString(USER, jsonUser);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                , fragment).commit();
    }

    private void enableSearchView(boolean active) {
        if(this.searchView != null) {
            int visable;
            searchView.setEnabled(false);
            if(active)
                visable = searchView.VISIBLE;
            else
                visable = searchView.INVISIBLE;
            searchView.setVisibility(visable);
        }
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_main:
                Log.d("pttt", "Switch to mainFragment");
                enableSearchView(true);
                replaceFragment(new MainFragment());
                break;
            case R.id.nav_favorities:
                Log.d("pttt", "Switch to favoritiesFragment");
                enableSearchView(true);
                replaceFragment(new FavoritiesFragment());
                break;
            case R.id.nav_profile:
                Log.d("pttt", "Switch to profileFragment");
                enableSearchView(false);
                replaceFragment(new ProfileFragment());
                break;
            case R.id.nav_share:
                MySignal.getInstance().toast("Share");
                break;

            case R.id.nav_send:
                MySignal.getInstance().toast("Send");
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onUserUpdate(User updatedUser) {
        boolean isUpdatedImage = true;
        if(!updatedUser.getImageUrl().equals(user.getImageUrl())) {
            isUpdatedImage = false;
            user.setImageUrl(updatedUser.getImageUrl());
        }
        setNavBar(updatedUser,isUpdatedImage);
    }

    public void setSearchViewCallBack(
            SearchViewCallBack searchViewCallBack) {
        this.searchViewCallBack = searchViewCallBack;
    }

    // CONNECT USER:
}