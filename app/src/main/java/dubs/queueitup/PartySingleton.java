package dubs.queueitup;

import android.content.Context;

/**
 * Created by ryanschott on 2017-11-06.
 */

public class PartySingleton {
    private static PartySingleton mInstance;
    private PartySocket partySocket = null;
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
        if(party.getConnection() == null){
            return;
        }
        partySocket = party;
    }

    public synchronized PartySocket getSocket(){
        return partySocket;
    }
}
