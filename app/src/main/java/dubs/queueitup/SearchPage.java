package dubs.queueitup;

/**
 * Created by ryanschott on 2017-09-28.
 */
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;

public class SearchPage extends Fragment implements Search.View{

    private static final String KEY_CURRENT_QUERY = "CURRENT_QUERY";
    static final String EXTRA_TOKEN = "EXTRA_TOKEN";

    private SpotifyApi api;
    private SpotifyService spotify;
    private Search.ActionListener mActionListener;

    private ScrollListener mScrollListener;
    private SearchResultsAdapter mAdapter;

    private class ScrollListener extends ResultListScrollListener {

        public ScrollListener(LinearLayoutManager layoutManager) {
            super(layoutManager);
        }

        @Override
        public void onLoadMore() {
            mActionListener.loadMoreResults();
        }
    }

    public SearchPage() {
        // Required empty public constructor
    }

    OnTrackItemSelected sListener;

    public interface OnTrackItemSelected {
        void addTrack(Track item);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.search_page, container, false);
//        EditText text = (EditText) v.findViewById(R.id.search_input);
//
//        text.addTextChangedListener(this);
//
        mActionListener = new SearchPresenter(getActivity(), this);
        mActionListener.init(RequestSingleton.getSpotify_auth_token());

        // Setup search field
        final SearchView searchView = v.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mActionListener.search(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        // Setup search results list
        mAdapter = new SearchResultsAdapter(getActivity(), new SearchResultsAdapter.ItemSelectedListener() {
            @Override
            public void onItemSelected(View itemView, Track item) {
                sListener.addTrack(item);
                mActionListener.selectTrack(item);
            }
        });

        RecyclerView resultsList = (RecyclerView) v.findViewById(R.id.search_results);
        resultsList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        resultsList.setLayoutManager(mLayoutManager);
        resultsList.setAdapter(mAdapter);
        mScrollListener = new ScrollListener(mLayoutManager);
        resultsList.addOnScrollListener(mScrollListener);

        // If Activity was recreated wit active search restore it
        if (savedInstanceState != null) {
            String currentQuery = savedInstanceState.getString(KEY_CURRENT_QUERY);
            mActionListener.search(currentQuery);
        }
        return v;
    }


//    @Override
//    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//    }

//    @Override
//    public void onTextChanged(CharSequence s, int start, int before, int count) {
//        if (sListener != null) {
//            sListener.searchSpotify(s.toString());
//        }
//    }

//    @Override
//    public void afterTextChanged(Editable s) {
//
//    }

    @Override
    public void reset() {
        mScrollListener.reset();
        mAdapter.clearData();
    }

    @Override
    public void addData(List<Track> items) {
        mAdapter.addData(items);
    }

    @Override
    public void onPause() {
        super.onPause();
        mActionListener.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mActionListener.resume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActionListener.getCurrentQuery() != null) {
            outState.putString(KEY_CURRENT_QUERY, mActionListener.getCurrentQuery());
        }
    }

    @Override
    public void onDestroy() {
        mActionListener.destroy();
        super.onDestroy();
    }


    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if (context instanceof OnTrackItemSelected) {
            sListener = (OnTrackItemSelected) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement searchTextEntered");
        }
    }
}