package com.cjdell.podclient;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.cjdell.podclient.adapters.PlaylistAdapter;
import com.cjdell.podclient.models.Playlist;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlaylistListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlaylistListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class PlaylistListFragment extends Fragment implements
        PageFragmentInterface,
        PodClientApp.OnPlaylistListChangeListener, PopupMenu.OnMenuItemClickListener {

    // Controls
    private GridView lstPlaylists;

    private List<Playlist>  mPlaylists = new ArrayList<Playlist>();
    private PlaylistAdapter mAdapter;
    private Playlist        mContextPlaylist;


    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlaylistListFragment.
     */
    public static PlaylistListFragment newInstance() {
        PlaylistListFragment fragment = new PlaylistListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public PlaylistListFragment() {
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
        View view = inflater.inflate(R.layout.fragment_playlist_list, container, false);

        PodClientApp.current.addPlaylistListChangeListener(this);

        lstPlaylists = (GridView) view.findViewById(R.id.lstPlaylists);

        mAdapter = new PlaylistAdapter(getActivity(), mPlaylists);

        mAdapter.setOnMenuClickListener(new PlaylistAdapter.OnPlaylistMenuButtonClick() {
            @Override
            public void onPlaylistMenuButtonClick(Playlist playlist, View v) {
                showItemPopupMenu(playlist, v);
            }
        });

        lstPlaylists.setAdapter(mAdapter);

        lstPlaylists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Playlist playlist = (Playlist)lstPlaylists.getItemAtPosition(position);

                PodClientApp.current.setActivePlaylist(playlist);

                onPlaylistSelected(playlist.getId());
            }
        });

        refreshList();

        return view;
    }

    public void onDestroyView() {
        PodClientApp.current.removePlaylistListChangeListener(this);

        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, android.view.MenuInflater inflater) {
        inflater.inflate(R.menu.playlist_list, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_playlist:
                newPlaylist();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showItemPopupMenu(Playlist playlist, View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        Menu menu = popup.getMenu();

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.playlist_item_menu, menu);

        popup.setOnMenuItemClickListener(PlaylistListFragment.this);
        mContextPlaylist = playlist;

        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_playlist:
                deletePlaylist(mContextPlaylist);
                return true;
        }

        return false;
    }

    @Override
    public void shown() {
//        mAdapter.notifyDataSetChanged();
    }

    private void deletePlaylist(Playlist playlist) {
        if (!playlist.getItemCollection().supportsAddItem()) {
            Toast.makeText(getActivity().getApplicationContext(), "You cannot delete the (All Items) playlist", Toast.LENGTH_LONG).show();
            return;
        }

        PodClientApp.current.removePlaylist(playlist);
    }

    private void newPlaylist() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

        DialogFragment dialogFragment = new AddPlaylistFragment();
        dialogFragment.show(ft, "dialog");
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

    private void refreshList() {
        mPlaylists.clear();
        mPlaylists.addAll( PodClientApp.current.getViewablePlaylists());

        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlaylistListChange() {
        refreshList();
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
