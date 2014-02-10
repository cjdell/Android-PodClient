package com.cjdell.podclient;

import android.util.Log;

import com.orm.SugarRecord;

import com.cjdell.podclient.models.Cast;
import com.cjdell.podclient.models.Feed;
import com.cjdell.podclient.models.ModelLifecycleDispatcher;
import com.cjdell.podclient.models.Playlist;
import com.cjdell.podclient.models.playlist.PlaylistItemCollection;

/**
 * Created by cjdell on 04/02/14.
 */
public class CleanUpHelper implements ModelLifecycleDispatcher.Listener {

    private static final String TAG = "CleanUpHelper";

    @Override
    public void onCreated(SugarRecord record) {
        Log.i(TAG, "Created: " + record.getClass().getName() + ", " + record.getId());
    }

    @Override
    public void onSaved(SugarRecord record) {
        Log.i(TAG, "Saved: " + record.getClass().getName() + ", " + record.getId());

        if (record instanceof Feed) {
            refreshPlaylists((Feed) record);
        }
    }

    @Override
    public void onDeleted(SugarRecord record) {
        Log.i(TAG, "Deleted: " + record.getClass().getName() + ", " + record.getId());

        if (record instanceof Feed) {
            cleanUpFeed((Feed) record);
        }
        else if (record instanceof Cast) {
            cleanUpCast((Cast) record);
        }
    }

    private void refreshPlaylists(Feed feed) {
        for (Playlist playlist : PodClientApp.current.getPlaylists()) {
            playlist.getItemCollection().invalidateItemCache();
        }
    }

    private void cleanUpFeed(Feed feed) {
        Playlist playlist = PodClientApp.current.getPlaylistForFeed(feed, false);

        if (playlist != null) PodClientApp.current.removePlaylist(playlist);
    }

    private void cleanUpCast(Cast cast) {
        for (Playlist p : PodClientApp.current.getPlaylists()) {
            PlaylistItemCollection items = p.getItemCollection();

            if (items.supportsAddItem()) {
                if (items.hasItem(cast)) {
                    items.removeItem(cast);
                    p.save();
                }
            }
        }
    }

}
