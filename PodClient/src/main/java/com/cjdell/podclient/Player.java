package com.cjdell.podclient;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.cjdell.podclient.models.Cast;
import com.cjdell.podclient.models.playlist.PlaylistItemCollection;

/**
 * Created by cjdell on 02/02/14.
 */
public class Player implements
        MediaPlayer.OnCompletionListener,
        PlaylistItemCollection.OnChangeListener {

    private Context mContext;
    private PlaylistItemCollection mItemCollection;

    private MediaPlayer mMediaPlayer;
    private boolean mPlayStarting;

    private final List<OnPlayerStatusChangeListener> mOnPlayerStatusChangeListeners = new ArrayList<OnPlayerStatusChangeListener>();

    public Player(Context context) {
        mContext = context;
    }

    public PlaylistItemCollection getItemCollection() {
        return mItemCollection;
    }

    public void setItemCollection(PlaylistItemCollection col) {
        if (mItemCollection != null && mItemCollection.equals(col)) return; // Do Nothing, it's the same playlist

        cleanUp();

        if (mItemCollection != null) mItemCollection.removeOnChangeListener(this);

        mItemCollection = col;

        if (mItemCollection != null) mItemCollection.addOnChangeListener(this);

        dispatchPlayerStatusChange();
    }

    public boolean hasPlaylist() {
        return mItemCollection != null;
    }

    public Boolean isPlaying() {
        return mPlayStarting || (mMediaPlayer != null ? mMediaPlayer.isPlaying() : false);
    }

    public void play() {
        play(null);
    }

    public void play(Cast desiredCast) {
        // Should we unpause?
        if (mMediaPlayer != null && (desiredCast == null || desiredCast.equals(mItemCollection.getCurrentItem()))) {
            unpause();   // Resume playing existing media player
        }
        else {
            Cast cast;
            int position;

            // Decide which cast we're playing
            if (desiredCast == null) {
                cast = mItemCollection.getCurrentItem();
                position = mItemCollection.getCurrentItemPosition();

                if (cast == null) {
                    cast = mItemCollection.getRelativeItem(1);
                }
            }
            else {
                // TODO: Check the cast is actually on this playlist
                cast = desiredCast;
                position = desiredCast.getPosition();
            }

            if (cast != null) {
                // Load the file and play it
                createMediaPlayer(cast, position);
            }
            else {
                Toast.makeText(mContext, "No item", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void change(int offset) {
        // Get the next item
        Cast cast = mItemCollection.getRelativeItem(offset);

        if (cast != null) {
            // Load the file and play it
            createMediaPlayer(cast, cast.getPosition());
        }
        else {
            Toast.makeText(mContext, "You have reached the end of the playlist", Toast.LENGTH_LONG).show();

            mItemCollection.setCurrentItem(null, 0);

            mItemCollection.save();

            dispatchPlayerStatusChange();
        }
    }

    public void jump(int secs) {
        seek(getCurrentItemPosition() + secs * 1000);
    }

    private void createMediaPlayer(final Cast cast, final int position) {
        File file = cast.getDownloadedFile();

        String path;

        if (file.exists()) {
            path = file.getPath();  // Download path
        }
        else {
            path = cast.getDownloadUrl();   // Stream path

            Toast.makeText(mContext, "Streaming... " + path, Toast.LENGTH_SHORT).show();
        }

        // Destroy the old media player if there is one
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        // Remember where we are
        mItemCollection.update(cast, position, -1);

        // This is so the view knows the play request has been acknowledged
        mPlayStarting = true;

        dispatchPlayerStatusChange();

        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mPlayStarting = false;

                if (position != 0) mMediaPlayer.seekTo(position);

                mMediaPlayer.start();   // Immediately start as soon as prepare is complete

                mMediaPlayer.setOnCompletionListener(Player.this);

                dispatchPlayerStatusChange();
            }
        });

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mPlayStarting = false;

                return false;
            }
        });

        try {
            mMediaPlayer.setDataSource(path);   // Set the file/url path
            mMediaPlayer.prepareAsync();        // Don't block
        }
        catch (IOException ex) {
            Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();   // Pause
            savePosition();

            dispatchPlayerStatusChange();
        }
    }

    private void unpause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();   // Unpause

            dispatchPlayerStatusChange();
        }
    }

    public void seek(int progress) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(progress);
            savePosition();

            dispatchPlayerStatusChange();
        }
    }

    private void savePosition() {
        mItemCollection.update(mItemCollection.getCurrentItem(), getCurrentItemPosition(), getDuration());
    }

    public void onCompletion(MediaPlayer mp) {
        savePosition();

        change(1);
    }

    public void addOnPlayerStatusChangeListener(OnPlayerStatusChangeListener onPlayerStatusChangeListener) {
        mOnPlayerStatusChangeListeners.add(onPlayerStatusChangeListener);
    }

    public void removePlayerStatusChangeListener(OnPlayerStatusChangeListener onPlayerStatusChangeListener) {
        mOnPlayerStatusChangeListeners.remove(onPlayerStatusChangeListener);
    }

    public int getCurrentItemPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        else {
            return mItemCollection.getCurrentItemPosition();
        }
    }

    public int getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        }
        else if (mItemCollection.getCurrentItem() != null) {
            return mItemCollection.getCurrentItem().getDuration();
        }
        else {
            return 0;
        }
    }

    public int getPlayedPercent() {
        if (mMediaPlayer != null) {
            return (mMediaPlayer.getCurrentPosition() * 100) / mMediaPlayer.getDuration();
        }
        else if (mItemCollection.getCurrentItem() != null) {
            return mItemCollection.getCurrentItem().getPlayedPercent();
        }
        else {
            return 0;
        }
    }

    private void dispatchPlayerStatusChange() {
        for (OnPlayerStatusChangeListener listener : mOnPlayerStatusChangeListeners) {
            listener.onStatusChange();
        }
    }

    private void cleanUp() {
        if (mMediaPlayer != null) {
            pause();    // This will save the position too
            killMediaPlayer();
        }
    }

    private void killMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void checkSkipItem(Cast cast) {
        // Skip an item if it is playing
        if (mItemCollection.getCurrentItem() != null && mItemCollection.getCurrentItem().equals(cast)) {
            killMediaPlayer();
            change(1);
        }
    }

    @Override
    public void onItemListChanged(PlaylistItemCollection col) {
        dispatchPlayerStatusChange();
    }

    @Override
    public void onItemAdded(PlaylistItemCollection col, Cast item) {

    }

    @Override
    public void onItemRemoving(PlaylistItemCollection col, Cast item) {
        checkSkipItem(item);
    }

    @Override
    public void onItemRemoved(PlaylistItemCollection col, Cast item) {

    }

    @Override
    public void onItemDeleted(PlaylistItemCollection col, Cast item) {

    }

    @Override
    public void onItemMoved(PlaylistItemCollection col, Cast item) {

    }

}
