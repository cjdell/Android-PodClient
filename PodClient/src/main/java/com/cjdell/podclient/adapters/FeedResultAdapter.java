package com.cjdell.podclient.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import com.cjdell.podclient.R;
import com.cjdell.podclient.services.FeedDirectory;

/**
 * Created by cjdell on 16/01/14.
 */
public class FeedResultAdapter extends ArrayAdapter<FeedDirectory.FeedResult> {

    private List<FeedDirectory.FeedResult>  mFeedResults;
    private Activity                        mActivity;

    public FeedResultAdapter(Activity activity, List<FeedDirectory.FeedResult> feedResults) {
        super(activity, R.layout.feed_search_result, feedResults);

        mFeedResults    = feedResults;
        mActivity       = activity;
    }

//    public FeedResultAdapter(Activity activity) {
//        mActivity       = activity;
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        FeedDirectory.FeedResult cast = (FeedDirectory.FeedResult) getItem(position); //mFeedResults.get(position);

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.feed_search_result, null, false);

            viewHolder = new ViewHolder(convertView);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.refresh(position, cast);

        return convertView;
    }

    private class ViewHolder {
        int         position;

        View        root;
        TextView    name;

        public ViewHolder(View view) {
            root = view;

            name = (TextView) root.findViewById(R.id.name);
        }

        public void refresh(int pos, final FeedDirectory.FeedResult feedResult) {
            position = pos;

            name.setText(feedResult.name);
        }

    }
}
