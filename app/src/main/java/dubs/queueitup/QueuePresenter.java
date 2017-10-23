package dubs.queueitup;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

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

    private final Context mContext;
    private final Search.View mView;
    private String mCurrentQuery;
    private SpotifyApi spotifyApi;
    private SpotifyService mSpotifyApi = null;

    private SearchPager mSearchPager;
    private SearchPager.CompleteListener mSearchListener;

    private Player mPlayer;

    public QueuePresenter(Context context, Search.View view) {
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

    public void addQueueItem(String id){
        List<Track> tracksToAdd = new ArrayList<>();

        mSpotifyApi = spotifyApi.getService();

        Track item = mSpotifyApi.getTrack(id);
        tracksToAdd.add(item);
        mView.addData(tracksToAdd);
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
    public void destroy() {    }

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

        if (mPlayer == null) return;

        String currentTrackUrl;
        currentTrackUrl = mPlayer.getCurrentTrack();

        if (currentTrackUrl == null || !currentTrackUrl.equals(previewUrl)) {
            mPlayer.play(previewUrl);
        } else if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.resume();
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
}