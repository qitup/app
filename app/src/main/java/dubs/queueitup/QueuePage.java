package dubs.queueitup;

/**
 * Created by ryanschott on 2017-09-28.
 */
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class QueuePage extends Fragment implements Search.View {

    QueueAdapter mAdapter;
    QueueAdapter npAdapter;
    private QueuePresenter mPresenter;
    OnMediaPlayerAction cListener;
    ImageButton mediaButton;

    public QueuePage() {
// Required empty public constructor
    }

    public interface OnMediaPlayerAction {
        void onMediaAction(View v);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
// Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.queue_page, container, false);

        mPresenter = new QueuePresenter(getActivity(), this);
        mPresenter.init(RequestSingleton.getSpotify_auth_token());

        RecyclerView resultsList = v.findViewById(R.id.queue_list);
        RecyclerView playingNow = v.findViewById(R.id.now_playing);
        resultsList.setHasFixedSize(true);
        playingNow.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        resultsList.setLayoutManager(mLayoutManager);
        playingNow.setLayoutManager(new LinearLayoutManager(getActivity()));

        mediaButton = v.findViewById(R.id.media_button);

        mediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PlayerSingleton.getInstance(getActivity()).isPlaying() == 1){
                    mediaButton.setImageResource(R.drawable.play_button);
                } else {
                    mediaButton.setImageResource(R.drawable.pause);
                }
                cListener.onMediaAction(mediaButton);
            }
        });

        mAdapter = new QueueAdapter(getActivity(), new QueueAdapter.ItemVotedListener(){
            @Override
            public void onItemVoted(View itemView, Track item, int position) {
                Log.d("QueuePage", "Vote submitted");
            }
        });

        npAdapter = new QueueAdapter(getActivity(), new QueueAdapter.ItemVotedListener(){
            @Override
            public void onItemVoted(View itemView, Track item, int position) {
                // Do nothing
            }
        });

        resultsList.setAdapter(mAdapter);
        playingNow.setAdapter(npAdapter);

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
    public void addPlaying(List<Track> items) {
        npAdapter.addPlaying(items);
    }

    public void enableMediaButton(boolean enabled){
        mediaButton.setClickable(enabled);
    }

    @Override
    public void addData(List<Track> items) {
        mAdapter.addData(items);
    }

    @Override
    public void removeItem(int position){
        mAdapter.removeItem(position);
    }

    public Track getItem(int position){
        return mAdapter.getItem(position);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        if (context instanceof OnMediaPlayerAction) {
            cListener = (OnMediaPlayerAction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement searchTextEntered");
        }
    }
}

