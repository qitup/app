package dubs.queueitup;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.*;
import com.spotify.sdk.android.authentication.*;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import android.view.View;
import android.view.View.OnClickListener;

import org.json.JSONObject;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

import android.webkit.CookieManager;

import java.net.CookieHandler;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Map;

import static com.spotify.sdk.android.player.PlayerEvent.*;

public class MainActivity extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private static final String HOST_EMULATOR = "10.0.2.2:8081";
    private static final String CLIENT_ID = "SPOTIFY_ID";
    private static final String REDIRECT_URI = "queueitup-login://callback";

    private Player mPlayer;
    private SpotifyApi api;
    private SpotifyService spotify;
    ViewPager simpleViewPager;
    TabLayout tabLayout;
    Button button;
    private WebView mWebview;
    private String baseURL = BuildConfig.scheme + "://" + getHost();
    private RequestQueue requestQueue;
    java.net.CookieManager systemCookies;


    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // get the reference of ViewPager and TabLayout
        simpleViewPager = (ViewPager) findViewById(R.id.simpleViewPager);
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());

        // Add Fragments to adapter one by one
        adapter.addFragment(new PartyPage(), "Party");
        adapter.addFragment(new QueuePage(), "Queue");
        adapter.addFragment(new SearchPage(), "Search");

        simpleViewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.simpleTabLayout);
        tabLayout.setupWithViewPager(simpleViewPager);

//        TabLayout.Tab firstTab = tabLayout.newTab();
//        tabLayout.addTab(firstTab); // add  the tab at in the TabLayout
//        TabLayout.Tab secondTab = tabLayout.newTab();
//        tabLayout.addTab(secondTab); // add  the tab  in the TabLayout
//        TabLayout.Tab thirdTab = tabLayout.newTab();
//        tabLayout.addTab(thirdTab); // add  the tab at in the TabLayout

//        firstTab.setText("Search"); // set the Text for the first Tab
//        firstTab.setIcon(R.drawable.ic_search_white_48dp); // set an icon for the tab
//        secondTab.setText("Queue"); // set the Text for the second Tab
//        secondTab.setIcon(R.drawable.ic_reorder_white_48dp); // set an icon for the second tab
//        thirdTab.setText("Party"); // set the Text for the first Tab
//        thirdTab.setIcon(R.drawable.ic_search_white_48dp); // set an icon for the first tab

        CookieManager cookieManager = CookieManager.getInstance();

        mWebview = new WebView(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(mWebview, true);
        } else {
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(mWebview, true);
        }

        final WebSettings settings = mWebview.getSettings();
        settings.setAppCacheEnabled(true);
        settings.setBuiltInZoomControls(true);

        systemCookies = new java.net.CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(systemCookies);

        requestQueue = Volley.newRequestQueue(this);

        String url = baseURL + "/auth/spotify";

        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String request_url = request.getUrl().toString();

                if (request_url.startsWith(baseURL + "/auth/spotify/callback")) {
                    String cookie = CookieManager.getInstance().getCookie(request.getUrl().getHost());

                    if (cookie != null) {
                        String[] parts = cookie.split("=");
                        systemCookies.getCookieStore().add(URI.create(baseURL), new HttpCookie(parts[0], parts[1]));
                    }
                    MainActivity.this.finishAuthentication(request_url);

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
                    mWebview = null;
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

//
//        // Add the request to the RequestQueue.
//        int party = R.layout.party_page;
//                Button button2 = (Button) party_page find(R.id.joinPartyButton);
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("MainActivity", "You attempted to join a party");
//            }
//        });

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

        requestQueue.add(request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());

//
//        switch (playerEvent) {
//            case kSpPlaybackNotifyMetadataChanged:
//                Log.d("MainActivity", mPlayer.getMetadata().currentTrack.toString());
//                Log.d("MainActivity", auth_token);
//
//                spotify.getAlbum("7xl50xr9NDkd3i2kBbzsNZ", new Callback<Album>() {
//                    @Override
//                    public void success(Album album, Response response) {
//                        Log.d("Album success", album.name);
//                    }
//
//                    @Override
//                    public void failure(RetrofitError error) {
//                        Log.d("Album failure", error.toString());
//                    }
//                });
//
//
//                break;
//            // Handle event type as necessary
//            default:
//                break;
//        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");

//        mPlayer.playUri(null, "spotify:track:5SiEPQmziTAi5DHNhB3Wz5", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {

    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    public static String getHost() {
        return (Build.PRODUCT).contains("sdk") ? HOST_EMULATOR : BuildConfig.HOST;
    }
}
