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
    protected Context ctx = null;
    private String msgType;

    public Emitter(String msgType) {
        this.msgType = msgType;
    }

    public void setCallback(final Context ctx) {
        this.ctx = ctx;
    }

    protected void emit(final String eventName) {
        this.emit(eventName, new JSONObject());
    }

    protected void emit(final String eventName, final Object data) {
        String str = (data != null) ? data.toString() : "";
        try {
            this.emit(eventName, "", new JSONObject().put("str",str));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void emit(final String eventName, final String eventType, final JSONObject data) {
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
            final JSONObject event = new JSONObject();
            final JSONObject arg = new JSONObject()
                    .put("type", eventType)
                    .put("name", eventName)
                    .put("metadata", data);

            event.put("type", this.msgType);
            event.put("event", arg);
            Log.d("Emitter", event.toString());
            PartySingleton.getInstance(ctx).getSocket().send(event.toString());
        } catch (JSONException ex) {
            Log.e(
                    TAG,
                    "An error occured while encoding the JSON for raising event '" + eventName + "'.",
                    ex
            );
        }
    }
}
