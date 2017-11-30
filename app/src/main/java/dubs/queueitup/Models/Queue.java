package dubs.queueitup.Models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by ryanschott on 2017-11-01.
 */

public class Queue implements Parcelable{

    private List<TrackItem> queue_items;

    protected Queue(Parcel in) {
        queue_items = new ArrayList<>();
        in.readTypedList(queue_items, TrackItem.CREATOR);
    }

    public Queue(List<TrackItem> list){
        queue_items = list;
    }

    public static final Parcelable.Creator<Queue> CREATOR = new Parcelable.Creator<Queue>() {
        @Override
        public Queue createFromParcel(Parcel in) {
            return new Queue(in);
        }

        @Override
        public Queue[] newArray(int size) {
            return new Queue[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public List<TrackItem> getQueue_items(){
        return queue_items;
    }

    public void setQueue_items(List<TrackItem> list){
        queue_items = list;
    }

    public void removeItem(int position){
        queue_items.remove(position);
    }

    public void addItem(TrackItem item){
        queue_items.add(queue_items.size(), item);
    }

    public void addItemAt(TrackItem item, int position){
        queue_items.add(position, item);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(queue_items);

    }
}
