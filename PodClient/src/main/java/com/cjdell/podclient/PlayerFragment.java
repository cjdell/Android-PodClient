package com.cjdell.podclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.atermenji.android.iconicdroid.icon.IconicIcon;

import java.util.HashMap;

import com.cjdell.podclient.helpers.TextHelper;
import com.cjdell.podclient.models.playlist.PlaylistItemCollection;

/**
 * Created by cjdell on 30/01/14.
 */
public class PlayerFragment extends Fragment implements OnPlayerStatusChangeListener, PodClientApp.OnPlaylistChangeListener {

    private static final String TAG = "PlayerFragment";

    private TextView        txtPlaylistTitle;
    private Button          btnPlay;

    private Button          btnSort;
    private Button          btnAdd;
    private Button          btnDownload;

    private Player          mPlayer;

    public static PlaylistFragment newInstance() {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public PlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        mPlayer = PodClientApp.current.getPlayer();

        txtPlaylistTitle    = (TextView)    view.findViewById(R.id.txtPlaylistTitle);
        btnPlay             = (Button)      view.findViewById(R.id.btnPlay);

        btnSort             = (Button)      view.findViewById(R.id.btnSort);
        btnAdd              = (Button)      view.findViewById(R.id.btnAdd);
        btnDownload         = (Button)      view.findViewById(R.id.btnDownload);

        btnDownload.setBackground(TextHelper.getIcon(getActivity().getApplicationContext(), IconicIcon.DOWNLOAD, Color.argb(191, 255, 255, 255)));

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                if (mPlayer.isPlaying())
                    mPlayer.pause();
                else
                    mPlayer.play();
            }
        });

        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSortDialog();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFeedListDialog();
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadAll();
            }
        });

        mPlayer.addOnPlayerStatusChangeListener(this);

        PodClientApp.current.addPlaylistChangeListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        refreshStatus();
    }

    private void openSortDialog() {
        /** Getting the fragment manager */
        FragmentManager manager = getActivity().getSupportFragmentManager();

        HashMap<Long, String> sortOptions = mPlayer.getItemCollection().getSortOptions();

        /** Instantiating the DialogFragment class */
        AlertRadioDialog alert = new AlertRadioDialog("Sort by...", sortOptions, new AlertRadioDialog.AlertPositiveListener() {
            @Override
            public void onPositiveClick(String option, Long key) {
                mPlayer.getItemCollection().setSort(key);
            }
        });

        /** Creating a bundle object to store the selected item's index */
        Bundle b  = new Bundle();

        /** Storing the selected item's index in the bundle object */
        b.putInt("position", (int) (long) mPlayer.getItemCollection().getSort());

        /** Setting the bundle object to the dialog fragment object */
        alert.setArguments(b);

        /** Creating the dialog fragment object, which will in turn open the alert dialog window */
        alert.show(manager, "alert_radio_dialog");
    }

    private void openFeedListDialog() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

        FeedListFragment feedListFragment = FeedListFragment.newInstance();
        feedListFragment.selectPlaylist(PodClientApp.current.getActivePlaylist());
        feedListFragment.show(ft, "feed_list_dialog");
    }

    private void downloadAll() {
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Download all?")
                .setMessage("Are you sure you wish to download all items on this playlist?")
                .setPositiveButton("Download all", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPlayer.getItemCollection().downloadAll();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void onDestroyView() {
        mPlayer.removePlayerStatusChangeListener(this);

        PodClientApp.current.removePlaylistChangeListener(this);

        super.onDestroyView();
    }

    @Override
    public void onPlaylistChange(PlaylistItemCollection playlistItemCollection) {

    }

    @Override
    public void onStatusChange() {
        refreshStatus();
    }

    private void refreshStatus() {
        txtPlaylistTitle.setText(mPlayer.getItemCollection().getPlaylistTitle());

        if (mPlayer.isPlaying())
            //btnPlay.setText("Pause");
            btnPlay.setBackground(Resources.getSystem().getDrawable(android.R.drawable.ic_media_pause));
        else
            //btnPlay.setText("Play");
            btnPlay.setBackground(Resources.getSystem().getDrawable(android.R.drawable.ic_media_play));

        if (mPlayer.getItemCollection().supportsAddItem())
            btnAdd.setVisibility(View.VISIBLE);
        else
            btnAdd.setVisibility(View.GONE);
    }

}
