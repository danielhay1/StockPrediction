package com.example.stockprediction.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.stockprediction.R;

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
            Log.d("pttt", "Init: MySignal");
            instance = new MySignal(appContext);
        }
    }

    public void toast(String msg) {
        Log.d("pttt", "toast: "+msg);
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

    public void showNotification(Context context, String channelId, String channelName, String title, String message, Intent intent, int iconId, int layoutId) {
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
}
