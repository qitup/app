package dubs.queueitup;

import android.content.Context;

import com.spotify.sdk.android.player.Player;

/**
 * Created by ryanschott on 2017-11-06.
 */

class PlayerSingleton {
    private static PlayerSingleton mInstance = null;
    private static Context mCtx;
    private int playing = -1;
    private int duration = -1;
    private Player mPlayer;

    static PlayerSingleton getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new PlayerSingleton(context);
        }
        return mInstance;
    }

    private PlayerSingleton(Context context) {
        mCtx = context;
    }

    public synchronized Player getPlayer(){
        return this.mPlayer;
    }

    public void setPlayer(Player mPlayer) {
        this.mPlayer = mPlayer;
    }

    public void setPlaying(int playing) {
        this.playing = playing;
    }

    public boolean isEmpty(){
        if(this.playing < 0 ){
            return true;
        }
        return false;
    }

    public synchronized int isPlaying(){
        return this.playing;
    }

    public void setDuration(int duration){
        this.duration = duration;
    }

    public synchronized int getDuration(){
        return this.duration;
    }
}
