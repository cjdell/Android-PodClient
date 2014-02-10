package com.cjdell.podclient;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cjdell.podclient.adapters.CastAdapter;
import com.cjdell.podclient.helpers.TextHelper;
import com.cjdell.podclient.models.Cast;
import com.cjdell.podclient.models.playlist.PlaylistItemCollection;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlaylistFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlaylistFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PlaylistFragment extends Fragment implements OnPlayerStatusChangeListener, PodClientApp.OnPlaylistChangeListener, PageFragmentInterface, PopupMenu.OnMenuItemClickListener, PlaylistItemCollection.OnChangeListener {

    private static final String TAG = "PlaylistFragment";

    private DragSortListView    lstCasts;

    private Button              btnPrev;
    private Button              btnBackward;
    private Button              btnPlay;
    private Button              btnForward;
    private Button              btnNext;

    private SeekBar             seekBar;
    private TextView            txtPos;
    private TextView            txtLen;

    final private List<Cast>        mItems = new ArrayList<Cast>();

    private PlaylistItemCollection  mItemCollection = null;
    private Player                  mPlayer;
    private CastAdapter             mAdapter = null;
    private Cast                    mContextCast = null;
    private boolean                 mTracking;

    private OnFragmentInteractionListener mListener;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlaylistFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlaylistFragment newInstance() {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public PlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        mPlayer = PodClientApp.current.getPlayer();

        lstCasts = (DragSortListView) view.findViewById(R.id.lstCasts);

        btnPrev     = (Button)  view.findViewById(R.id.btnPrev);
        btnBackward = (Button)  view.findViewById(R.id.btnBackward);
        btnPlay     = (Button)  view.findViewById(R.id.btnPlay);
        btnForward  = (Button)  view.findViewById(R.id.btnForward);
        btnNext     = (Button)  view.findViewById(R.id.btnNext);

        seekBar     = (SeekBar)     view.findViewById(R.id.seekBar);
        txtPos      = (TextView)    view.findViewById(R.id.txtPos);
        txtLen      = (TextView)    view.findViewById(R.id.txtLen);

        mItemCollection = mPlayer.getItemCollection();

        mAdapter = new CastAdapter(getActivity(), mItems, false, true);

        mAdapter.setOnMenuClickListener(new CastAdapter.OnCastMenuButtonClick() {
            @Override
            public void onCastMenuButtonClick(Cast cast, View v) {
                showItemPopupMenu(cast, v);
            }
        });

        lstCasts.setAdapter(mAdapter);
        lstCasts.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        lstCasts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cast cast = (Cast)lstCasts.getItemAtPosition(position);
                mPlayer.play(cast);
            }
        });

        lstCasts.setDragSortListener(new DragSortListView.DragSortListener() {
            @Override
            public void drag(int from, int to) {

            }

            @Override
            public void drop(int from, int to) {
                mPlayer.getItemCollection().moveItem(from, to);
                refreshList();
            }

            @Override
            public void remove(int which) {
                // Not used
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                if (mPlayer.isPlaying())
                    mPlayer.pause();
                else
                    mPlayer.play();
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                mPlayer.change(-1);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                mPlayer.change(1);
            }
        });

        btnBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                mPlayer.jump(-30);
            }
        });

        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                mPlayer.jump(30);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) txtPos.setText(TextHelper.getTimeString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mTracking = true;   // Stop the process bar flickering
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mTracking = false;
                mPlayer.seek(seekBar.getProgress());
            }
        });

        txtPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lstCasts.setSelection(0);
                Toast.makeText(getActivity().getApplicationContext(), "Beginning of the list", Toast.LENGTH_SHORT).show();
            }
        });

        txtLen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lstCasts.setSelection(mItems.size() - 1);
                Toast.makeText(getActivity().getApplicationContext(), "End of the list", Toast.LENGTH_SHORT).show();
            }
        });

        mItemCollection.addOnChangeListener(this);
        mPlayer.addOnPlayerStatusChangeListener(this);

        PodClientApp.current.addPlaylistChangeListener(this);

        return view;
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart()");

        super.onStart();

        refreshList();
    }

    private void showItemPopupMenu(Cast cast, View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        Menu menu = popup.getMenu();

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.cast_item_menu, menu);

        if (!mPlayer.getItemCollection().supportsAddItem()) menu.removeItem(R.id.action_remove_item);

        if (cast.getPlayed() == null)
            menu.removeItem(R.id.action_mark_item_unplayed);
        else
            menu.removeItem(R.id.action_mark_item_played);

        popup.setOnMenuItemClickListener(PlaylistFragment.this);
        mContextCast = cast;

        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_mark_item_played:
                mContextCast.markPlayed();
                mContextCast.save();
                mAdapter.notifyDataSetChanged();
                return true;

            case R.id.action_mark_item_unplayed:
                mContextCast.markUnplayed();
                mContextCast.save();
                mAdapter.notifyDataSetChanged();
                return true;

            case R.id.action_download_item:
                mContextCast.download();
                return true;

            case R.id.action_remove_item:
                mPlayer.getItemCollection().removeItem(mContextCast);
                return true;

            case R.id.action_delete_item:
                mPlayer.getItemCollection().deleteItem(mContextCast);
                return true;
        }

        return false;
    }

    @Override
    public void shown() {
        Log.i(TAG, "shown()");

        refreshList();

        if (mItems.size() == 0 && mItemCollection != null && mItemCollection.supportsAddItem())
            Toast.makeText(getActivity().getApplicationContext(), "This playlist is empty. Why not use the top right (+) button to add items from your feeds?", Toast.LENGTH_LONG).show();

        if (mPlayer != null) {
            int index = mPlayer.getItemCollection().getCurrentItemIndex();
            lstCasts.setSelection(index);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        Log.i(TAG, "onAttach()");

        super.onAttach(activity);

        try {
            mListener = (OnFragmentInteractionListener) activity;

            mListener.onFragmentInteraction(this, "Attached");
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach()");

        super.onDetach();

        mListener = null;
    }

    public void onDestroyView() {
        Log.i(TAG, "onDestroyView()");

        mItemCollection.removeOnChangeListener(this);
        mPlayer.removePlayerStatusChangeListener(this);

        PodClientApp.current.removePlaylistChangeListener(this);

        super.onDestroyView();
    }

    @Override
    public void onPlaylistChange(PlaylistItemCollection playlistItemCollection) {
        mItemCollection.removeOnChangeListener(this);

        mItemCollection = playlistItemCollection;

        mItemCollection.addOnChangeListener(this);

        refreshList();
    }

    @Override
    public void onStatusChange() {
        refreshStatus();
    }

    private void refreshList() {
        try {
            Log.i("PlaylistFragment", "Refreshing existing adapter");

            mItems.clear();
            mItems.addAll(mItemCollection.getItems());

            lstCasts.setDragEnabled(mPlayer.getItemCollection().isCustomOrderable());

            refreshStatus();
        }
        catch (Exception ex) {
            Toast.makeText(getActivity().getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void refreshStatus() {
        int duration = mPlayer.getDuration();

        seekBar.setMax(duration);

        txtLen.setText(TextHelper.getTimeString(duration));
        txtPos.setText(TextHelper.getTimeString(mPlayer.getCurrentItemPosition()));

        int index = mPlayer.getItemCollection().getCurrentItemIndex();

        if (mAdapter.getSelectedCast() != index) {
            Log.i(TAG, "setSelectedCast(" + index + ")");
            mAdapter.setSelectedCast(index);

            int last = lstCasts.getLastVisiblePosition();
            int first = lstCasts.getFirstVisiblePosition();

            // Only jump to the item if we can't see it. less annoying...
            if (index < first || index > last) {
                lstCasts.requestFocusFromTouch();
                lstCasts.setSelection(index);
            }
        }

        if (mPlayer.isPlaying())
            btnPlay.setBackground(Resources.getSystem().getDrawable(android.R.drawable.ic_media_pause));
        else
            btnPlay.setBackground(Resources.getSystem().getDrawable(android.R.drawable.ic_media_play));

        if (!mRunning) {
            mRunnable.run();
            mRunning = true;
        }

        mAdapter.notifyDataSetChanged();
    }

    private Boolean mRunning = false;

    private Handler mHandler = new Handler();

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            if (!mTracking) {
                int pos = mPlayer.getCurrentItemPosition();

                seekBar.setProgress(pos);
                txtPos.setText(TextHelper.getTimeString(pos));
            }

            mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onItemListChanged(PlaylistItemCollection col) {
        refreshList();
    }

    @Override
    public void onItemAdded(PlaylistItemCollection col, Cast item) {

    }

    @Override
    public void onItemRemoving(PlaylistItemCollection col, Cast item) {

    }

    @Override
    public void onItemRemoved(PlaylistItemCollection col, Cast item) {

    }

    @Override
    public void onItemDeleted(PlaylistItemCollection col, Cast item) {

    }

    @Override
    public void onItemMoved(PlaylistItemCollection col, Cast item) {

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
