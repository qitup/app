package dubs.queueitup;

import android.content.Context;

/**
 * Created by ryanschott on 2017-11-06.
 */

public class PartySingleton {
    private static PartySingleton mInstance;
    private static PartySocket partySocket;
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
}
