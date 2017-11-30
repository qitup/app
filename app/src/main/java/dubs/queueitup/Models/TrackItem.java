package dubs.queueitup.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by ryanschott on 2017-11-29.
 */

public class TrackItem extends QItem {

    private String uri;
    private Track data;

    protected TrackItem(Parcel in) {
        super(in);
        uri = in.readString();
    }

    public TrackItem(String Type, String addBy, String addAt, boolean playing, String uri){
        super(Type, addBy, addAt, playing);
        this.uri = uri;
    }

    public TrackItem(QItem item){
        super(item.getType(), item.getAddedBy(), item.getAddedAt(), item.isPlaying());
    }

    public static final Parcelable.Creator<TrackItem> CREATOR = new Parcelable.Creator<TrackItem>() {
        @Override
        public TrackItem createFromParcel(Parcel in) {
            return new TrackItem(in);
        }

        @Override
        public TrackItem[] newArray(int size) {
            return new TrackItem[size];
        }
    };


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(uri);
    }

    public String getUri(){
        return uri;
    }

    public void loaded(Track track){
        data = track;
    }

    public Track getTrack(){
        return data;
    }

}
