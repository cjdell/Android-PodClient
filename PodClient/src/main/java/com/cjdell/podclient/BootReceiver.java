package com.cjdell.podclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by cjdell on 02/02/14.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "BOOTED!!!", Toast.LENGTH_LONG).show();

        AlarmReceiver.setAlarm(context, "[BOOT ALARM]");
    }

}
