package dubs.queueitup;

/**
 * Created by Ryan on 2017-11-28.
 */
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private static String baseURL = BuildConfig.scheme + "://" + getHost();
    private static final String HOST_EMULATOR = "10.0.2.2:8081";

    @InjectView(R.id.input_name) EditText _nameText;
    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_signup) Button _signupButton;
    @InjectView(R.id.link_login) TextView _loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.inject(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        JSONObject creds = new JSONObject();
        try {
            creds.put("username", name);
            creds.put("email", email);
            creds.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // TODO: Implement your own signup logic here.
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, baseURL + "/signup", creds,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        Log.d(TAG, "Response is: " + response.toString());

                        Intent intent = new Intent();
                        try {
                            intent.putExtra("jwt_token", response.getString("token"));
                            intent.putExtra("result_code", 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        onSignupSuccess(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", "That didn't work!" + error.toString());
                        onSignupFailed();
                    }
                });

        RequestSingleton.getInstance(this).addToRequestQueue(request);

//        new android.os.Handler().postDelayed(
//                new Runnable() {
//                    public void run() {
//                        // On complete call either onSignupSuccess or onSignupFailed
//                        // depending on success
//                        onSignupSuccess();
//                        // onSignupFailed();
//                    }
//                }, 3000);
    }


    public void onSignupSuccess(Intent intent) {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public static String getHost() {
        return (Build.PRODUCT).contains("sdk") ? HOST_EMULATOR : BuildConfig.HOST;
    }

}