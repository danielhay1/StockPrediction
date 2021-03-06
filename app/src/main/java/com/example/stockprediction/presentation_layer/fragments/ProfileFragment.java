package com.example.stockprediction.presentation_layer.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.stockprediction.R;
import com.example.stockprediction.data_access_layer.firebase.MyFireBaseServices;
import com.example.stockprediction.presentation_layer.activites.MainActivity;
import com.example.stockprediction.presentation_layer.BaseFragment;
import com.example.stockprediction.business_logic_layer.objects.stock.Stock;
import com.example.stockprediction.business_logic_layer.objects.User;
import com.example.stockprediction.utils.ImageTools;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class ProfileFragment extends BaseFragment {
    private final int PERMISSION_CODE_READ = 20;
    private MaterialButton profile_BTN_edit;
    private MaterialButton profile_BTN_Settings;
    private MaterialButton profile_BTN_Logout;
    private TextView profile_TV_username;
    private TextView profile_TV_emails;
    private TextView profile_TV_favStocks;
    private FloatingActionButton profile_FAB_editImage;
    private CircleImageView imageview_account_profile;

    // Edit layout
    private LinearLayout profile_LAYOUT_editUser;

    private ActivityResultLauncher mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if(result != null) {
                        Log.d("profile_fragment", "uploadImage: uri= "+result);
                        ImageTools.glideSetImageByStrUrl(getActivity(),result,imageview_account_profile);
                        MyFireBaseServices.getInstance().savePhotoToStorage("profile-pic_"+getUser().getUid(), result, imageUri -> {
                            if(imageUri!=null) {
                                // TODO: invoke mainActivity to update image on toolbar
                                updateUser(getUser().setImageUrl(imageUri));
                            }
                        });
                    }
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        findViews(view);
        setProfile(getUser());
        initViews();
        return view;
    }

    public void findViews(View view) {
        //this.profile_BTN_edit = view.findViewById(R.id.profile_BTN_edit);
        this.profile_BTN_Settings = view.findViewById(R.id.profile_BTN_Settings);
        this.profile_BTN_Logout = view.findViewById(R.id.profile_BTN_Logout);
        this.profile_TV_username = view.findViewById(R.id.profile_TV_username);
        this.profile_TV_emails = view.findViewById(R.id.profile_TV_email);
        this.profile_TV_favStocks = view.findViewById(R.id.profile_TV_favStocks);
        this.profile_FAB_editImage = view.findViewById(R.id.profile_FAB_editImage);
        this.imageview_account_profile = view.findViewById(R.id.imageview_account_profile);
    }

    public void initViews() {
//        profile_BTN_edit.setOnClickListener(v -> {
//            Fragment fragment = new EditProfileFragment();
//            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.profile_BTN_edit, fragment);
//            fragmentTransaction.commit();
//        });
        profile_FAB_editImage.setOnClickListener(v -> uploadImage());
        profile_BTN_Logout.setOnClickListener(v-> MyFireBaseServices.getInstance().signOut(getActivity()));
        profile_BTN_Settings.setOnClickListener(v-> ((MainActivity)getActivity()).openPreferenceFragment());
    }

    private void uploadImage() {
        if(!checkPermission()) {
            Log.d("profile_fragment", "uploadImage: no permissions");
            requestStoragePermission();
        } else {
            mGetContent.launch("image/*");
        }
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        ) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[] {(Manifest.permission.READ_EXTERNAL_STORAGE)}, PERMISSION_CODE_READ);
    }

    private boolean checkPermission() {
        int result = checkSelfPermission(
                this.requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
        );
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void setProfile(User user) {
        if(user!=null) {
            profile_TV_username.setText("Username: " + user.getName());
            profile_TV_emails.setText("Email: " + user.getEmail());
            if(!user.getFavStocks().isEmpty()) {
                ArrayList<String> symbolList = new ArrayList<String>();
                for (Stock stock: user.getFavStocks()) {
                    symbolList.add(stock.getSymbol());
                }
                profile_TV_favStocks.setText("Favorite stocks: " + symbolList.toString());
            } else {
                profile_TV_favStocks.setText("Favorite stocks: None");
            }
            if(user.getImageUrl() != null && !user.getImageUrl().equalsIgnoreCase("")) {
                ImageTools.glideSetImageByStrUrl(getActivity(),user.getImageUrl(),imageview_account_profile);
            }
        }
    }
}