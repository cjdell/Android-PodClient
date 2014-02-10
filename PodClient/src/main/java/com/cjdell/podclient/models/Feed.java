package com.cjdell.podclient.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.cjdell.podclient.Downloader;
import com.cjdell.podclient.TaskDelegate;

/**
 * Created by cjdell on 22/11/13.
 */
public class Feed extends SugarRecord<Feed> {

    private String  title;
    private String  description;
    private String  url;
    private String  imageUrl;
    private boolean autoSyncEnabled = true;
    private boolean autoDownloadEnabled = true;
    private int     maximumItems = 100;
    private int     castCount = 0;
    private Date    latestItemPubDate;

    @Ignore
    private List<Cast> mCasts = null;

    public static Feed addFromUrl(Context context, String url, TaskDelegate delegate) {
        Feed feed = new Feed(context);
        feed.setUrl(url);
        feed.sync(delegate);
        return feed;
    }

    public static List<Feed> getAllFeeds() {
        return find(Feed.class, null, null, null, "TITLE ASC", "1000");
    }

    public static List<Feed> getSyncFeeds() {
        return find(Feed.class, "AUTO_SYNC_ENABLED = 1", null, null, "TITLE ASC", "1000");
    }

    public Feed(Context context) {
        super(context);

        this.title = "Adding feed...";
        this.description = "Feed will appear momentarily";

        ModelLifecycleDispatcher.dispatchOnCreated(this);
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean getAutoSyncEnabled() {
        return autoSyncEnabled;
    }

    public void setAutoSyncEnabled(boolean autoSyncEnabled) {
        this.autoSyncEnabled = autoSyncEnabled;
    }

    public boolean getAutoDownloadEnabled() {
        return autoDownloadEnabled;
    }

    public void setAutoDownloadEnabled(boolean autoDownloadEnabled) {
        this.autoDownloadEnabled = autoDownloadEnabled;
    }

    public int getMaximumItems() {
        return maximumItems;
    }

    public void setMaximumItems(int maximumItems) {
        this.maximumItems = maximumItems;
    }

    // Might return null if not yet persistent
    public List<Cast> getCasts() {
        if (mCasts == null && getId() != null) {
            // Start with newest first
            String sql = "SELECT * FROM CAST WHERE FEED = " + getId() + " ORDER BY PUB_DATE DESC";
            mCasts = Cast.findWithQuery(Cast.class, sql);
        }

        return mCasts;
    }

    public int getCastCount() {
        return castCount;
    }

    public Date getLatestItemPubDate() {
        return latestItemPubDate;
    }

    private void computeCastCount() {
        // For efficiency, only use the casts array if it is already available
        if (mCasts != null) {
            int count = mCasts.size();

            // Make sure we're not overflowing
            if (count > maximumItems) {
                int itemsToDelete = count - maximumItems;

                for (int i = 0; i < itemsToDelete; i++) {
                    Cast toDelete = mCasts.get(0);
                    mCasts.remove(toDelete);
                    if (toDelete.getId() != null) toDelete.delete();
                }
            }

            castCount = count = mCasts.size();

            if (count > 0) latestItemPubDate = getCasts().get(count - 1).getPubDate();
        }
    }

    private void expireItemsCache() {
        mCasts = null;
    }

    public void sync(TaskDelegate delegate) {
        expireItemsCache();

        SyncTask syncTask = new SyncTask(delegate);
        syncTask.execute(this);
    }

    @Ignore
    private class SyncTask extends AsyncTask<Feed, Integer, Feed> {

        private Exception       mException;

        private TaskDelegate    mDelegate;

        public SyncTask(TaskDelegate delegate) {
            mDelegate = delegate;
        }

        protected Feed doInBackground(Feed... feeds) {
            Feed feed = feeds[0];

            try {
                DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

                feed.setDescription("Syncing...");

                URL url = new URL(feed.url);

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);

                XmlPullParser xpp = factory.newPullParser();

                // We will get the XML from an input stream
                xpp.setInput(url.openConnection().getInputStream(), "UTF_8");

                Cast cast = null;
                Boolean inImage = false;

                // Returns the type of current event: START_TAG, END_TAG, etc..
                int eventType = xpp.getEventType();

                //feed.mCasts = new ArrayList<Cast>();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equalsIgnoreCase("item")) {
                            cast = new Cast(feed.getContext());
                            //cast.setFeed(feed);
                        }
                        else if (xpp.getName().equalsIgnoreCase("image")) {
                            inImage = true;
                        }
                        else if (xpp.getName().equalsIgnoreCase("title")) {
                            if (cast != null) {
                                cast.setTitle(xpp.nextText().trim());
                            }
                            else {
                                feed.setTitle(xpp.nextText().trim());
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("description")) {
                            if (cast != null) {
                                cast.setDescription(xpp.nextText().trim());
                            }
                            else {
                                feed.setDescription(xpp.nextText().trim());
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("itunes:summary")) {    // Preferable to <description> as this won't contain HTML markup
                            if (cast != null) {
                                cast.setDescription(xpp.nextText().trim());
                            }
                            else {
                                feed.setDescription(xpp.nextText().trim());
                            }
                        }
//                        else if (xpp.getName().equalsIgnoreCase("link")) {
//                            if (cast != null) {
//                                cast.setDownloadUrl(xpp.nextText());
//                            }
//                        }
                        else if (xpp.getName().equalsIgnoreCase("media:content")) {
                            if (cast != null) {
                                String type = xpp.getAttributeValue(null, "type");

                                if (type == null || type.equals("audio/mpeg")) {
                                    cast.setDownloadUrl(xpp.getAttributeValue(null, "url"));
                                }
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("enclosure")) {
                            if (cast != null) {
                                cast.setDownloadUrl(xpp.getAttributeValue(null, "url"));
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("pubDate")) {
                            if (cast != null) {
                                try {
                                    cast.setPubDate(dateFormat.parse(xpp.nextText()));
                                }
                                catch (ParseException ex) {
                                    cast.setPubDate(new Date());
                                }
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("url")) {
                            if (inImage) {
                                feed.setImageUrl(xpp.nextText());
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("itunes:image")) {
                            if (cast == null) {
                                feed.setImageUrl(xpp.getAttributeValue(null, "href"));
                            }
                        }
                    }
                    else if (eventType == XmlPullParser.END_TAG) {
                        if (xpp.getName().equalsIgnoreCase("item")) {
                            //feed.mCasts.add(cast);
                            feed.addCast(cast);
                            cast = null;
                        }
                        else if (xpp.getName().equalsIgnoreCase("image")) {
                            inImage = false;
                        }
                    }

                    eventType = xpp.next(); //move to next element
                }

                feed.sortItems();

                feed.save();

                publishProgress(1);

                feed.downloadArtwork();

                return feed;
            }
            catch (FileNotFoundException e) {
                feed.setDescription("File not file");
                publishProgress(1);

                mException = e;
            }
            catch (MalformedURLException e) {
                feed.setDescription("MalformedURLException");
                publishProgress(1);

                mException = e;
            }
            catch (XmlPullParserException e) {
                feed.setDescription("XmlPullParserException");
                publishProgress(1);

                mException = e;
            }
            catch (IOException e) {
                feed.setDescription("IOException");
                publishProgress(1);

                mException = e;
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (mDelegate != null) mDelegate.taskCompletionResult(Feed.this, null);
        }

        protected void onPostExecute(Feed feed) {
            if (feed == null) {
                Log.e("Feed", "Feed is null!");
                return;
            }

            if (mDelegate != null) mDelegate.taskCompletionResult(Feed.this, feed.getId().toString());
        }
    }

    private void sortItems() {
        if (mCasts != null) {
            Collections.sort(mCasts, new Comparator<Cast>() {
                @Override
                public int compare(Cast lhs, Cast rhs) {
                    return lhs.getPubDate().compareTo(rhs.getPubDate());
                }
            });
        }
    }

    // Adds a new cast but checks for duplicates first
    private void addCast(Cast newCast) {
        if (newCast.getDownloadUrl() == null) return;

        List<Cast> casts = getCasts();

        if (casts == null) {
            mCasts = casts = new ArrayList<Cast>();
        }

        Boolean found = false;

        for (Cast cast : casts) {
            if (cast.getDownloadUrl().equals(newCast.getDownloadUrl())) {
                found = true;
            }
        }

        if (!found) casts.add(newCast);
    }

    private File getImagesPath() {
        return this.getContext().getDir("images", Context.MODE_PRIVATE);
    }

    public File getImageFilePath() {
        return new File(this.getImagesPath(), "feed-" + this.getId() + ".png");
    }

    public Boolean hasImage() {
        return this.getImageFilePath().exists();
    }

    private void downloadArtwork() {
        String url = this.getImageUrl();

        if (url != null) {
            String fileName = new File(url).getName();
            String savePath = new File(this.getImagesPath(), fileName).getPath();

            Downloader downloader = new Downloader(this.getContext());
            String result = downloader.downloadFile(url, savePath);

            if (result == "Complete") {
                Bitmap original = BitmapFactory.decodeFile(savePath);
                Bitmap resized = getResizedBitmap(original, 256);

                File output = Feed.this.getImageFilePath();

                try {
                    resized.compress(Bitmap.CompressFormat.PNG, 0, new FileOutputStream(output));
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                finally {
                    original.recycle();
                    resized.recycle();
                }
            }
        }
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        float aspect = (float)width / height;
        float scaleWidth = newWidth;
        float scaleHeight = scaleWidth / aspect;        // yeah!

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // resize the bit map
        matrix.postScale(scaleWidth / width, scaleHeight / height);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);

        return resizedBitmap;
    }

    @Override
    public void save() {
        computeCastCount();

        super.save();

        // Only save children if the cache is active
        if (mCasts != null) {
            for (Cast cast : mCasts) {
                cast.setFeedId(getId());
                cast.save();
            }
        }

        expireItemsCache();

        ModelLifecycleDispatcher.dispatchOnSaved(this);
    }

    public void delete() {
        for (Cast cast : getCasts()) {
            cast.delete();
        }

        super.delete();

        ModelLifecycleDispatcher.dispatchOnDeleted(this);
    }

}