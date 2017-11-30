package dubs.queueitup;

import com.spotify.sdk.android.player.Metadata;

import java.util.List;

import dubs.queueitup.Models.TrackItem;
import kaaes.spotify.webapi.android.models.Track;


/**
 * Created by ryanschott on 2017-10-18.
 */

public class Queue {
    public interface View {
        void reset();

        void addData(List<TrackItem> items);

        void addPlaying(List<TrackItem> items);

        void removeItem(int position);

        void setPlaying(int position);

        void clearData();
    }
}
