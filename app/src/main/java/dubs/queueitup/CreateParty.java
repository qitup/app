package dubs.queueitup;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreateParty extends AppCompatActivity {

    private String baseURL = BuildConfig.scheme + "://" + getHost();
    private static final String HOST_EMULATOR = "10.0.2.2:8081";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_party);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void submitParty(View view) {
        EditText party_name_entry = (EditText) findViewById(R.id.partyName);
        EditText party_code_entry = (EditText) findViewById(R.id.party_code);

        Log.d("CreateParty", party_name_entry.getText().toString());
        Log.d("CreateParty", party_code_entry.getText().toString());

        JSONObject party_info = new JSONObject();
        try {
            party_info.put("name", party_name_entry.getText().toString());
            party_info.put("join_code", party_code_entry.getText().toString());
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
