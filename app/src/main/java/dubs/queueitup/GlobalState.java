package dubs.queueitup;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by ryanschott on 2017-11-05.
 */

public class GlobalState extends IntentService {
    PartySocket socket = null;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GlobalState(String name) {
        super(name);
    }


    public void setSocket(PartySocket party){
        socket = party;
    }

    public PartySocket getSocket(){
        return socket;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
