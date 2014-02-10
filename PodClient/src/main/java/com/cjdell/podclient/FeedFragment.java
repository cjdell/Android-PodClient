package com.cjdell.podclient;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import com.cjdell.podclient.adapters.CastAdapter;
import com.cjdell.podclient.models.Cast;
import com.cjdell.podclient.models.Feed;
import com.cjdell.podclient.models.Playlist;

/**
 * Created by cjdell on 09/01/14.
 */
public class FeedFragment extends DialogFragment {

    // Controls
    private ListView lstCasts;

    private CastAdapter mAdapter;

    private Feed        mFeed;
    private Playlist    mSelectedPlaylist;

    private OnFragmentInteractionListener mListener;

    public static FeedFragment newInstance() {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public FeedFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        lstCasts = (ListView) view.findViewById(R.id.lstCasts);

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
//        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setTitle("Choose items...");

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null) {
            int width = (getActivity().getResources().getDisplayMetrics().widthPixels * 95) / 100;
            int height = (getActivity().getResources().getDisplayMetrics().heightPixels * 95) / 100;

            getDialog().getWindow().setLayout(width, height);
        }

        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            dismiss();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDismiss(android.content.DialogInterface dialog) {
        if (mSelectedPlaylist != null) {
            mSelectedPlaylist.save();   // Save the playlist when we close the dialog
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Fragment fragment, Object args);
    }

    public void selectPlaylist(Feed feed, Playlist playlist) {
        mFeed               = feed;
        mSelectedPlaylist   = playlist;

        refresh();
    }

    private void refresh() {
        if (getActivity() != null && mFeed != null) {
            try {
                List<Cast> casts = mFeed.getCasts();

                mAdapter = new CastAdapter(getActivity(), casts, true, false);
                mAdapter.setPlaylist(mSelectedPlaylist);

                lstCasts.setAdapter(mAdapter);
            }
            catch (Exception ex) {
                Toast.makeText(getActivity().getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
