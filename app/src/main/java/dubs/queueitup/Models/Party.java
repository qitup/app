package dubs.queueitup.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by ryanschott on 2017-10-30.
 */

public class Party implements Parcelable{
    private String name;
    private String code;
    private String host;
//    private List<String> attendees;


    protected Party(Parcel in) {
        name = in.readString();
        code = in.readString();
        host = in.readString();
//        attendees = in.createStringArrayList();
    }

    public Party(String pname, String jcode, String host_name){
        name = pname;
        code = jcode;
        host = host_name;
//        attendees = attendees_list;
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(code);
        dest.writeString(host);
//        dest.writeStringList(attendees);
    }
}
