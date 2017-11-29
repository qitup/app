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

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by ryanschott on 2017-10-23.
 */

public class QueuePresenter implements Search.ActionListener {
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

    @Override
    public void init(String accessToken) {
        spotifyApi = new SpotifyApi();

        if (accessToken != null) {
            spotifyApi.setAccessToken(accessToken);
        } else {
            logError("No valid access token");
        }
    }

    public void addQueueItem(final String id) {
        if(spotifyApi == null){
            if(RequestSingleton.getSpotify_auth_token() == null){
                logError("HOLY SHIT ITS EMPTY");
            } else {
                init(RequestSingleton.getSpotify_auth_token());
                mSpotifyApi = spotifyApi.getService();

                LoadTrackTask task = new LoadTrackTask();
                task.execute(id);
            }

        } else {
            mSpotifyApi = spotifyApi.getService();

            LoadTrackTask task = new LoadTrackTask();
            task.execute(id);
        }
    }

    public Track removeQueueItem(final int position){
        Track item = mView.getItem(position);
        mView.removeItem(position);
        return item;

    }

    public void addPlaying(final String id){
        if(spotifyApi == null){
            init(RequestSingleton.getSpotify_auth_token());
        }
        mSpotifyApi = spotifyApi.getService();

        LoadPTrackTask task = new LoadPTrackTask();
        task.execute(id);
    }


    @Override
    public void search(@Nullable String searchQuery) {
        if (searchQuery != null && !searchQuery.isEmpty() && !searchQuery.equals(mCurrentQuery)) {
//            logMessage("query text submit " + searchQuery);
            mCurrentQuery = searchQuery;
            mView.reset();
            mSearchListener = new SearchPager.CompleteListener() {
                @Override
                public void onComplete(List<Track> items) {
                    mView.addData(items);
                }

                @Override
                public void onError(Throwable error) {
                    logError(error.getMessage());
                }
            };
            mSearchPager.getFirstPage(searchQuery, PAGE_SIZE, mSearchListener);
        }
    }


    @Override
    public void destroy() {
    }

    @Override
    @Nullable
    public String getCurrentQuery() {
        return mCurrentQuery;
    }

    @Override
    public void resume() {
//        mContext.stopService(PlayerService.getIntent(mContext));
    }

    @Override
    public void pause() {
//        mContext.startService(PlayerService.getIntent(mContext));
    }

    @Override
    public void loadMoreResults() {
        Log.d(TAG, "Load more...");
//        mSearchPager.getNextPage(mSearchListener);
    }

    @Override
    public void selectTrack(Track item) {
        String previewUrl = item.preview_url;

        if (previewUrl == null) {
            logMessage("Track doesn't have a preview");
            return;
        }
    }

    private void logError(String msg) {
        Toast.makeText(mContext, "Error: " + msg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, msg);
    }

    private void logMessage(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        Log.d(TAG, msg);
    }

    private class LoadTrackTask extends AsyncTask<String, Integer, Track> {
        @Override
        protected Track doInBackground(String... strings) {
            return mSpotifyApi.getTrack(strings[0]);
        }

        @Override
        protected void onPostExecute(Track result) {
            List<Track> tracksToAdd = new ArrayList<>();

            tracksToAdd.add(result);
            mView.addData(tracksToAdd);
        }
    }

    private class LoadPTrackTask extends AsyncTask<String, Integer, Track> {
        @Override
        protected Track doInBackground(String... strings) {
            return mSpotifyApi.getTrack(strings[0]);
        }

        @Override
        protected void onPostExecute(Track result) {
            List<Track> tracksToAdd = new ArrayList<>();

            tracksToAdd.add(result);
            mView.addPlaying(tracksToAdd);
        }
    }
}