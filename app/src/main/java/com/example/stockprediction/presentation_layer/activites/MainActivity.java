package com.example.stockprediction.presentation_layer.activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;

import android.widget.TextView;

import com.example.stockprediction.R;
import com.example.stockprediction.data_access_layer.firebase.MyFireBaseServices;
import com.example.stockprediction.data_access_layer.firebase.PushNotificationService;
import com.example.stockprediction.presentation_layer.fragments.AboutUsFragment;
import com.example.stockprediction.presentation_layer.fragments.PreferencesFragment;
import com.example.stockprediction.presentation_layer.fragments.StockFragment;
import com.example.stockprediction.presentation_layer.fragments.StockRecyclerFragment.FavoritiesFragment;
import com.example.stockprediction.presentation_layer.fragments.StockRecyclerFragment.MainFragment;
import com.example.stockprediction.presentation_layer.fragments.StockRecyclerFragment.SearchViewModel;
import com.example.stockprediction.presentation_layer.fragments.ProfileFragment;
import com.example.stockprediction.business_logic_layer.objects.User;
import com.example.stockprediction.utils.ImageTools;
import com.example.stockprediction.data_access_layer.MyPreference;
import com.example.stockprediction.utils.MySignal;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, User.OnUserUpdate {

    public interface SearchViewCallBack {
        void onTextChanged(String text);
    }

    public static final String USER = "user";
    public static final String FRAGMENT_TO_LOAD = "frgToLoad";
    private final int NULL_FRAGMENT_TO_LOAD = -1;

    private String currency = "USD";    // Will be enum currency and it would effect stock values.
    // Side menu
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private de.hdodenhof.circleimageview.CircleImageView nav_IMGVIEW_userImg;
    private TextView nav_LBL_userName;
    private TextView nav_LBL_userInfo;
    private SearchView searchView;
    private com.google.android.material.progressindicator.CircularProgressIndicator progress;
    private int displayingFragmentId;
    // User
    private User user;
    private SearchViewCallBack searchViewCallBack;
    private SearchViewModel searchViewModel;
    private Bundle state;
    private boolean isUpdatedImage = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("main_activity", "onCreate:");
        state = savedInstanceState;
        setContentView(R.layout.activity_main);
        // In case of new user
        findViews();
        progress.setVisibility(View.VISIBLE);
        progress.show();
        loadNewUser();
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        initListeners();
        initFragment(false,savedInstanceState);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void handleNoNetwork() {
        if(!isNetworkAvailable()) {
            progress.setVisibility(View.INVISIBLE);
            progress.hide();
            MySignal.getInstance().alertDialog(this,"No network","make sure internet connection is established.","Open network settings","Cancel", (dialog, which)->{
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            });
            progress.setVisibility(View.VISIBLE);
            progress.show();
        }
    }

    private void loadNewUser() {
        Gson gson = new Gson();
        if (getIntent().hasExtra(USER)) {
            String jsonUser = getIntent().getStringExtra(USER);
            user = gson.fromJson(jsonUser,User.class);
        }
    }

    private void loadFragment() {
        Intent intent = getIntent();
        int fragmentToLoad;
        if (intent.hasExtra(FRAGMENT_TO_LOAD)) {
            fragmentToLoad = getIntent().getExtras().getInt(FRAGMENT_TO_LOAD, NULL_FRAGMENT_TO_LOAD);
            if (fragmentToLoad != NULL_FRAGMENT_TO_LOAD) {
                Log.e("main_activity", "loadFragment: loading fragment: "+fragmentToLoad);
            }  else {   // Fragment to load not found -> open default fragment - MainFragment
                fragmentToLoad = R.id.nav_main;
            }
        } else { // No fragment to load sent -> open default fragment - MainFragment
            fragmentToLoad = R.id.nav_main;
        }
        navigateById(fragmentToLoad);
        navigationView.setCheckedItem(fragmentToLoad);
        progress.setVisibility(View.GONE);
        progress.hide();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        initSearchView(menu);
        //loadFragment();
        return super.onCreateOptionsMenu(menu);
    }

    private void initSearchView(Menu menu) {
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
                Log.e("main_activity" ,"onQueryTextSubmit: textChanged");
                searchViewModel.sendData(newText);
                return false;
            }
        });
    }

    private void findViews() {
        drawer = findViewById(R.id.drawer_layout);
        // nav header layout

        navigationView = findViewById(R.id.nav_view);
        progress = findViewById(R.id.progress);
        View headerLayout = navigationView.getHeaderView(0);
        nav_IMGVIEW_userImg = headerLayout.findViewById(R.id.nav_IMGVIEW_userImg);
        nav_LBL_userName = headerLayout.findViewById(R.id.nav_LBL_userName);
        nav_LBL_userInfo = headerLayout.findViewById(R.id.nav_LBL_userInfo);
    }

    private void initFragment(boolean isImgUpdated, Bundle savedInstanceState) {
        MyFireBaseServices.getInstance().loadUserFromFireBase(new MyFireBaseServices.FB_Request_Callback<User>() {
                    @Override
                    public void OnSuccess(User result) {
                        setNavBar(result,isImgUpdated);
                        if(savedInstanceState == null) {
                            loadFragment();
                            // register for notification in case it allowed on settings.
                            Boolean notificationLevelVal = MyPreference.SettingsInspector.getInstance(getApplicationContext()).getNotification_mode();
                            if(notificationLevelVal) {
                                MyFireBaseServices.getInstance().registerPredictionTopic(task -> {});
                            }
                        } else {
                            if(progress.getVisibility() == View.VISIBLE) {
                                progress.hide();
                                progress.setVisibility(View.INVISIBLE);
                            }
                        }
                    }

                    @Override
                    public void OnFailure(Exception e) {
                        Log.e("main_activity", "loadFailed: setUserDetails-exception: "+e);
                    }
                });
        handleNoNetwork();
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
        nav_IMGVIEW_userImg.setOnClickListener(v -> {
            navigateById(R.id.nav_profile);
            replaceFragment(new  ProfileFragment());
            drawer.closeDrawers();
            navigationView.setCheckedItem(R.id.nav_profile);
        });
    }

    private void setUserDetails() {
        Activity activity = this;
        MyFireBaseServices.getInstance().loadUserFromFireBase(new MyFireBaseServices.FB_Request_Callback<User>() {
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
                Log.e("main_activity", "loadFailed: setUserDetails-exception: "+e);
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

    private void replaceFragment(Fragment fragment, String argKey, String arg) {
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user,User.class);
        bundle.putString(USER, jsonUser);
        bundle.putString(argKey, arg);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                , fragment).commit();
    }

    private void enableSearchView(boolean active) {
        if(this.searchView != null) {
            int visable;
            if(active) {
                visable = searchView.VISIBLE;
                searchView.setQuery("", false);
                searchView.setIconified(active);
            } else
                visable = searchView.INVISIBLE;
            searchView.setVisibility(visable);
            searchView.setEnabled(active);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        navigateById(item.getItemId());
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openPreferenceFragment() {
        navigateById(R.xml.preferences);
        navigationView.setCheckedItem(R.id.nav_profile);
    }

    public void openStockFragment(String argKey, String arg) {
        navigateById(R.layout.fragment_stock,argKey,arg);
    }

    public void openAboutUsFragment() {
        navigateById(R.id.nav_about_us);
        navigationView.setCheckedItem(R.id.nav_about_us);
    }


    private void navigateById(int itemId, String argKey, String arg) {
        displayingFragmentId = itemId;
        Fragment fragment;
        switch (itemId) {
            case R.id.nav_main:
                Log.d("main_activity", "Switch to MainFragment");
                enableSearchView(true);
                fragment = new MainFragment();
                replaceFragment(fragment,argKey,arg);
                break;
            case R.id.nav_favorities:
                Log.d("main_activity", "Switch to FavoritiesFragment");
                enableSearchView(true);
                fragment = new FavoritiesFragment();
                replaceFragment(fragment,argKey,arg);
                break;
            case R.id.nav_profile:
                Log.d("main_activity", "Switch to ProfileFragment");
                enableSearchView(false);
                fragment = new ProfileFragment();
                replaceFragment(fragment,argKey,arg);
                break;
            case R.xml.preferences:
                Log.d("main_activity", "Switch to PreferenceFragment");
                enableSearchView(false);
                fragment = new PreferencesFragment();
                replaceFragment(fragment,argKey,arg);
                break;
            case R.layout.fragment_stock:
                Log.d("main_activity", "Switch to StockFragment");
                enableSearchView(false);
                fragment = new StockFragment();
                replaceFragment(fragment,argKey,arg);
                break;

            case R.id.nav_about_us:
                MySignal.getInstance().toast("share");
                enableSearchView(false);
                fragment = new StockFragment();
                replaceFragment(fragment,argKey,arg);
                break;

            case R.id.version:
                MySignal.getInstance().toast("Version: "+ getResources().getString(R.string.version_number));
                break;
        }
    }

    private void navigateById(int itemId) {
        displayingFragmentId = itemId;
        switch (itemId) {
            case R.id.nav_main:
                Log.d("main_activity", "Switch to mainFragment");
                enableSearchView(true);
                replaceFragment(new MainFragment());
                break;
            case R.id.nav_favorities:
                Log.d("main_activity", "Switch to favoritiesFragment");
                enableSearchView(true);
                replaceFragment(new FavoritiesFragment());
                break;
            case R.id.nav_profile:
                Log.d("main_activity", "Switch to profileFragment");
            enableSearchView(false);
            replaceFragment(new ProfileFragment());
            break;
            case R.xml.preferences:
                Log.d("main_activity", "Switch to preferenceFragment");
                enableSearchView(false);
                replaceFragment(new PreferencesFragment());
                break;
            case R.layout.fragment_stock:
                Log.d("main_activity", "Switch to preferenceFragment");
                enableSearchView(false);
                replaceFragment(new StockFragment());
                break;

            case R.id.nav_about_us:
                Log.d("main_activity", "Switch to AboutUsFragment");
                enableSearchView(false);
                replaceFragment(new AboutUsFragment());
                break;

            case R.id.version:
                MySignal.getInstance().toast("Version: "+ getResources().getString(R.string.version_number));
                break;
        }
    }

    @Override
    public void onUserUpdate(User updatedUser) {
        if(user == null) {
            MyFireBaseServices.getInstance().loadUserFromFireBase(new MyFireBaseServices.FB_Request_Callback<User>() {
                @Override
                public void OnSuccess(User result) {
                    if(!updatedUser.getImageUrl().equals(user.getImageUrl())) {
                        isUpdatedImage = false;
                        user.setImageUrl(updatedUser.getImageUrl());
                    }
                    setNavBar(updatedUser,isUpdatedImage);
                }
                @Override
                public void OnFailure(Exception e) { }
            });
        } else {
            if(!updatedUser.getImageUrl().equals(user.getImageUrl())) {
                isUpdatedImage = false;
                user.setImageUrl(updatedUser.getImageUrl());
            }
            setNavBar(updatedUser,isUpdatedImage);
        }

    }

    public void setSearchViewCallBack(
            SearchViewCallBack searchViewCallBack) {
        this.searchViewCallBack = searchViewCallBack;
    }


    @Override
    protected void onResume() {
        Log.d("main_activity", "onResume:");
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("main_activity", "onStart:");
        // Open fragment -> if not specified load main fragment.
        //loadFragment(); //-> TODO: FIX CRASH
        // navigateById(R.id.nav_favorities);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {  // If drawer is open close it.
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(displayingFragmentId == R.xml.preferences) { // If displaying fragment is preference fragment open profile fragment.
                navigateById(R.id.nav_profile);
                navigationView.setCheckedItem(R.id.nav_profile);
            } else if (displayingFragmentId == R.layout.fragment_stock) {
                if(navigationView.getCheckedItem().getItemId() == R.id.nav_favorities) {
                    navigateById(R.id.nav_favorities);
                    navigationView.setCheckedItem(R.id.nav_favorities);
                } else {
                    navigateById(R.id.nav_main);
                    navigationView.setCheckedItem(R.id.nav_main);
                }
            } else {
                if(displayingFragmentId != R.id.nav_main) { // If displaying fragment isn't main fragment open main fragment.
                    navigateById(R.id.nav_main);
                    navigationView.setCheckedItem(R.id.nav_main);
                } else {
                    super.onBackPressed();
                }
            }
        }
    }
    // CONNECT USER:


}