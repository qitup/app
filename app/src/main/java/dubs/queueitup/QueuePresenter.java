package dubs.queueitup;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.ArrayList;
import java.util.List;

import dubs.queueitup.Models.TrackItem;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by ryanschott on 2017-10-23.
 */

public class QueuePresenter {
    private static final String TAG = SearchPresenter.class.getSimpleName();
    public static final int PAGE_SIZE = 20;
    private static final String CLIENT_ID = "SPOTIFY_ID";

    private final Context mContext;
    private final QueuePage mView;
    private String mCurrentQuery;
    private SpotifyApi spotifyApi = null;
    private SpotifyService mSpotifyApi = null;

    private SearchPager mSearchPager;
    private SearchPager.CompleteListener mSearchListener;

    private SpotifyPlayer mPlayer;

    public QueuePresenter(Context context, QueuePage view) {
        mContext = context;
        mView = view;
    }

    public void init(String accessToken) {
        spotifyApi = new SpotifyApi();

        if (accessToken != null) {
            spotifyApi.setAccessToken(accessToken);
        } else {
            logError("No valid access token");
        }
    }

    public void addQueueItem(final List<TrackItem>items) {
        if(spotifyApi == null){
            if(RequestSingleton.getSpotify_auth_token() == null){
                logError("HOLY SHIT ITS EMPTY");
            } else {
                init(RequestSingleton.getSpotify_auth_token());
                mSpotifyApi = spotifyApi.getService();

                for (int i = 0; i < items.size(); i++){
                    LoadTrackTask task = new LoadTrackTask();
                    task.execute(items.get(i));
                }
            }

        } else {
            mSpotifyApi = spotifyApi.getService();

            for (int i = 0; i < items.size(); i++){
                LoadTrackTask task = new LoadTrackTask();
                task.execute(items.get(i));
            }
        }
    }

    public TrackItem removeQueueItem(final int position){
        TrackItem item = mView.getItem(position);
        mView.removeItem(position);
        return item;

    }

    public void addPlaying(TrackItem item){
        if(spotifyApi == null){
            init(RequestSingleton.getSpotify_auth_token());
        }
        mSpotifyApi = spotifyApi.getService();

        LoadPTrackTask task = new LoadPTrackTask();
        task.execute(item);
    }


    private void logError(String msg) {
        Toast.makeText(mContext, "Error: " + msg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, msg);
    }

    private void logMessage(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        Log.d(TAG, msg);
    }

    private class LoadTrackTask extends AsyncTask<TrackItem, Integer, Track> {

        private TrackItem item;

        @Override
        protected Track doInBackground(TrackItem... items) {
            item = items[0];
            return  mSpotifyApi.getTrack(items[0].getUri());
        }

        @Override
        protected void onPostExecute(Track result) {
            List<TrackItem> tracksToAdd = new ArrayList<>();

            item.loaded(result);

            tracksToAdd.add(item);
            mView.addData(tracksToAdd);
        }
    }

    private class LoadPTrackTask extends AsyncTask<TrackItem, Integer, Track> {
        private TrackItem item;

        @Override
        protected Track doInBackground(TrackItem... items) {
            return mSpotifyApi.getTrack(items[0].getUri());
        }

        @Override
        protected void onPostExecute(Track result) {
            List<TrackItem> tracksToAdd = new ArrayList<>();

            item.loaded(result);

            tracksToAdd.add(item);
            mView.addPlaying(tracksToAdd);
        }
    }
}