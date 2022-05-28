package com.example.stockprediction.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.stockprediction.R;
import com.example.stockprediction.presentation_layer.activites.MainActivity;
import com.google.firebase.messaging.RemoteMessage;

public class MySignal {
    private  static MySignal instance;
    private Context appContext;

    public static MySignal getInstance() {
        return instance;
    }

    private MySignal(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static void Init(Context appContext){
        if(instance == null) {
            Log.d("my_signal", "Init: MySignal");
            instance = new MySignal(appContext);
        }
    }

    public void toast(String msg) {
        Log.d("my_signal", "toast: "+msg);
        Toast.makeText(appContext,msg,Toast.LENGTH_SHORT).show();
    }

    public void alertDialog(Activity activity, String title, String msg, String pos, DialogInterface.OnClickListener onClickListener) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(msg)
                //set positive button
                .setPositiveButton(pos, onClickListener)
                .show();
    }

    public void alertDialog(Activity activity, String title, String msg, String pos, String neg, DialogInterface.OnClickListener onClickListener) {
                AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(msg)
                //set positive button
                .setPositiveButton(pos, onClickListener)
                //set negative button
                .setNegativeButton(neg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what should happen when negative button is clicked
                    }
                }).show();
    }

    /**
     *
     * @param context
     * @param title  --> title to show
     * @param message --> details to show
     * @param intent --> What should happen on clicking the notification
     * @param reqCode --> unique code for the notification
     *
     * How to use this method:

        Use example -> (clicking on notification will pop up fav-stocks fragment on MainActivity):

        int reqCode = 1;
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.FRAGMENT_TO_LOAD, R.id.nav_favorities); // -> In case of calling MainActivity, I can specify the fragment to load.
        MySignal.getInstance().showNotification(this, "channel_id", "Title", "This is the message to display", intent, reqCode, R.mipmap.ic_launcher);

     */

    public void showCustomViewNotification(Context context, String channelId, String channelName, String title, String message, Intent intent, int iconId, int layoutId) {
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
        RemoteViews remoteViews = new RemoteViews(appContext.getPackageName(), layoutId);
        remoteViews.setTextViewText(R.id.notification_TV_title,title);
        remoteViews.setTextViewText(R.id.notification_TV_body,body);
        remoteViews.setImageViewResource(R.id.notification_IMG_icon, iconId);
        return remoteViews;
    }

    public void showNotification(RemoteMessage remoteMessage, String  channelId, String  channelName){
        NotificationManager notificationManager = null;
        NotificationCompat.Builder mBuilder;

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();


        //Set pending intent to builder
        Intent intent = new Intent(appContext.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(appContext.getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //Notification builder
        if (notificationManager == null){
            notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notificationManager.getNotificationChannel(channelId);
            if (mChannel == null){
                mChannel = new NotificationChannel(channelId, channelName, importance);
                mChannel.enableVibration(true);
                mChannel.setLightColor(Color.GREEN);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notificationManager.createNotificationChannel(mChannel);
            }

            mBuilder = new NotificationCompat.Builder(appContext, channelId);
            mBuilder.setContentTitle(title)
                    .setColor(appContext.getColor(R.color.purple_900))
                    .setSmallIcon(R.drawable.icon_foreground_purpel)
                    .setContentText(body) //show icon on status bar
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setDefaults(Notification.DEFAULT_ALL);
        }else {
            mBuilder = new NotificationCompat.Builder(appContext);
            mBuilder.setContentTitle(title)
                    .setSmallIcon(R.drawable.icon_foreground_purpel)
                    .setContentText(body)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setDefaults(Notification.DEFAULT_VIBRATE);
        }

        notificationManager.notify(0, mBuilder.build());
    }
}
