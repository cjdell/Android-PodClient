package com.cjdell.podclient;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by cjdell on 27/01/14.
 */
public class AboutFragment extends DialogFragment {

    private Button btnDonate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        btnDonate = (Button) view.findViewById(R.id.btnDonate);

        return view;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setTitle("About");

        return dialog;
    }

}
