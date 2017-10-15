package dubs.queueitup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Base64;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.CookieHandler;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Map;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.notification.AHNotification;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public class MainActivity extends AppCompatActivity implements PartyPage.OnCreatePartyButtonListener, SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private final int[] colors = {R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent};

    private NoSwiperPager viewPager;
    private AHBottomNavigation bottomNavigation;
    private BottomBarAdapter pagerAdapter;
    private static final String HOST_EMULATOR = "10.0.2.2:8081";
    private static final String CLIENT_ID = "SPOTIFY_ID";
    private static final String REDIRECT_URI = "queueitup-login://callback";

    private Player mPlayer;
    private SpotifyApi api;
    private SpotifyService spotify;
    NoSwiperPager simpleViewPager;
    private WebView mWebview;
    private String baseURL = BuildConfig.scheme + "://" + getHost();
    private RequestQueue requestQueue;
    java.net.CookieManager systemCookies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_AUTO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setupViewPager();


        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        setupBottomNavBehaviors();
        setupBottomNavStyle();


        CookieManager cookieManager = CookieManager.getInstance();

        mWebview = new WebView(this);
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

        requestQueue = Volley.newRequestQueue(this);

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

    private void setupViewPager() {
        viewPager = (NoSwiperPager) findViewById(R.id.viewpager);
        viewPager.setPagingEnabled(true);
        pagerAdapter = new BottomBarAdapter(getSupportFragmentManager());

        pagerAdapter.addFragments(createFragment(0));
        pagerAdapter.addFragments(createFragment(1));
        pagerAdapter.addFragments(createFragment(2));

        viewPager.setAdapter(pagerAdapter);
    }

    @NonNull
    private Fragment createFragment(int type) {
        Fragment fragment = new PartyPage();
        switch(type){
            case 0:
                fragment = new PartyPage();
                break;
            case 1:
                fragment = new QueuePage();
                break;
            case 2:
                fragment = new SearchPage();
                break;
        }
        fragment.setArguments(passFragmentArguments(R.color.selectedTextColor));
        return fragment;
    }

    @NonNull
    private Bundle passFragmentArguments(int color) {
        Bundle bundle = new Bundle();
        bundle.putInt("color", color);
        return bundle;
    }

        @Override
    public void onCreateParty(String password){
        Log.d("MainActivity", "Create party button clicked" + password);
            Intent intent = new Intent(this, CreateParty.class);
            startActivity(intent);
    }


    public void setupBottomNavBehaviors() {
//        bottomNavigation.setBehaviorTranslationEnabled(false);

        /*
        Before enabling this. Change MainActivity theme to MyTheme.TranslucentNavigation in
        AndroidManifest.
        Warning: Toolbar Clipping might occur. Solve this by wrapping it in a LinearLayout with a top
        View of 24dp (status bar size) height.
         */
        bottomNavigation.setTranslucentNavigationEnabled(false);
    }

    /**
     * Adds styling properties to {@link AHBottomNavigation}
     */
    private void setupBottomNavStyle() {
        /*
        Set Bottom Navigation colors. Accent color for active item,
        Inactive color when its view is disabled.
        Will not be visible if setColored(true) and default current item is set.
         */
        bottomNavigation.setDefaultBackgroundColor(Color.WHITE);
        bottomNavigation.setAccentColor(fetchColor(R.color.tabTextColor));
        bottomNavigation.setInactiveColor(fetchColor(R.color.tabTextColor));

        // Colors for selected (active) and non-selected items.
        bottomNavigation.setColoredModeColors(Color.WHITE,
                fetchColor(R.color.selectedTextColor));

        //  Enables Reveal effect
        bottomNavigation.setColored(true);

        //  Displays item Title always (for selected and non-selected items)
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
    }


    /**
     * Adds (items) {@link AHBottomNavigationItem} to {@link AHBottomNavigation}
     * Also assigns a distinct color to each Bottom Navigation item, used for the color ripple.
     */
    private void addBottomNavigationItems() {
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.title_party, R.drawable.ic_home_white_24dp, colors[0]);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.title_queue, R.drawable.ic_reorder_white_48dp, colors[1]);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.title_search, R.drawable.ic_search_white_48dp, colors[2]);

        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);
    }


    /**
     * Simple facade to fetch color resource, so I avoid writing a huge line every time.
     *
     * @param color to fetch
     * @return int color value.
     */
    private int fetchColor(@ColorRes int color) {
        return ContextCompat.getColor(this, color);
    }

}

class JWTUtils {

    public static void decoded(String JWTEncoded) throws Exception {
        try {
            String[] split = JWTEncoded.split("\\.");
            Log.d("JWT_DECODED", "Header: " + getJson(split[0]));
            Log.d("JWT_DECODED", "Body: " + getJson(split[1]));
        } catch (UnsupportedEncodingException e) {
            //Error
        }
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}
