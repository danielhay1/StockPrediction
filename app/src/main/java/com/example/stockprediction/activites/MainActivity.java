package com.example.stockprediction.activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stockprediction.R;
import com.example.stockprediction.fragments.favoritiesFragment;
import com.example.stockprediction.fragments.mainFragment;
import com.example.stockprediction.fragments.profileFragment;
import com.example.stockprediction.objects.User;
import com.example.stockprediction.utils.MySignal;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Side menu
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ImageView nav_IMGVIEW_userImg;
    private TextView nav_LBL_userName;
    private TextView nav_LBL_userInfo;
    // Fragments
    private mainFragment mainFragment;
    private favoritiesFragment favoritiesFragment;
    private profileFragment profileFragment;
    // User
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViews();
        initListeners();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        // Open main fragment
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                    , new mainFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_main);
        }

    }
    private void findViews() {
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        nav_IMGVIEW_userImg = findViewById(R.id.nav_IMGVIEW_userImg);
        nav_LBL_userName = findViewById(R.id.nav_LBL_userName);
        nav_LBL_userInfo = findViewById(R.id.nav_LBL_userInfo);
    }

    private void initListeners() {
        navigationView.setNavigationItemSelectedListener(this);
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
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                        , new mainFragment()).commit();
                break;
            case R.id.nav_favorities:
                Log.d("pttt", "Switch to favoritiesFragment");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                        , new favoritiesFragment()).commit();
                break;
            case R.id.nav_profile:
                Log.d("pttt", "Switch to profileFragment");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                        , new profileFragment()).commit();
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

    // CONNECT USER:

}