package com.cjdell.podclient;

import android.app.Activity;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.cjdell.podclient.adapters.FeedAdapter;
import com.cjdell.podclient.models.Feed;
import com.cjdell.podclient.models.Playlist;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FeedListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FeedListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class FeedListFragment extends DialogFragment implements
        PageFragmentInterface,
        FeedSearchFragment.OnFragmentInteractionListener,
        PodClientApp.OnFeedListChangeListener, PopupMenu.OnMenuItemClickListener {

    private static final String TAG = "FeedListFragment";

    // Controls
    private ListView    lstFeeds;

    private List<Feed>          mFeeds;
    private FeedAdapter         mAdapter;

    private Playlist            mPlaylist;
    private Feed                mContextFeed;

    private FeedSearchFragment  mFeedSearchFragment;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FeedListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedListFragment newInstance() {
        FeedListFragment fragment = new FeedListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public FeedListFragment() {
        // Required empty public constructor
        Log.i(TAG, "Instantiated");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");

        if (getArguments() != null) {

        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed_list, container, false);

        lstFeeds = (ListView) view.findViewById(R.id.lstFeeds);

        try {
            mFeeds = PodClientApp.current.getFeeds();
        }
        catch (Exception ex) {
            Toast.makeText(getActivity().getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            mFeeds = new ArrayList<Feed>();
        }

        mAdapter = new FeedAdapter(getActivity(), mFeeds);

        mAdapter.setOnMenuClickListener(new FeedAdapter.OnFeedMenuButtonClick() {
            @Override
            public void onFeedMenuButtonClick(Feed feed, View v) {
                showItemPopupMenu(feed, v);
            }
        });

        lstFeeds.setAdapter(mAdapter);

        lstFeeds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                Feed feed = (Feed) lstFeeds.getItemAtPosition(position);

                if (feed.getId() != null) {
                    if (getDialog() == null) {
                        playFeed(feed);
                    }
                    else {
                        openFeedDialog(feed);
                    }
                }
            }
        });

        PodClientApp.current.addFeedListChangeListener(this);

        if (mFeeds.size() == 0)
            Toast.makeText(getActivity().getApplicationContext(), "Looks like you have no feeds. Why not use the top right (+) button to search for your favourites feeds?", Toast.LENGTH_LONG).show();

//        TextView emptyText = new TextView(getActivity().getApplicationContext());
//        emptyText.setText("Looks like you have no feeds. Why not use the top right (+) button to search for your favourites feeds?");
        lstFeeds.setEmptyView((TextView) view.findViewById(R.id.emptyResults));

        return view;
    }

    public void onDestroyView() {
        super.onDestroyView();

        Log.i(TAG, "onDestroyView");

        PodClientApp.current.removeFeedListChangeListener(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG, "onCreateDialog");

        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.getWindow().setTitle("Select feed...");

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.i(TAG, "onStart");

        if (getDialog() != null) {
            int width = (getActivity().getResources().getDisplayMetrics().widthPixels * 95) / 100;
            int height = (getActivity().getResources().getDisplayMetrics().heightPixels * 95) / 100;

            getDialog().getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, android.view.MenuInflater inflater) {
        if (getDialog() == null) {
            // Don't add options when we're a dialog (we can't access them anyway)
            inflater.inflate(R.menu.feed_list, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_feed:
                newFeed();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void shown() {

    }

    private void showItemPopupMenu(Feed feed, View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        Menu menu = popup.getMenu();

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.feed_item_menu, menu);

        popup.setOnMenuItemClickListener(this);
        mContextFeed = feed;

        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_feeds_settings:
                openFeedSettingsDialog(mContextFeed);
                return true;

            case R.id.action_update_feed:
                syncFeed(mContextFeed);
                return true;

            case R.id.action_delete_feed:
                deleteFeed(mContextFeed);
                return true;
        }

        return false;
    }

    private void syncFeed(Feed feed) {
        TaskDelegate taskDelegate = new TaskDelegate() {
            @Override
            public void taskCompletionResult(Object sender, Object result) {
                mAdapter.notifyDataSetChanged();
            }
        };

        feed.sync(taskDelegate);

        mAdapter.notifyDataSetChanged();
    }

    private void deleteFeed(Feed feed) {
        PodClientApp.current.removeFeed(feed);
    }

    public void selectPlaylist(Playlist playlist) {
        mPlaylist = playlist;
    }

    private void playFeed(Feed feed) {
        Playlist playlist = PodClientApp.current.playFeed(feed);

        onPlaylistSelected(playlist.getId());
    }

    private void openFeedSettingsDialog(Feed feed) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

        FeedSettingsFragment feedSettingsFragment = FeedSettingsFragment.newInstance(feed.getId());
        feedSettingsFragment.show(ft, "feed_settings_dialog");
    }

    private void openFeedDialog(Feed feed) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

        FeedFragment feedFragment = FeedFragment.newInstance();
        feedFragment.selectPlaylist(feed, mPlaylist);
        feedFragment.show(ft, "feed_dialog");
    }

    private void newFeed() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

        mFeedSearchFragment = FeedSearchFragment.newInstance();
        mFeedSearchFragment.setListener(this);
        mFeedSearchFragment.show(ft, "dialog");
    }

    private void onPlaylistSelected(Long playlistID) {
        if (mListener != null) {
            mListener.onFragmentInteraction(this, playlistID);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnFragmentInteractionListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    @Override
    public void onFragmentInteraction(Fragment fragment, Object args) {
        if (fragment instanceof FeedSearchFragment) {
            Uri uri = (Uri) args;

            mFeedSearchFragment.getDialog().dismiss();

            TaskDelegate taskDelegate = new TaskDelegate() {
                @Override
                public void taskCompletionResult(Object sender, Object result) {
                    mAdapter.notifyDataSetChanged();    // For when the feed has finished syncing...
                }
            };

            Feed feed = Feed.addFromUrl(getActivity().getApplicationContext(), uri.toString(), taskDelegate);
            PodClientApp.current.addFeed(feed);

            int index = mFeeds.indexOf(feed);
            lstFeeds.setSelection(index);
        }
    }

    @Override
    public void onFeedListChange() {
        mAdapter.notifyDataSetChanged();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Fragment fragment, Object args);
    }

}
