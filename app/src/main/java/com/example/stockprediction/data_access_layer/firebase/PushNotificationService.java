package com.example.stockprediction.data_access_layer.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.stockprediction.R;
import com.example.stockprediction.presentation_layer.activites.MainActivity;
import com.example.stockprediction.utils.MySignal;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushNotificationService extends FirebaseMessagingService {
    private final String CHANNEL_ID = "PREDICTIONS_PUSH";
    private final String CHANNEL_NAME = "com.example.stockprediction.data_access_layer.firebase";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d("push_notification_service", "PushNotificationService: onMessageReceived, body= "+remoteMessage.getNotification().getBody());
/*        if(remoteMessage.getNotification() != null) {
            showNotification(remoteMessage);
        }*/
        showNotification(remoteMessage);
        super.onMessageReceived(remoteMessage);
    }


    private void showNotification(RemoteMessage remoteMessage) {
        //String title = remoteMessage.getNotification().getTitle();
        String title = "New predictions";
        String text = "New predictions are available for the following stocks: " + remoteMessage.getNotification().getBody();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.FRAGMENT_TO_LOAD, R.id.nav_favorities); // -> In case of calling MainActivity, I can specify the fragment to load.
        MySignal.getInstance().showNotification(getBaseContext(), CHANNEL_ID, CHANNEL_NAME,title,text,intent,R.mipmap.ic_launcher_round,R.layout.notification);
        //displayNotification(getBaseContext(), CHANNEL_ID, CHANNEL_NAME,title,text,intent,R.mipmap.ic_launcher_round,R.layout.notification);
    }

    private void showNotification() {
        String title = "New predictions";
        String text = "New predictions are available";
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.FRAGMENT_TO_LOAD, R.id.nav_favorities); // -> In case of calling MainActivity, I can specify the fragment to load.
        MySignal.getInstance().showNotification(getBaseContext(), CHANNEL_ID, CHANNEL_NAME,title,text,intent,R.mipmap.ic_launcher_round,R.layout.notification);
    }

    public static void printToken(Context context) {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            if (!TextUtils.isEmpty(token)) {
                Log.d("push_notification_service", "retrieve token successful : " + token);
            } else{
                Log.e("push_notification_service", "token should not be null...");
            }
        });
    }
    /*

    MESSAGING DESCRIPTION:
    {
    "to": "/topic/PREDICTIONS_PUSH",
    "data": ,
    "notification": {
            "title": "text",
            "text": "text",
        }
    }

     Message body should include all stock targets seperated by ','
     */
    
}
