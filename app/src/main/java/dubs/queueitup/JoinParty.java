package dubs.queueitup;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


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

                        setResult(RESULT_OK, intent);

                        JoinParty.this.finish();
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
