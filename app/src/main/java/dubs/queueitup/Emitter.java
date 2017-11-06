package dubs.queueitup;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import org.apache.cordova.CallbackContext;
//import org.apache.cordova.PluginResult;
//import org.apache.cordova.PluginResult.Status;

public abstract class Emitter {
    private static final String TAG = "Emitter";
        private Context ctx = null;

    public Emitter() {
    }

    public void setCallback(final Context ctx) {
        this.ctx = ctx;
    }

    protected void emit(final String eventName) {
        this.emit(eventName, new JSONArray());
    }

    protected void emit(final String eventName, final Object data) {
        String str = (data != null) ? data.toString() : "";
        this.emit(eventName, new JSONArray().put(str));
    }

    protected void emit(final String eventName, final JSONArray data) {
        if (eventName == null || eventName.length() < 1) {
            throw new IllegalArgumentException("eventName is null or empty!");
        }


        final Context ctx = this.ctx;
        if (ctx == null) {
            Log.d(
                    TAG,
                    "Emit '" + eventName + "' triggered, but CallbackContext was null."
            );
            return;
        }

        try {
            final JSONObject arg = new JSONObject()
                    .put("type", eventName)
                    .put("args", data);

            final GlobalState gs = (GlobalState) ctx.getApplicationContext();
            PartySocket socket = gs.getSocket();
            socket.send(arg.toString());
        } catch (JSONException ex) {
            Log.e(
                    TAG,
                    "An error occured while encoding the JSON for raising event '" + eventName + "'.",
                    ex
            );
        }
    }
}
