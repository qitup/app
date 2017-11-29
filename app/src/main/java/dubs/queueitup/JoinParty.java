package dubs.queueitup;

import android.content.Intent;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import dubs.queueitup.Models.Party;
import dubs.queueitup.Models.QItem;
import dubs.queueitup.Models.Queue;
import dubs.queueitup.Models.User;


public class JoinParty extends AppCompatActivity {

    private String baseURL = BuildConfig.scheme + "://" + getHost();
    private static final String HOST_EMULATOR = "10.0.2.2:8081";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_party);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void submitJoinParty(View view) {
        EditText party_code_entry = (EditText) findViewById(R.id.join_party_code);

        Log.d("JoinParty", party_code_entry.getText().toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, baseURL + "/party/join/?code="+party_code_entry.getText().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        Log.d("JoinParty", "Response is: " + response.toString());
                        Intent intent = new Intent();
                        intent.putExtra("result_code", 1339);
                        try {
                            intent.putExtra("socket_url", response.get("url").toString());
                            String name = response.getJSONObject("party").getString("name");
                            String join_code = response.getJSONObject("party").getString("join_code");
                            String party_id = response.getJSONObject("party").getString("id");
                            JSONObject host = response.getJSONObject("party").getJSONObject("host");
                            User host_user = new User(host.getString("id"), host.getString("name"), host.get("avatar_url").toString());
                            JSONArray guests = response.getJSONObject("party").getJSONArray("attendees");

                            List<User> users = new ArrayList<User>();
                            for (int i = 0; i < guests.length(); i++){
                                JSONObject guest = guests.getJSONObject(i).getJSONObject("user");
                                User user = new User(guest.getString("id"), guest.get("name").toString(), guest.get("avatar_url").toString());
                                users.add(i, user);
                            }

                            JSONObject result = response.getJSONObject("queue");
                            JSONArray items = result.getJSONArray("items");
                            List<QItem> array = new ArrayList<QItem>();
                            for (int i = 0; i < items.length(); i++){
                                JSONObject item = items.getJSONObject(i);
                                QItem q_item = new QItem(item.get("type").toString(), item.get("added_by").toString(), item.get("added_at").toString(), item.getJSONObject("state").getBoolean("playing"));
                                array.add(i, q_item);
                            }
                            Queue queue = new Queue(array);
                            intent.putExtra("party_details", new Party(name, join_code, host_user, party_id, users, queue));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        setResult(RESULT_OK, intent);

                        JoinParty.this.finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse.statusCode == 400) {
                            Toast.makeText(JoinParty.this, "Party not found", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("Error", "That didn't work!" + error.toString());
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + RequestSingleton.getJWT_token());

                return params;
            }
        };


        RequestSingleton.getInstance(this).addToRequestQueue(request);
    }

    public static String getHost() {
        return (Build.PRODUCT).contains("sdk") ? HOST_EMULATOR : BuildConfig.HOST;
    }
}
