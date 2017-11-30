package dubs.queueitup;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dubs.queueitup.Models.*;
import dubs.queueitup.Models.Queue;

public class CreateParty extends AppCompatActivity {

    private String baseURL = BuildConfig.scheme + "://" + getHost();
    private static final String HOST_EMULATOR = "10.0.2.2:8081";

    @InjectView(R.id.partyName) EditText _party_name;
    @InjectView(R.id.partyCode) EditText _party_code;
    @InjectView(R.id.timeout) EditText _time_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_party);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.inject(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void submitParty(View view) {
        Log.d("CreateParty", _party_name.getText().toString());
        Log.d("CreateParty", _party_code.getText().toString());
        Log.d("CreateParty", _time_out.getText().toString());

        JSONObject party_info = new JSONObject();
        JSONObject settings = new JSONObject();
        try {
            party_info.put("name", _party_name.getText().toString());
            party_info.put("join_code", _party_code.getText().toString());
            settings.put("timeout", Integer.parseInt(_time_out.getText().toString()));
            party_info.put("settings", settings);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, baseURL + "/party/", party_info,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        Log.d("CreateParty", "Response is: " + response.toString());
                        Intent intent = new Intent();
                        intent.putExtra("result_code", 1338);

                        try {
                            String name = response.getJSONObject("party").getString("name");
                            String join_code = response.getJSONObject("party").getString("join_code");
                            String id = response.getJSONObject("party").getString("id");
                          JSONObject host = response.getJSONObject("party").getJSONObject("host");
                            User host_user = new User(host.getString("id"), host.getString("name"), host.get("avatar_url").toString());
                            intent.putExtra("party_details", new Party(name, join_code, host_user, id, null, new Queue(new ArrayList<TrackItem>())));
                            intent.putExtra("socket_url", response.get("url").toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        setResult(RESULT_OK, intent);
                        CreateParty.this.finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", "That didn't work!" + error.toString());
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
