package dubs.queueitup.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Time;

/**
 * Created by ryanschott on 2017-11-22.
 */

public class User implements Parcelable {
    private String id;
    private String name;
    private String avatar_url;
    private boolean canHost;

    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        avatar_url = in.readString();
        canHost = in.readInt() != 0;
    }

    public User(String id, String name, String avatar_url){
        this.id = id;
        this.name = name;
        this.avatar_url = avatar_url;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String getName(){
        return this.name;
    }

    public String getAvatar_url(){
        return this.avatar_url;
    }

    public String getId(){
        return this.id;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(avatar_url);
        dest.writeInt(canHost ? 1 : 0);
    }
}
