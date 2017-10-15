package dubs.queueitup;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    java.net.CookieManager systemCookies;
    private String baseURL = BuildConfig.scheme + "://" + getHost();
    private static final String HOST_EMULATOR = "10.0.2.2:8081";
    private static final String CLIENT_ID = "SPOTIFY_ID";
    private static final String REDIRECT_URI = "queueitup-login://callback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        CookieManager cookieManager = CookieManager.getInstance();

        final WebView mWebview = (WebView) findViewById(R.id.webView);

        final WebSettings settings = mWebview.getSettings();
        settings.setAppCacheEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(mWebview, true);
        } else {
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(mWebview, true);
        }

//        final WebSettings settings = mWebview.getSettings();
//        settings.setAppCacheEnabled(true);
//        settings.setBuiltInZoomControls(true);

        systemCookies = new java.net.CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(systemCookies);


        String url = baseURL + "/auth/spotify";

        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String request_url = request.getUrl().toString();

                if (request_url.startsWith(baseURL + "/auth/spotify/callback")) {
                    String cookieHeader = CookieManager.getInstance().getCookie(request.getUrl().getHost());

                    // If there are cookies then add them to the cookie store for future requests
                    if (cookieHeader != null) {
                        List<HttpCookie> cookies = HttpCookie.parse(cookieHeader);
                        URI baseURI = URI.create(baseURL);
                        for (HttpCookie cookie : cookies) {
                            systemCookies.getCookieStore().add(baseURI, cookie);
                        }
                    }
                    LoginActivity.this.finishAuthentication(request_url);

                    mWebview.clearCache(true);

                    mWebview.onPause();
                    mWebview.removeAllViews();
                    mWebview.destroyDrawingCache();

                    // NOTE: This pauses JavaScript execution for ALL WebViews,
                    // do not use if you have other WebViews still alive.
                    // If you create another WebView after calling this,
                    // make sure to call mWebView.resumeTimers().
                    mWebview.pauseTimers();

                    mWebview.destroy();
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
//                loadingFinished = false;
                //SHOW LOADING IF IT ISNT ALREADY VISIBLE
            }

            @Override
            public void onPageFinished(WebView view, String url) {
//                if(!redirect){
//                    loadingFinished = true;
//                }
//
//                if(loadingFinished && !redirect){
//                    //HIDE LOADING IT HAS FINISHED
//                } else{
//                    redirect = false;
//                }
                super.onPageFinished(view, url);
            }
        });
        mWebview.loadUrl(url);
        setContentView(mWebview);


    }

    private void finishAuthentication(String exchange_url) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, exchange_url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        Log.d("MainActivity", "Response is: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", "That didn't work!" + error.toString());
                    }
                });

        RequestSingleton.getInstance(this).addToRequestQueue(request);
    }

    public static String getHost() {
        return (Build.PRODUCT).contains("sdk") ? HOST_EMULATOR : BuildConfig.HOST;
    }
}