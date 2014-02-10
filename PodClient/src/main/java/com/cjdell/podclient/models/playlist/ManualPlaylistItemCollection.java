package com.cjdell.podclient.models.playlist;

import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.cjdell.podclient.models.Cast;
import com.cjdell.podclient.models.Playlist;

/**
 * Created by cjdell on 01/01/14.
 */
public class ManualPlaylistItemCollection extends PlaylistItemCollection {

    protected static final long SORT_CUSTOM = 3;

    private List<Long> mStoredItems;

    public ManualPlaylistItemCollection(Playlist playlist) {
        super(playlist);

        setSort(new Long(SORT_CUSTOM));
    }

    @Override
    public Boolean isCustomOrderable() {
        return getSort().equals(SORT_CUSTOM);
    }

    @Override
    protected List<Cast> _getItems() {
        try {
            String sqlExpr = "(" + join(getStoredItems(), ",") + ")";

            String sqlSort = null;

            if (getSort().equals(SORT_DATE_ASC)) {
                sqlSort = "PUB_DATE ASC";
            }
            else if (getSort().equals(SORT_DATE_DESC)) {
                sqlSort = "PUB_DATE DESC";
            }

            String sql = "SELECT * FROM CAST WHERE ID IN " + sqlExpr;

            if (sqlSort != null) {
                sql += " ORDER BY " + sqlSort;
            }

            List<Cast> casts = Cast.findWithQuery(Cast.class, sql);

            // Hacky way of sorting by the stored ID order
            if (getSort() == SORT_CUSTOM) {
                List<Cast> sortedCasts = new ArrayList<Cast>();

                for (Long id : getStoredItems()) {
                    Cast cast = null;

                    for (Cast c : casts) {
                        if (c.getId().equals(id)) {
                            cast = c;
                            break;
                        }
                    }

                    if (cast != null) sortedCasts.add(cast);
                }

                casts = sortedCasts;
            }

            return casts;
        }
        catch (Exception ex) {
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return new ArrayList<Cast>();
        }
    }

    static String join(Collection<?> s, String delimiter) {
        StringBuilder builder = new StringBuilder();
        Iterator<?> iter = s.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (!iter.hasNext()) {
                break;
            }
            builder.append(delimiter);
        }
        return builder.toString();
    }

    @Override
    public Boolean supportsAddItem() {
        return true;
    }

    @Override
    protected void addItemToStore(Cast cast) {
        getStoredItems().add(cast.getId());

        saveStoreItems();
    }

    @Override
    protected void removeItemFromStore(Cast cast) {
        getStoredItems().remove(cast.getId());

        saveStoreItems();
    }

    @Override
    protected void moveItemInStore(int from, int to) {
        Long id = getStoredItems().remove(from);
        getStoredItems().add(to, id);

        saveStoreItems();
    }

    @Override
    public boolean hasItem(Cast cast) {
        return getStoredItems().contains(cast.getId());
    }

    @Override
    public HashMap<Long, String> getSortOptions() {
        HashMap<Long, String> options = super.getSortOptions();

        options.put(SORT_CUSTOM, "Custom (drag sort)");

        return options;
    }

    private List<Long> getStoredItems() {
        if (mStoredItems == null) {
            String data = getData();

            if (data == null) {
                mStoredItems = new ArrayList<Long>();
            }
            else {
                try {
                    mStoredItems = (ArrayList<Long>)Base64Coder.fromString(data);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return mStoredItems;
    }

    private void saveStoreItems() {
        try {
//            for (Long i : mStoredItems) {
//                Log.i("ManualPlaylistItemCollection", "CastID: " + i);
//            }

            setData(Base64Coder.toString(mStoredItems));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
