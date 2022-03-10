package com.example.stockprediction.utils;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class ImageTools {
    private ImageTools() {};

    public static void glideSetImageByStrUrl(Activity activity, String stringImg, ImageView imageView) {
        Log.d("pttt", "setImageUserWithUriGlide:");
        Glide.with(activity)
                .load(stringImg)
                .centerCrop()
                .into(imageView);
    }

    public static void glideSetImageByStrUrl(Activity activity, Uri uri, ImageView imageView) {
        Log.d("profile_fragment", "setImageUserWithUriGlide: ");
        Glide.with(activity)
                .load(uri)
                .centerCrop()
                .into(imageView);
    }
}
