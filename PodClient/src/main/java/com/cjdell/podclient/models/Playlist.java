package com.cjdell.podclient.models;

import android.content.Context;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.cjdell.podclient.models.playlist.AutoPlaylistItemCollection;
import com.cjdell.podclient.models.playlist.ManualPlaylistItemCollection;
import com.cjdell.podclient.models.playlist.PlaylistItemCollection;
import com.cjdell.podclient.models.playlist.SingleFeedPlaylistItemCollection;

/**
 * Created by cjdell on 03/12/13.
 */
public class Playlist extends SugarRecord<Feed> {

    @Ignore
    public static final int AUTO_PLAYLIST = 1;
    @Ignore
    public static final int MANUAL_PLAYLIST = 2;
    @Ignore
    public static final int SINGLE_FEED_PLAYLIST = 3;

    // Fields
    private     int     type = 0;
    private     String  title;
    private     Cast    currentItem = null;
    private     int     currentItemPosition;
    private     String  data;
    private     String  thumbnailPaths;
    private     Date    lastPlayed = null;

    @Ignore
    private PlaylistItemCollection mItemCollection;

    public static Collection<? extends Playlist> getAllPlaylists() {
        //return Playlist.listAll(Playlist.class);
        return find(Playlist.class, null, null, null, "LAST_PLAYED DESC", "1000");
    }

    public static List<Playlist> getAddablePlaylists() {
        return find(Playlist.class, "TYPE = ?", new Integer(MANUAL_PLAYLIST).toString());
    }

    public static Playlist getSingleFeedPlaylistForFeed(Long feedID) {
        List<Playlist> playlists = find(Playlist.class, "DATA = ?", feedID.toString());
        if (playlists.size() > 0) return playlists.get(0);
        return null;
    }

    public Playlist(Context context) {
        super(context);
    }

    public Playlist(Context context, int type) {
        super(context);

        this.type = type;
        this.title = "New Playlist";
    }

    public int getType() {
        return type;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Cast getCurrentItem() {
        return this.currentItem;
    }

    public int getCurrentItemPosition() {
        return this.currentItemPosition;
    }

//    public void setCurrentItemPosition(int position) {
//        this.currentItemPosition = position;
//    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Date getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(Date lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public void setCurrentItem(Cast cast, int position) {
        this.currentItem = cast;
        this.currentItemPosition = position;
    }

    public PlaylistItemCollection getItemCollection() {
        if (mItemCollection == null) {
            mItemCollection = getNewItemCollection();
        }

        return mItemCollection;
    }

    private PlaylistItemCollection getNewItemCollection() {
        if (type == AUTO_PLAYLIST) {
            return new AutoPlaylistItemCollection(this);
        }
        else if (type == MANUAL_PLAYLIST) {
            return new ManualPlaylistItemCollection(this);
        }
        else if (type == SINGLE_FEED_PLAYLIST) {
            return new SingleFeedPlaylistItemCollection(this);
        }
        else {
            return null;
        }
    }

    public List<File> getThumbnails() {
        List<File> thumbnails = new ArrayList<File>();

        if (thumbnailPaths == null) return thumbnails;

        String[] thumbnailPathsArr = thumbnailPaths.split("\\|");

        for (String thumbnailPath : thumbnailPathsArr) {
            File thumbnail = new File(thumbnailPath);

            if (thumbnail.exists()) thumbnails.add(thumbnail);
        }

        return thumbnails;
    }

    public void setThumbnails(List<File> thumbnails) {
        thumbnailPaths = "";

        for (File thumbnail : thumbnails) {
            if (thumbnail != null && thumbnail.exists()) {
                thumbnailPaths += thumbnail.getAbsolutePath() + "|";
            }
        }

        if (thumbnailPaths.length() > 0) thumbnailPaths = thumbnailPaths.substring(0, thumbnailPaths.length() - 1);
    }

    @Override
    public void save() {
        if (getItemCollection() != null) setThumbnails(getItemCollection().getThumbnails());

        super.save();
    }

}
