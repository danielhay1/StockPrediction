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
        //MySignal.getInstance().showNotification(getBaseContext(), CHANNEL_ID, CHANNEL_NAME,title,text,intent,R.mipmap.ic_launcher_round,R.layout.notification);
        displayNotification(getBaseContext(), CHANNEL_ID, CHANNEL_NAME,title,text,intent,R.mipmap.ic_launcher_round,R.layout.notification);
    }

    private void showNotification() {
        String title = "New predictions";
        String text = "New predictions are available";
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.FRAGMENT_TO_LOAD, R.id.nav_favorities); // -> In case of calling MainActivity, I can specify the fragment to load.
       // displayNotification(this, CHANNEL_ID, CHANNEL_NAME,title,text,intent,R.mipmap.ic_launcher_round,R.layout.notification);
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

    private void displayNotification(Context context, String channelId, String channelName,String title, String message, Intent intent, int iconId, int layoutId) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.adi_icon)
                .setOnlyAlertOnce(true)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);

        notificationBuilder = notificationBuilder.setContent(getRemoteView(channelName,title,message,iconId,layoutId));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(0, notificationBuilder.build()); // 0 is the request code, it should be unique id
        Log.d("push_notification_service", "showNotification:");
    }

    private RemoteViews getRemoteView(String channgelName, String title, String body, int iconId, int layoutId) {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), layoutId);
        remoteViews.setTextViewText(R.id.notification_TV_title,title);
        remoteViews.setTextViewText(R.id.notification_TV_body,body);
        remoteViews.setImageViewResource(R.id.notification_IMG_icon, iconId);
        return remoteViews;
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
