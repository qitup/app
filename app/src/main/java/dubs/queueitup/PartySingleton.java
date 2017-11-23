package dubs.queueitup;

import android.content.Context;
import android.util.Log;

import dubs.queueitup.Models.Queue;

/**
 * Created by ryanschott on 2017-11-06.
 */

public class PartySingleton {
    private static PartySingleton mInstance;
    private PartySocket partySocket = null;
    private boolean isHost = false;
    private static Context mCtx;


    private PartySingleton(Context context) {
        mCtx = context;
    }

    public static synchronized PartySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PartySingleton(context);
        }
        return mInstance;
    }

    public synchronized void setSocket(PartySocket party){
        this.partySocket = party;
    }

    public synchronized PartySocket getSocket(){
        return this.partySocket;
    }

    public synchronized void setHost(boolean isHost){
        this.isHost = isHost;
    }



    public synchronized boolean isHost(){
        return isHost;
    }
}
