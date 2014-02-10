package com.cjdell.podclient.models.playlist;

import java.util.ArrayList;
import java.util.List;

import com.cjdell.podclient.models.Cast;
import com.cjdell.podclient.models.Feed;
import com.cjdell.podclient.models.Playlist;

/**
 * Created by cjdell on 21/01/14.
 */
public class SingleFeedPlaylistItemCollection extends PlaylistItemCollection {

    public SingleFeedPlaylistItemCollection(Playlist playlist) {
        super(playlist);
    }

    @Override
    protected List<Cast> _getItems() {
        if (getFeedID() != null) {
            String sqlSort = null;

            if (getSort().equals(SORT_DATE_ASC)) {
                sqlSort = "PUB_DATE ASC";
            }
            else if (getSort().equals(SORT_DATE_DESC)) {
                sqlSort = "PUB_DATE DESC";
            }

            String sql = "SELECT * FROM CAST WHERE FEED = " + getFeedID();

            if (sqlSort != null) {
                sql += " ORDER BY " + sqlSort;
            }

            List<Cast> casts = Cast.findWithQuery(Cast.class, sql);

            return casts;
        }
        else {
            return new ArrayList<Cast>();
        }
    }

    public void setFeed(Feed feed) {
        setFeedID(feed.getId());
    }

    @Override
    protected void addItemToStore(Cast cast) {

    }

    @Override
    protected void removeItemFromStore(Cast cast) {

    }

    @Override
    protected void moveItemInStore(int from, int to) {

    }

    @Override
    public boolean hasItem(Cast cast) {
        return false;
    }

    private Long getFeedID() {
        String data = getData();

        if (data != null)
            return Long.parseLong(data);
        else
            return null;
    }

    private void setFeedID(Long feedID) {
        if (feedID != null)
            setData(feedID.toString());
        else
            setData(null);
    }
}
