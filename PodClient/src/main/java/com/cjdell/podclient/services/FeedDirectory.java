package com.cjdell.podclient.services;

import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.cjdell.podclient.TaskDelegate;

/**
 * Created by cjdell on 15/01/14.
 */
public class FeedDirectory {

    private final String mApplicationID;

    public FeedDirectory(String appID) {
        mApplicationID = appID;
    }

    public void search(SearchRequest req, TaskDelegate delegate) {
        SyncTask syncTask = new SyncTask(delegate);
        syncTask.execute(req);
    }

    public static class SearchRequest {
        public String keywords;
        public Integer start = 0;
        public Integer results = 10;
    }

    public static class FeedResult {
        public String name;
        public String url;
    }

    private class SyncTask extends AsyncTask<SearchRequest, Integer, List<FeedResult>> {

        private Exception       mException;
        private TaskDelegate    mDelegate;

        public SyncTask(TaskDelegate delegate) {
            mDelegate = delegate;
        }

        protected List<FeedResult> doInBackground(SearchRequest... searchRequests) {
            try {
                SearchRequest req = searchRequests[0];

                URL url = new URL("http://api.digitalpodcast.com/v2r/search/?appid=" + mApplicationID + "&keywords=" + URLEncoder.encode(req.keywords, "ISO-8859-1") + "&format=opml&sort=rating&searchsource=title&contentfilter=noadult&start=" + req.start + "&results=" + req.results);

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);

                XmlPullParser xpp = factory.newPullParser();

                // We will get the XML from an input stream
                xpp.setInput(url.openConnection().getInputStream(), "UTF_8");

                List<FeedResult> feedResults = new ArrayList<FeedResult>();
                FeedResult feedResult = null;

                // Returns the type of current event: START_TAG, END_TAG, etc..
                int eventType = xpp.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equalsIgnoreCase("outline")) {
                            feedResult = new FeedResult();

                            feedResult.name = xpp.getAttributeValue(null, "text");
                            feedResult.url = xpp.getAttributeValue(null, "url");
                        }
                    }
                    else if (eventType == XmlPullParser.END_TAG) {
                        if (xpp.getName().equalsIgnoreCase("outline")) {
                            feedResults.add(feedResult);
                            feedResult = null;
                        }
                    }

                    eventType = xpp.next(); //move to next element
                }

//                this.publishProgress(1);

                return feedResults;
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
                mException = e;
            }
            catch (XmlPullParserException e) {
                e.printStackTrace();
                mException = e;
            }
            catch (IOException e) {
                e.printStackTrace();
                mException = e;
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (mDelegate != null) mDelegate.taskCompletionResult(this, null);
        }

        protected void onPostExecute(List<FeedResult> feedResults) {
            if (mDelegate != null) mDelegate.taskCompletionResult(this, feedResults);
        }

    }

}
