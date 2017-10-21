package dubs.queueitup;

/**
 * Created by ryanschott on 2017-10-18.
 */

import android.support.annotation.Nullable;

public interface Player {

    void play(String url);

    void pause();

    void resume();

    boolean isPlaying();

    @Nullable
    String getCurrentTrack();

    void release();
}