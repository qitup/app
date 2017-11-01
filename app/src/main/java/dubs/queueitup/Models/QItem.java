package dubs.queueitup.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ryanschott on 2017-11-01.
 */

public class QItem implements Parcelable{
    private String type;
    private String addedBy;
    private String addedAt;
    private String uri;

    protected QItem(Parcel in) {
        type = in.readString();
        addedBy = in.readString();
        addedAt = in.readString();
        uri = in.readString();
    }

    public QItem(String Type, String addBy, String addAt, String id){
        type = Type;
        addedBy = addBy;
        addedAt = addAt;
        uri = id;
    }

    public static final Parcelable.Creator<QItem> CREATOR = new Parcelable.Creator<QItem>() {
        @Override
        public QItem createFromParcel(Parcel in) {
            return new QItem(in);
        }

        @Override
        public QItem[] newArray(int size) {
            return new QItem[size];
        }
    };


    public String getType(){
        return type;
    }

    public String getAddedAt(){
        return addedAt;
    }

    public String getAddedBy(){
        return addedBy;
    }

    public String getUri(){
        return uri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(addedAt);
        dest.writeString(addedBy);
        dest.writeString(uri);
    }
}
