package com.cjdell.podclient.adapters;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjdell.podclient.R;
import com.cjdell.podclient.models.Feed;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by cjdell on 01/01/14.
 */
public class FeedAdapter extends ArrayAdapter<Feed> {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEEE dd MMMM yyyy");

    private List<Feed>  mFeeds;
    private Activity    mActivity;

    private OnFeedMenuButtonClick mOnFeedMenuButtonClick;

    public FeedAdapter(Activity activity, List<Feed> feeds) {
        super(activity, R.layout.feed_list_item, feeds);

        mFeeds = feeds;
        mActivity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        Feed feed = mFeeds.get(position);

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.feed_list_item, null, false);

            viewHolder = new ViewHolder(convertView);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.refresh(position, feed);

        return convertView;
    }

    public OnFeedMenuButtonClick getOnMenuClickListener() {
        return mOnFeedMenuButtonClick;
    }

    public void setOnMenuClickListener(OnFeedMenuButtonClick onFeedMenuButtonClick) {
        mOnFeedMenuButtonClick = onFeedMenuButtonClick;
    }

    public interface OnFeedMenuButtonClick {
        public void onFeedMenuButtonClick(Feed feed, View v);
    }

    public class ViewHolder {
        int         position;

        View        root;

        TextView    title;
        TextView    sync_date;
        TextView    description;
        TextView    count;
        ImageView   list_image;

        ImageView   menu_button;

        public ViewHolder(View view) {
            root = view;

            title           = (TextView)    root.findViewById(R.id.title);
            sync_date       = (TextView)    root.findViewById(R.id.sync_date);
            description     = (TextView)    root.findViewById(R.id.description);
            count           = (TextView)    root.findViewById(R.id.count);
            list_image      = (ImageView)   root.findViewById(R.id.list_image);

            menu_button     = (ImageView)   root.findViewById(R.id.menu_button);
        }

        public void refresh(int pos, final Feed feed) {
            position = pos;

            String syncDateString = "";

            if (feed.getLatestItemPubDate() != null) syncDateString = DATE_FORMAT.format(feed.getLatestItemPubDate());

            title       .setText(feed.getTitle());
            sync_date   .setText(syncDateString);
            description .setText(feed.getDescription());
            count       .setText(new Integer(feed.getCastCount()).toString());
            list_image  .setImageURI(Uri.fromFile(feed.getImageFilePath()));

            menu_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnFeedMenuButtonClick.onFeedMenuButtonClick(feed, menu_button);
                }
            });
        }
    }

}


