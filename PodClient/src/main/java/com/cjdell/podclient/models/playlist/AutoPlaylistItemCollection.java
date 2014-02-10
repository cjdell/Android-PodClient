package com.cjdell.podclient.models.playlist;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.cjdell.podclient.models.Cast;
import com.cjdell.podclient.models.Playlist;

/**
 * Created by cjdell on 08/12/13.
 */
public class AutoPlaylistItemCollection extends PlaylistItemCollection {

    public AutoPlaylistItemCollection(Playlist playlist) {
        super(playlist);
    }

    @Override
    protected List<Cast> _getItems() {
        //return Cast.listAll(Cast.class);
        String sqlSort = null;

        if (getSort().equals(SORT_DATE_ASC)) {
            sqlSort = "PUB_DATE ASC";
        }
        else if (getSort().equals(SORT_DATE_DESC)) {
            sqlSort = "PUB_DATE DESC";
        }

        try {
            return Cast.find(Cast.class, null, null, null, sqlSort, "" + BATCH_SIZE);
        }
        catch (Exception ex) {
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return new ArrayList<Cast>();
        }
    }

    @Override
    protected void addItemToStore(Cast cast) {
        Toast.makeText(getContext(), "Cannot add items to this playlist", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void removeItemFromStore(Cast cast) {
        Toast.makeText(getContext(), "Cannot remove items from this playlist", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void moveItemInStore(int from, int to) {

    }

    @Override
    public boolean hasItem(Cast cast) {
        return true;
    }

}
