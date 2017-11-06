package dubs.queueitup;

import android.app.Application;

/**
 * Created by ryanschott on 2017-11-05.
 */

public class GlobalState extends Application {
    PartySocket socket = null;


    public void setSocket(PartySocket party){
        socket = party;
    }

    public PartySocket getSocket(){
        return socket;
    }

}
