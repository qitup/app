package dubs.queueitup.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanschott on 2017-10-30.
 */

public class Party implements Parcelable{
    private String name;
    private String code;
    private String host;
    private String id;
    private List<User> attendees;
    private Queue queue;


    protected Party(Parcel in) {
        name = in.readString();
        code = in.readString();
        host = in.readString();
        id = in.readString();
        attendees = new ArrayList<>();
        in.readTypedList(attendees, User.CREATOR);
        queue = in.readParcelable(Queue.class.getClassLoader());
    }

    public Party(String pname, String jcode, String host_name, String id, List<User> attendees, Queue queue){
        name = pname;
        code = jcode;
        this.id = id;
        host = host_name;
        this.attendees = attendees;
        this.queue = queue;
    }

    public static final Creator<Party> CREATOR = new Creator<Party>() {
        @Override
        public Party createFromParcel(Parcel in) {
            return new Party(in);
        }

        @Override
        public Party[] newArray(int size) {
            return new Party[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String getName(){
        return name;
    }

    public String getCode(){
        return code;
    }

    public String getHost(){
        return host;
    }

    public String getID(){
        return id;
    }

    public void setQueue(Queue queue){
        this.queue = queue;
    }

    public Queue getQueue(){
        return this.queue;
    }

    public int numAttendees(){
        return attendees.size();
    }

    public List<User> getAttendees(){
        return attendees;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(code);
        dest.writeString(host);
        dest.writeString(id);
        dest.writeTypedList(attendees);
        dest.writeParcelable(queue, 0);
    }
}
