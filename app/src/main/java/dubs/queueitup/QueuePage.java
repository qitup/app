package dubs.queueitup;

/**
 * Created by ryanschott on 2017-09-28.
 */
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class QueuePage extends Fragment implements Search.View {

    QueueAdapter mAdapter;
    private QueuePresenter mPresenter;
    OnQueueItemSelected sListener;


    public QueuePage() {
// Required empty public constructor
    }

    public interface OnQueueItemSelected {
        void playTrack(Track item);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
// Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.queue_page, container, false);

        mPresenter = new QueuePresenter(getActivity(), this);
        mPresenter.init(RequestSingleton.getSpotify_auth_token());

        RecyclerView resultsList = (RecyclerView) v.findViewById(R.id.queue_list);
        resultsList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        resultsList.setLayoutManager(mLayoutManager);

        mAdapter = new QueueAdapter(getActivity(), new QueueAdapter.ItemVotedListener(){
            @Override
            public void onItemVoted(View itemView, Track item) {
                Log.d("QueuePage", "Vote submitted");
                sListener.playTrack(item);
            }
        });

        resultsList.setAdapter(mAdapter);


        return v;

    }

    public QueuePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void reset() {
        mAdapter.clearData();
    }

    @Override
    public void addData(List<Track> items) {
        mAdapter.addData(items);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if (context instanceof OnQueueItemSelected) {
            sListener = (OnQueueItemSelected) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement searchTextEntered");
        }
    }
}

