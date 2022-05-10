package com.example.stockprediction.utils.firebase;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.stockprediction.R;
import com.example.stockprediction.activites.MainActivity;
import com.example.stockprediction.objects.User;
import com.example.stockprediction.utils.MyPreference;
import com.example.stockprediction.utils.MySignal;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushNotificationService extends FirebaseMessagingService {
    private final String CHANNEL_ID = "PREDICTIONS_PUSH";
    private final String CHANNEL_NAME = "com.example.stockprediction.utils.firebase";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d("push_notification_service", "PushNotificationService: onMessageReceived, body= "+remoteMessage.getNotification().getBody());
        if(remoteMessage.getNotification() != null) {
            SendNotificationManager(remoteMessage);
        }
        super.onMessageReceived(remoteMessage);
    }


    private void showNotification(RemoteMessage remoteMessage) {
        //String title = remoteMessage.getNotification().getTitle();
        String title = "New predictions";
        String text = "New predictions are available for the following stocks: " + remoteMessage.getNotification().getBody();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.FRAGMENT_TO_LOAD, R.id.nav_favorities); // -> In case of calling MainActivity, I can specify the fragment to load.
        MySignal.getInstance().showNotification(this, CHANNEL_ID, CHANNEL_NAME,title,text,intent,R.mipmap.ic_launcher_round,R.layout.notification);
    }

    private void showNotification() {
        String title = "New predictions";
        String text = "New predictions are available";
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.FRAGMENT_TO_LOAD, R.id.nav_favorities); // -> In case of calling MainActivity, I can specify the fragment to load.
        MySignal.getInstance().showNotification(this, CHANNEL_ID, CHANNEL_NAME,title,text,intent,R.mipmap.ic_launcher_round,R.layout.notification);
    }

    private void SendNotificationManager(RemoteMessage remoteMessage) { //TODO: finish implementaion
        String notificationLevelVal = MyPreference.SettingsInspector.getInstance(this).getNotification_mode();
        if (notificationLevelVal != null) {
            Log.d("push_notification_service", "SendNotificationManager: notificationLevelVal = " + notificationLevelVal);
            switch (notificationLevelVal.toLowerCase()) {
                case "all predictions":
                    showNotification(remoteMessage);
                    break;
                case "favorite stocks":
                    String[] symbolArr = remoteMessage.getNotification().getBody().split(",");
                    MyFireBaseServices.getInstance().loadUserFromFireBase(new MyFireBaseServices.FB_Request_Callback<User>() {
                        @Override
                        public void OnSuccess(User result) {
                            for (String symbol : symbolArr) {
                                for (int i = 0; i < result.getFavStocks().size(); i++) {
                                    if (symbol.equalsIgnoreCase(result.getFavStocks().get(i).getSymbol())) {
                                        showNotification(remoteMessage);
                                        return;
                                    }
                                }
                            }
                        }

                        @Override
                        public void OnFailure(Exception e) {
                            Log.d("push_notification_service", "SendNotificationManager: error= " + e);
                        }
                    });
                    break;
                case "none":
                    break;
            }
        }
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
