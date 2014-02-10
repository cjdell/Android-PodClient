package com.cjdell.podclient;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.cjdell.podclient.models.Feed;

/**
 * Created by cjdell on 04/02/14.
 */
public class FeedSettingsFragment extends DialogFragment {

    private static final String ARG_FEED_ID = "feedID";

    private Feed mFeed;

    private CheckBox chkAutoSyncEnabled;
    private CheckBox chkAutoDownloadEnabled;
    private EditText txtMaximumItems;

    public static FeedSettingsFragment newInstance(long feedID) {
        FeedSettingsFragment fragment = new FeedSettingsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_FEED_ID, feedID);
        fragment.setArguments(args);
        return fragment;
    }

    public FeedSettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mFeed = PodClientApp.current.getFeed(getArguments().getLong(ARG_FEED_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed_settings, container, false);

        chkAutoSyncEnabled      = (CheckBox) view.findViewById(R.id.chkSyncFeedEnabled);
        chkAutoDownloadEnabled  = (CheckBox) view.findViewById(R.id.chkAutoDownloadEnabled);
        txtMaximumItems         = (EditText) view.findViewById(R.id.txtMaximumItems);

        readFeed();

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setTitle("Feed Settings - " + mFeed.getTitle());

        return dialog;
    }

    private void readFeed() {
        chkAutoSyncEnabled.setChecked(mFeed.getAutoSyncEnabled());
        chkAutoDownloadEnabled.setChecked(mFeed.getAutoDownloadEnabled());
        txtMaximumItems.setText(new Integer(mFeed.getMaximumItems()).toString());
    }

    private void updateFeed() {
        mFeed.setAutoSyncEnabled(chkAutoSyncEnabled.isChecked());
        mFeed.setAutoDownloadEnabled(chkAutoDownloadEnabled.isChecked());
        mFeed.setMaximumItems(Integer.parseInt(txtMaximumItems.getText().toString()));
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        updateFeed();

        mFeed.save();
    }

}
