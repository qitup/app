package dubs.queueitup;

import com.spotify.sdk.android.player.Metadata;

import java.util.List;

import dubs.queueitup.Models.TrackItem;
import kaaes.spotify.webapi.android.models.Track;


/**
 * Created by ryanschott on 2017-10-18.
 */

public class Search {
    public interface View {
        void reset();

        void addData(List<Track> items);

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
