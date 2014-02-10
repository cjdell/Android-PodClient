package com.cjdell.podclient.models;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by cjdell on 22/11/13.
 */
public class Cast extends SugarRecord<Cast> {

    private Long    feed;
    private String  title;
    private String  description;
    private Date    pubDate;
    private String  downloadUrl;
    private Date    downloaded = null;
    private Date    played = null;
    private int     position = 0;   // In milliseconds
    private int     duration = 0;   // In milliseconds

    @Ignore
    private Feed    mFeed;

    public static List<Cast> getSince(Date date) {
//        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String dateString = formatter.format(date);

        //return find(         Cast.class, "PUB_DATE > ?", new String[] { "" + date.getTime() }, null, "PUB_DATE ASC", "1000");
        return findWithQuery(Cast.class,
                "SELECT     * " +
                "FROM       CAST AS C " +
                "JOIN       FEED AS F " +
                "ON         C.FEED = F.ID " +
                "WHERE      AUTO_SYNC_ENABLED = 1 " +
                "AND        AUTO_DOWNLOAD_ENABLED = 1 " +
                "AND        PUB_DATE > ?", new Long(date.getTime()).toString());
    }

    public Cast(Context context) {
        super(context);

        ModelLifecycleDispatcher.dispatchOnCreated(this);
    }

    public Long getFeedId() {
        return feed;
    }

    public void setFeedId(Long feed) {
        this.feed = feed;
        mFeed = null;
    }

    public String getTitle() {
        return title;
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

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Date getDownloaded() {
        return this.downloaded;
    }

    public void setDownloaded(Date downloaded) {
        this.downloaded = downloaded;
    }

    public Date getPlayed() {
        return this.played;
    }

//    public void setPlayed(Date played) {
//        this.played = played;
//    }

    public int getPosition() {
        return position;
    }

    public int getDuration() {
        return duration;
    }

    public int getPlayedPercent() {
        if (duration == 0) return 0;
        return (position * 100) / duration;
    }

    public void update(int pos, int dur) {
        position = pos;
        if (dur != -1) duration = dur;

        if (position * 100 > duration * 90) played = new Date();    // If 90% of duration, consider it played
    }

    public String getFileName() {
        Uri uri = Uri.parse(getDownloadUrl());
        return uri.getLastPathSegment().replace("/", "-");
    }

    public void download() {
        String url = getDownloadUrl();
        String fileName = getFileName();

        if (getDownloadedFile().exists()) {
            // File somehow managed to be downloaded without us knowing about it...
            Cast.this.setDownloaded(new Date());
            Cast.this.save();
        }
        else {
            //Toast.makeText(getContext(), "Downloading..." + getTitle(), Toast.LENGTH_SHORT).show();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(getTitle());
            request.setDescription(getDescription());
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            request.setDestinationInExternalFilesDir(getContext(), Environment.DIRECTORY_PODCASTS, fileName);

            DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            final Long downloadID = manager.enqueue(request);

            getContext().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Long dwnId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);

                    if (downloadID.equals(dwnId)) {
                        Cast.this.setDownloaded(new Date());
                        Cast.this.save();
                    }
                }
            }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }

    public File getDownloadedFile() {
        return new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PODCASTS), getFileName());
    }

    public boolean isDownloaded() {
        return getDownloadedFile().exists();
    }

    private Feed getFeed() {
        if (mFeed == null) {
            mFeed = Feed.findById(Feed.class, getFeedId());
        }

        return mFeed;
    }

    public File getImageFilePath() {
        return getFeed().getImageFilePath();
    }

    @Override
    public void save() {
        super.save();

        ModelLifecycleDispatcher.dispatchOnSaved(this);
    }

    public void delete() {
        if (getDownloadedFile().exists()) getDownloadedFile().delete();

        super.delete();

        ModelLifecycleDispatcher.dispatchOnDeleted(this);
    }

    public void markPlayed() {
        played = new Date();
    }

    public void markUnplayed() {
        played = null;
        position = 0;
    }

}
