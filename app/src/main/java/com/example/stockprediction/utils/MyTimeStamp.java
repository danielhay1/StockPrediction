package com.example.stockprediction.utils;

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

    public static String getDaysBack(int numOfDays) { //TODO: test function
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,-1*numOfDays);
        Date date = calendar.getTime();
        // full name form of the day
        return new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
    }
}
