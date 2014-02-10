package com.cjdell.podclient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.cjdell.podclient.services.FeedUpdater;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    //final public static String MESSAGE = "message";

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");

        // Acquire the lock
        wl.acquire();

        // You can do the processing here.
        Bundle extras = intent.getExtras();

//        if (extras != null) {
//            msgStr.append(extras.getString(MESSAGE, ""));
//        }

        Format formatter = new SimpleDateFormat("hh:mm:ss a");

        Log.i(TAG, formatter.format(new Date()));

//        Feed feed = Feed.getAllFeeds().get(0);
//        Toast.makeText(context, feed.getTitle(), Toast.LENGTH_LONG).show();

        FeedUpdater feedUpdater = new FeedUpdater(context);
        feedUpdater.syncAll();

        // Release the lock
        wl.release();
    }

    public static void setAlarm(Context context, String message) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        //intent.putExtra(MESSAGE, message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60 * 60 * 1000, pendingIntent);  // Every hour
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

}
