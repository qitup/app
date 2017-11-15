package dubs.queueitup;

import com.spotify.sdk.android.player.Metadata;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;


/**
 * Created by ryanschott on 2017-10-18.
 */

public class Search {
    public interface View {
        void reset();

        void addData(List<Track> items);

        void addPlaying(List<Track> items);

        void removeItem(int position);
    }

    public interface ActionListener {

        void init(String token);

        String getCurrentQuery();

        void search(String searchQuery);

        void loadMoreResults();

        void selectTrack(Track item);

        void resume();

        void pause();

        void destroy();

    }
}
