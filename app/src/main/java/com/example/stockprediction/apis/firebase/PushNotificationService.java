package com.example.stockprediction.apis.firebase;

import android.app.NotificationManager;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.example.stockprediction.R;
import com.example.stockprediction.activites.MainActivity;
import com.example.stockprediction.utils.MySignal;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushNotificationService extends FirebaseMessagingService {
    private final String CHANNEL_ID = "PREDICTIONS_PUSH";
    private final String CHANNEL_NAME = "com.example.stockprediction.apis.firebase";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if(remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String text = remoteMessage.getNotification().getBody();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra(MainActivity.FRAGMENT_TO_LOAD, R.id.nav_favorities); // -> In case of calling MainActivity, I can specify the fragment to load.
            MySignal.getInstance().showNotification(this, CHANNEL_ID, CHANNEL_NAME,title,text,intent,R.mipmap.ic_launcher_round,R.layout.notification);
        }
        super.onMessageReceived(remoteMessage);
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
     */
    
}
