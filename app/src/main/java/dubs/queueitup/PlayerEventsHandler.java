package dubs.queueitup;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dubs.queueitup.Emitter;

class PlayerEventsHandler extends Emitter
        implements SpotifyPlayer.NotificationCallback {
    private static final String TAG = "PlayerEventsHandler";
//    private static Context ctx;

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        // Strip off enum prefix for platform consistency
        String eventName;
        eventName = playerEvent.toString().substring(17);

        Player player = PlayerSingleton.getInstance(this.ctx).getPlayer();
        Gson gson = new Gson();
        JSONObject metaData = null;
        try {
            metaData = new JSONObject(gson.toJson(player.getMetadata()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ;

        switch (playerEvent) {
            case kSpPlaybackNotifyMetadataChanged:
            case kSpPlaybackNotifyTrackChanged:
            case kSpPlaybackNotifyPause:
            case kSpPlaybackNotifyPlay:
            case kSpPlaybackNotifyLostPermission:
            case kSpPlaybackNotifyNext:
                this.emit(eventName, "player.spotify", metaData);
                break;
            default:
                // Strip off kSpPlaybackNotify
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        String errorName;
        switch (error) {
            case kSpErrorOk:
                return;
            case UNKNOWN:
                errorName = "Unknown";
                break;
            case kSpAlreadyPrefetching:
            case kSpPrefetchDownloadFailed:
            case kSpStorageReadError:
            case kSpStorageWriteError:
                // Strip off kSp
                errorName = error.toString().substring(3);
                break;
            default:
                // Strip off kSpError
                errorName = error.toString().substring(8);
                break;
        }

        this.emit("playbackerror", errorName, new JSONObject());
    }

    public void setContext(Context context){
        ctx = context;
    }
}