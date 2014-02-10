package com.cjdell.podclient;

import com.cjdell.podclient.models.Feed;
import com.cjdell.podclient.models.ModelLifecycleDispatcher;
import com.cjdell.podclient.models.Playlist;
import com.cjdell.podclient.models.playlist.PlaylistItemCollection;
import com.cjdell.podclient.models.playlist.SingleFeedPlaylistItemCollection;
import com.orm.SugarApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cjdell on 12/12/13.
 */
public class PodClientApp extends SugarApp {

    private final List<Feed>        mFeedList = new ArrayList<Feed>();
    private final List<Playlist>    mPlaylistList = new ArrayList<Playlist>();

    private Playlist mActivePlaylist;

    private Player mPlayer;

    private List<OnFeedListChangeListener>      mOnFeedListChangeListeners = new ArrayList<OnFeedListChangeListener>();
    private List<OnPlaylistListChangeListener>  mOnPlaylistListChangeListeners = new ArrayList<OnPlaylistListChangeListener>();
    private List<OnPlaylistChangeListener>      mOnPlaylistChangeListeners = new ArrayList<OnPlaylistChangeListener>();

    public static PodClientApp current;

    public PodClientApp() {
        current = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPlayer = new Player(getApplicationContext());

        AlarmReceiver.setAlarm(getApplicationContext(), "[ON CREATE ALARM]");

        CleanUpHelper cleanUpHelper = new CleanUpHelper();

        ModelLifecycleDispatcher.addModelLifecycleListener(cleanUpHelper);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();


    }

    public List<Feed> getFeeds() {
        if (mFeedList.size() == 0) mFeedList.addAll(Feed.getAllFeeds());

        return mFeedList;
    }

    public Feed getFeed(long feedID) {
        for (Feed feed : getFeeds()) {
            if (feed.getId() != null && feed.getId().equals(feedID)) return feed;
        }

        return null;
    }

    public void addFeed(Feed feed) {
        mFeedList.add(0, feed);

        triggerFeedListChange();
    }

//    public void sortFeeds() {
//        // Sort alphabetically
//        Collections.sort(mFeedList, new Comparator<Feed>() {
//            @Override
//            public int compare(Feed lhs, Feed rhs) {
//                return lhs.getTitle().compareTo(rhs.getTitle());
//            }
//        });
//    }

    public void removeFeed(Feed feed) {
        mFeedList.remove(feed);

        if (feed.getId() != null) {
            //cleanUpFeed(feed);
            feed.delete();
        }

        triggerFeedListChange();
    }

    public List<Playlist> getPlaylists() {
        if (mPlaylistList.size() == 0) mPlaylistList.addAll(Playlist.getAllPlaylists());

        // If still zero...
        if (mPlaylistList.size() == 0) {
            // No playlists exist, make the "(All Items)" playlist
            Playlist autoPlaylist = createAutoPlaylist();
            addPlaylist(autoPlaylist);
        }

        return mPlaylistList;
    }

    public List<Playlist> getViewablePlaylists() {
        List viewablePlaylists = new ArrayList<List>();

        for (Playlist playlist : getPlaylists()) {
            if (playlist.getType() != Playlist.SINGLE_FEED_PLAYLIST) viewablePlaylists.add(playlist);
        }

        return viewablePlaylists;
    }

    public void addPlaylist(Playlist playlist) {
        mPlaylistList.add(0, playlist);

        triggerPlaylistListChange();
    }

    public void removePlaylist(Playlist playlist) {
        mPlaylistList.remove(playlist);

        if (playlist.equals(mActivePlaylist)) {
            mActivePlaylist = null;

            triggerPlaylistChange(getActivePlaylist());
        }

        playlist.delete();

        triggerPlaylistListChange();
    }

    public Playlist getActivePlaylist() {
        if (mActivePlaylist == null) {
            mActivePlaylist = getPlaylists().get(0);    // There'll always be at least the auto playlist
        }

        return mActivePlaylist;
    }

    public Player getPlayer() {
        if (!mPlayer.hasPlaylist()) {
            // Ensure the player isn't without a playlist (will default to the last played playlist)
            mPlayer.setItemCollection(getActivePlaylist().getItemCollection());
        }

        return mPlayer;
    }

    public void setActivePlaylist(Playlist playlist) {
        mActivePlaylist = playlist;

        triggerPlaylistChange(playlist);
        triggerPlaylistListChange();
    }

    // Create the sole instance of the "(All Items)" playlist
    private Playlist createAutoPlaylist() {
        Playlist autoPlaylist = new Playlist(getApplicationContext(), Playlist.AUTO_PLAYLIST);
        autoPlaylist.setTitle("(All Items)");
        autoPlaylist.save();
        return autoPlaylist;
    }

    public Playlist playFeed(Feed feed) {
        Playlist playlist = getPlaylistForFeed(feed, true);

        setActivePlaylist(playlist);

        return playlist;
    }

    public Playlist getPlaylistForFeed(Feed feed, Boolean create) {
        Playlist playlist = Playlist.getSingleFeedPlaylistForFeed(feed.getId());

        if (playlist == null) {
            if (create) {
                // Create a new one
                playlist = new Playlist(PodClientApp.getSugarContext(), Playlist.SINGLE_FEED_PLAYLIST);
                playlist.setTitle(feed.getTitle());
                ((SingleFeedPlaylistItemCollection) playlist.getItemCollection()).setFeed(feed);
                playlist.save();

                mPlaylistList.add(playlist);

                triggerPlaylistListChange();
            }
        }
        else {
            playlist = matchInstance(playlist);
        }

        return playlist;
    }

    private Playlist matchInstance(Playlist playlist) {
        if (playlist == null) return null;

        // This is a horrible hack to prevent there being two instances in memory of the same playlist
        return getPlaylists().get(getPlaylists().indexOf(playlist));
    }

    private void triggerFeedListChange() {
        for (OnFeedListChangeListener listener : mOnFeedListChangeListeners) {
            listener.onFeedListChange();
        }
    }

    private void triggerPlaylistListChange() {
        for (OnPlaylistListChangeListener listener : mOnPlaylistListChangeListeners) {
            listener.onPlaylistListChange();
        }
    }

    private void triggerPlaylistChange(Playlist playlist) {
        mPlayer.setItemCollection(mActivePlaylist.getItemCollection());

        for (OnPlaylistChangeListener listener : mOnPlaylistChangeListeners) {
            listener.onPlaylistChange(playlist.getItemCollection());
        }
    }

    public void addFeedListChangeListener(OnFeedListChangeListener listener) {
        mOnFeedListChangeListeners.add(listener);
    }

    public void addPlaylistListChangeListener(OnPlaylistListChangeListener listener) {
        mOnPlaylistListChangeListeners.add(listener);
    }

    public void addPlaylistChangeListener(OnPlaylistChangeListener listener) {
        mOnPlaylistChangeListeners.add(listener);
    }

    public void removeFeedListChangeListener(OnFeedListChangeListener listener) {
        mOnFeedListChangeListeners.remove(listener);
    }

    public void removePlaylistListChangeListener(OnPlaylistListChangeListener listener) {
        mOnPlaylistListChangeListeners.remove(listener);
    }

    public void removePlaylistChangeListener(OnPlaylistChangeListener listener) {
        mOnPlaylistChangeListeners.remove(listener);
    }

    public interface OnFeedListChangeListener {
        public void onFeedListChange();
    }

    public interface OnPlaylistListChangeListener {
        public void onPlaylistListChange();
    }

    public interface OnPlaylistChangeListener {
        public void onPlaylistChange(PlaylistItemCollection playlistItemCollection);
    }

}
