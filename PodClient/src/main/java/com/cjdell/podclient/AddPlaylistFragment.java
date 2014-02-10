package com.cjdell.podclient;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.cjdell.podclient.models.Playlist;

/**
 * Created by cjdell on 24/01/14.
 */
public class AddPlaylistFragment extends DialogFragment {

    private TextView    name;
    private Button      btnAdd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_add_playlist, container, false);

        name    = (TextView)    view.findViewById(R.id.name);
        btnAdd  = (Button)      view.findViewById(R.id.btnAdd);

        name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    addPlaylist();

                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(name.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    return true;
                }

                return false;
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlaylist();
            }
        });

        // Automatically show the keyboard
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Create custom playlist...");
        return dialog;
    }

    private void addPlaylist() {
        Playlist playlist = new Playlist(getActivity().getApplicationContext(), Playlist.MANUAL_PLAYLIST);
        playlist.setTitle(name.getText().toString());
        playlist.save();

        PodClientApp.current.addPlaylist(playlist);

        dismiss();
    }
}