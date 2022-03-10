package com.example.stockprediction.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.stockprediction.R;
import com.example.stockprediction.activites.MainActivity;
import com.example.stockprediction.objects.BaseFragment;
import com.example.stockprediction.objects.User;
import com.example.stockprediction.utils.ImageTools;
import com.example.stockprediction.utils.MyFireBaseServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class profileFragment extends BaseFragment {
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
                    Log.d("profile_fragment", "uploadImage: uri= "+result);
                    ImageTools.glideSetImageByStrUrl(getActivity(),result,imageview_account_profile);
                    MyFireBaseServices.getInstance().savePhotoToStorage("profile-pic_"+getUser().getUid(), result, imageUri -> {
                        if(imageUri!=null) {
                            MyFireBaseServices.getInstance().updateUserPhotoInFireStore(imageUri,getUser());
                            // TODO: invoke mainActivity to update image on toolbar
                            updateUser(getUser());
                        }
                    });
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
        this.profile_BTN_edit = view.findViewById(R.id.profile_BTN_edit);
        this.profile_BTN_Settings = view.findViewById(R.id.profile_BTN_Settings);
        this.profile_BTN_Logout = view.findViewById(R.id.profile_BTN_Logout);
        this.profile_TV_username = view.findViewById(R.id.profile_TV_username);
        this.profile_TV_emails = view.findViewById(R.id.profile_TV_email);
        this.profile_TV_favStocks = view.findViewById(R.id.profile_TV_favStocks);
        this.profile_FAB_editImage = view.findViewById(R.id.profile_FAB_editImage);
        this.imageview_account_profile = view.findViewById(R.id.imageview_account_profile);
    }

    public void initViews() {
        profile_BTN_edit.setOnClickListener(v -> {
            Fragment fragment = new EditProfileFragment();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.profile_BTN_edit, fragment);
            fragmentTransaction.commit();
        });
        profile_FAB_editImage.setOnClickListener(v -> uploadImage());
        profile_BTN_Logout.setOnClickListener(v-> MyFireBaseServices.getInstance().signOut(getActivity()));
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
                profile_TV_favStocks.setText("Favorite stocks: " + user.getFavStocks());
            }
            if(user.getImageUrl() != null) {
                ImageTools.glideSetImageByStrUrl(getActivity(),user.getImageUrl(),imageview_account_profile);
            }
        }
    }
}