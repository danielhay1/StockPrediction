package com.example.stockprediction.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MyTimeStamp {
    private MyTimeStamp() {}
    public static String getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        // full name form of the day
        return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
    }
    public static String timeStampToDay(long time) {
        Date date = new Date(time*1000L);
        return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date);
    }

    public static String getDisplayDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,Calendar.DATE-1);
        if(calendar.get(calendar.HOUR_OF_DAY) >= 23 && calendar.get(calendar.MINUTE) > 10)
            return getCurrentDay();
        Date date = calendar.getTime();
        return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
        // full name form of the day
    }
}
