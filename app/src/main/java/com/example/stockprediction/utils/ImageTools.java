package com.example.stockprediction.utils;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageTools {
    private ImageTools() {}

    public static void glideSetImageByStrUrl(Activity activity, String stringImg, ImageView imageView) {
        Log.d("image_tools", "setImageUserWithUriGlide:");
        Glide.with(activity)
                .load(stringImg)
                .centerCrop()
                .into(imageView);
    }

    public static void glideSetImageByStrUrl(Activity activity, Uri uri, ImageView imageView) {
        Log.d("image_tools", "setImageUserWithUriGlide: ");
        Glide.with(activity)
                .load(uri)
                .centerCrop()
                .into(imageView);
    }
}
