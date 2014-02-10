package com.cjdell.podclient.models.playlist;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.cjdell.podclient.models.Cast;
import com.cjdell.podclient.models.Playlist;

/**
 * Created by cjdell on 08/12/13.
 */
public abstract class PlaylistItemCollection {

    protected static final long BATCH_SIZE = 1000;

    protected static final long SORT_DATE_ASC = 1;
    protected static final long SORT_DATE_DESC = 2;

    private     Playlist    mPlaylist;
    private     List<Cast>  mItems;
    private     Long        mSort;

    // Listeners
    private final List<OnChangeListener> mOnChangeListeners = new ArrayList<OnChangeListener>();

    public PlaylistItemCollection(Playlist playlist) {
        mPlaylist = playlist;

        setSort(new Long(SORT_DATE_ASC));
    }

    public String getPlaylistTitle() {
        return mPlaylist.getTitle();
    }

    public String getPlayingTitle() {
        if (getCurrentItem() != null) return getCurrentItem().getTitle();

        return "No item";
    }

    protected abstract List<Cast> _getItems();

    protected String getData() {
        return mPlaylist.getData();
    }

    protected void setData(String data) {
        mPlaylist.setData(data);

        Log.i("PlaylistItemCollection", data);
    }

    protected Context getContext() {
        return mPlaylist.getContext();
    }

    public Boolean supportsAddItem() {
        return false;
    }

    public final void addItem(Cast cast) {
        addItemToStore(cast);

        expireItemCache();

        dispatchItemAdded(cast);
    }

    public final void removeItem(Cast cast) {
        dispatchItemRemoving(cast);

        removeItemFromStore(cast);  // This might not do anything

        expireItemCache();

        dispatchItemRemoved(cast);
    }

    public void deleteItem(Cast cast) {
        removeItem(cast);
        cast.delete();

        dispatchItemDeleted(cast);
    }

    public void moveItem(int from, int to) {
        moveItemInStore(from, to);

        expireItemCache();

        dispatchItemMoved(getItems().get(to));
    }

    protected abstract void addItemToStore(Cast cast);

    protected abstract void removeItemFromStore(Cast cast);

    protected abstract void moveItemInStore(int from, int to);

    public abstract boolean hasItem(Cast cast);

    public HashMap<Long, String> getSortOptions() {
        HashMap<Long, String> options = new HashMap<Long, String>();

        options.put(SORT_DATE_ASC, "Sort by date ascending");
        options.put(SORT_DATE_DESC, "Sort by date descending");

        return options;
    }

    public Long getSort() {
        return mSort;
    }

    public void setSort(Long sort) {
        mSort = sort;

        expireItemCache();
    }

    public Boolean isCustomOrderable() {
        return false;
    }

    public List<Cast> getItems() {
        if (mItems == null) {
            mItems = _getItems();
        }

        return mItems;
    }

    public List<File> getThumbnails() {
        List<File> thumbnails = new ArrayList<File>();

        for (Cast cast : getItems()) {
            if (!thumbnails.contains(cast.getImageFilePath())) {
                thumbnails.add(cast.getImageFilePath());
            }
        }

        return thumbnails;
    }

    // Must call me on UI thread!
    private void expireItemCache() {
        mItems = null;

        dispatchItemListChanged();
    }

    // Similar to expireItemCache but doesn't need to be called on UI thread
    public void invalidateItemCache() {
        mItems = null;
    }

    public Cast getRelativeItem(int offset) {
        if (getItems().size() == 0) {
            return null;
        }
        else if (getCurrentItem() == null) {
            // No record of last played, start at beginning
            if (getItems().size() > 0)
                return getItems().get(0);
            else
                return null;
        }
        else {
            int index = getItems().indexOf(getCurrentItem());

            int newIndex = index + offset;

            if (index == -1) {
                // No idea where we're supposed to be, go to the first in the list
                return getItems().get(0);
            }
            else if (newIndex < 0 || newIndex >= getItems().size()) {
                // Out of range...
                return null;
            }
            else {
                // Return the next item
                return mItems.get(newIndex);
            }
        }
    }

    public Playlist getPlaylist() {
        return mPlaylist;
    }

    public Cast getCurrentItem() {
        Cast currentItem = mPlaylist.getCurrentItem();

        // Try to get the same instance as the one in the list
        for (Cast item : getItems()) {
            if (item.equals(currentItem)) {
                currentItem = item;
                break;
            }
        }

        return currentItem;
    }

    public int getCurrentItemPosition() {
        return mPlaylist.getCurrentItemPosition();
    }

    public void setCurrentItem(Cast cast, int position) {
        mPlaylist.setCurrentItem(cast, position);
    }

    public int getCurrentItemIndex() {
        return getItems().indexOf(getCurrentItem());
    }

    public void update(Cast cast, int position, int duration) {
        mPlaylist.setLastPlayed(new Date());
        mPlaylist.setCurrentItem(cast, position);
        mPlaylist.save();

        cast.update(position, duration);
        cast.save();
    }

    public void save() {
        mPlaylist.save();
    }

    public void downloadAll() {
        for (Cast item : getItems()) {
            item.download();
        }
    }

    public void addOnChangeListener(OnChangeListener listener) {
        mOnChangeListeners.add(listener);
    }

    public void removeOnChangeListener(OnChangeListener listener) {
        mOnChangeListeners.remove(listener);
    }

    private void dispatchItemListChanged() {
        for (OnChangeListener listener : mOnChangeListeners) {
            listener.onItemListChanged(this);
        }
    }

    private void dispatchItemAdded(Cast item) {
        for (OnChangeListener listener : mOnChangeListeners) {
            listener.onItemAdded(this, item);
        }
    }

    private void dispatchItemRemoving(Cast item) {
        for (OnChangeListener listener : mOnChangeListeners) {
            listener.onItemRemoving(this, item);
        }
    }

    private void dispatchItemRemoved(Cast item) {
        for (OnChangeListener listener : mOnChangeListeners) {
            listener.onItemRemoved(this, item);
        }
    }

    private void dispatchItemDeleted(Cast item) {
        for (OnChangeListener listener : mOnChangeListeners) {
            listener.onItemDeleted(this, item);
        }
    }

    private void dispatchItemMoved(Cast item) {
        for (OnChangeListener listener : mOnChangeListeners) {
            listener.onItemMoved(this, item);
        }
    }

    public interface OnChangeListener {

        public void onItemListChanged(PlaylistItemCollection col);

        public void onItemAdded(PlaylistItemCollection col, Cast item);

        public void onItemRemoving(PlaylistItemCollection col, Cast item);

        public void onItemRemoved(PlaylistItemCollection col, Cast item);

        public void onItemDeleted(PlaylistItemCollection col, Cast item);

        public void onItemMoved(PlaylistItemCollection col, Cast item);

    }

}
