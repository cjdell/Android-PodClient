package com.cjdell.podclient.services;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.widget.Toast;

import com.cjdell.podclient.TaskDelegate;
import com.cjdell.podclient.models.Cast;
import com.cjdell.podclient.models.Feed;

import java.util.Date;
import java.util.List;

/**
 * Created by cjdell on 03/02/14.
 */
public class FeedUpdater implements TaskDelegate {

    private Context mContext;

    public FeedUpdater(Context context) {
        mContext = context;
    }

    public void syncAll() {
        if (isOnMains()) {
            //Toast.makeText(mContext, "Syncing...", Toast.LENGTH_LONG).show();

            List<Feed> feeds = Feed.getSyncFeeds();

            for (Feed feed : feeds) {
                try {
                    feed.sync(this);
                }
                catch (Exception ex) {
                    Toast.makeText(mContext, "Feed sync error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            Date now = new Date();
            Date twentyFourHoursAgo  = new Date();

            twentyFourHoursAgo.setTime(now.getTime() - (1 * 24 * 60 * 60 * 1000));

            List<Cast> casts = Cast.getSince(twentyFourHoursAgo);

            for (Cast cast : casts) {
                try {
                    cast.download();
                }
                catch (Exception ex) {
                    Toast.makeText(mContext, "Cast download error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            //Toast.makeText(mContext, "Updated Feeds: " + feeds.size() + ", Casts: " + casts.size(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void taskCompletionResult(Object sender, Object result) {

    }

    private boolean isOnMains() {
        Intent intent = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) > 0;
    }

}
