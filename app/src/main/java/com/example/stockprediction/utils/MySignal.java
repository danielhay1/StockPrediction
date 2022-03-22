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
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

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
        MySignal.getInstance().showNotification(this, "Title", "This is the message to display", intent, reqCode, R.mipmap.ic_launcher);

     */

    public void showNotification(Context context, String title, String message, Intent intent, int reqCode, int iconId) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_ONE_SHOT);
        String CHANNEL_ID = "channel_name";// The id of the channel.
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(iconId)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(reqCode, notificationBuilder.build()); // 0 is the request code, it should be unique id

        Log.d("pttt", "showNotification: " + reqCode);
    }
}
