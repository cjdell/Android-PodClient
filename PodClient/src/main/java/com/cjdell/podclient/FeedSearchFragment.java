package com.cjdell.podclient;

import android.app.Activity;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cjdell.podclient.adapters.FeedResultAdapter;
import com.cjdell.podclient.helpers.EndlessScrollListener;
import com.cjdell.podclient.services.FeedDirectory;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FeedSearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FeedSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class FeedSearchFragment extends DialogFragment implements TaskDelegate {

    private FeedDirectory mFeedDirectory;
    private List<FeedDirectory.FeedResult> mResults;
    private FeedResultAdapter mAdapter;
    private FeedDirectory.SearchRequest mSearchRequest;

    private EditText    txtSearchTerm;
    private Button      btnGo;
    private ListView    lstSearchResults;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FeedSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedSearchFragment newInstance() {
        FeedSearchFragment fragment = new FeedSearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public FeedSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed_search, container, false);

        txtSearchTerm       = (EditText)    view.findViewById(R.id.txtSearchTerm);
        btnGo               = (Button)      view.findViewById(R.id.btnGo);
        lstSearchResults    = (ListView)    view.findViewById(R.id.lstSearchResults);

        mFeedDirectory = new FeedDirectory(getString(R.string.feed_directory_app_id));

        txtSearchTerm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                    return true;
                }

                return false;
            }
        });

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });

        lstSearchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onFeedSelected(Uri.parse(mResults.get(position).url));
            }
        });

        // Automatically show the keyboard
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Find a feed...");
        return dialog;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onFeedSelected(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(this, uri);
        }
    }

    public void setListener(OnFragmentInteractionListener listener) {
        mListener = listener;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        }
//        catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(android.os.Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void search() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(txtSearchTerm.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        String term = txtSearchTerm.getText().toString();

        if (term.length() > 4 && term.substring(0, 4).equals("http")) {
            onFeedSelected(Uri.parse(term));
        }
        else {
            loadData(term);
        }
    }

    private void loadData(String term) {
        mSearchRequest = new FeedDirectory.SearchRequest();

        mSearchRequest.results = 10;
        mSearchRequest.start = 0;
        mSearchRequest.keywords = term;

        // Reset for a fresh search
        mResults = new ArrayList<FeedDirectory.FeedResult>();
        mAdapter = new FeedResultAdapter(getActivity(), mResults);
        lstSearchResults.setAdapter(mAdapter);

        lstSearchResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                loadMoreData();
            }
        });

        mFeedDirectory.search(mSearchRequest, this);
    }

    private void loadMoreData() {
        mSearchRequest.start += mSearchRequest.results;

        mFeedDirectory.search(mSearchRequest, this);
    }

    @Override
    public void taskCompletionResult(Object sender, Object result) {
        if (result == null) return;

        List<FeedDirectory.FeedResult> results = (List<FeedDirectory.FeedResult>) result;

        if (results.size() > 0) {
            mResults.addAll(results);
            mAdapter.notifyDataSetChanged();
        }
        else {
            // Don't keep polling for more results after we reach the end
            lstSearchResults.setOnScrollListener(null);

            if (mResults.size() == 0) {
                Toast.makeText(getActivity().getApplicationContext(), "No results found", Toast.LENGTH_LONG).show();
            }
        }
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
