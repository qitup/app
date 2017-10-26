package dubs.queueitup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import com.spotify.sdk.android.player.Error;

public class MainActivity extends AppCompatActivity implements PartyPage.OnCreatePartyButtonListener, SearchPage.OnTrackItemSelected, SpotifyPlayer.NotificationCallback, ConnectionStateCallback, QueuePage.OnQueueItemSelected {

    private NoSwiperPager viewPager;
    private AHBottomNavigation bottomNavigation;
    private BottomBarAdapter pagerAdapter;
    private static final String HOST_EMULATOR = "10.0.2.2:8081";
    private static final String CLIENT_ID = "SPOTIFY_ID";
    private static final String REDIRECT_URI = "queueitup-login://callback";
    private static final int REQUEST_CODE = 1337;
    private static final int REQUEST_CODE_CREATE = 1338;
    private static final int REQUEST_CODE_JOIN = 1339;


    private Player mPlayer;
    private SpotifyApi api;
    private PartySocket partySocket = null;
    private QueueAdapter mAdapter = null;
    private QueuePresenter mPresenter = null;
    NoSwiperPager simpleViewPager;
    private WebView mWebview;
    private String auth_token;
    private Intent intent;
    private String baseURL = BuildConfig.scheme + "://" + getHost();
    private RequestQueue requestQueue;
    private SharedPreferences sharedPref;
    java.net.CookieManager systemCookies;
    private boolean notificationVisible = false;

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

        addBottomNavigationItems();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, REQUEST_CODE);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                bottomNavigation.restoreBottomNavigation();
            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigation.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (!wasSelected)
                    viewPager.setCurrentItem(position);

                return true;
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    RequestSingleton.setJWT_token(data.getStringExtra("auth_token"));
                    RequestSingleton.setSpotify_auth_token(getAuthToken());
                    //                SharedPreferences.Editor editor = sharedPref.edit();
                    //                editor.putString("auth_token", data.getStringExtra("auth_token"));
                    //                editor.apply();
                }
            } else if (requestCode == REQUEST_CODE_CREATE) {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Successfully created party", Toast.LENGTH_SHORT).show();

                    mPresenter = ((QueuePage) pagerAdapter.getItem(1)).getPresenter();
                    try {
                        partySocket = newPartySocket(new URI(data.getStringExtra("socket_url")));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    partySocket.connect();
                    initPlayer();
                }
            } else if (requestCode == REQUEST_CODE_JOIN) {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Successfully joined party", Toast.LENGTH_SHORT).show();

                    mPresenter = ((QueuePage) pagerAdapter.getItem(1)).getPresenter();
                    try {
                        partySocket = newPartySocket(new URI(data.getStringExtra("socket_url")));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    partySocket.connect();
                    initPlayer();
                }
            }
        }
    }

    private PartySocket newPartySocket(URI uri) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("Authorization", "Bearer " + RequestSingleton.getJWT_token());

        return new PartySocket(getAuthToken(), uri, new Draft_6455(), params, 3000) {
            @Override
            public void onMessage(String message) {
                JSONObject response = null;
                JSONObject track = null;

                try {
                    response = new JSONObject(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    switch (response.getString("type")) {
                        case "queue.push":
                            try {
                                track = response.getJSONObject("added").getJSONObject("item");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String uri = track.getString("uri");

                            String[] parts = uri.split(":");
                            final String id = parts[parts.length - 1];

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mPresenter.addQueueItem(id);
                                    Toast.makeText(getApplicationContext(), "Added song to queue", Toast.LENGTH_SHORT).show();
                                }
                            });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
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
        switch (type) {
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
        fragment.setArguments(passFragmentArguments(R.color.textColorDefault));
        return fragment;
    }

    @NonNull
    private Bundle passFragmentArguments(int color) {
        Bundle bundle = new Bundle();
        bundle.putInt("color", color);
        return bundle;
    }

    // Fragment interface overrides

    @Override
    public void onCreateParty(View v) {
        switch (v.getId()) {
            case R.id.createPartyButton:
                Log.d("MainActivity", "Create party button clicked");
//                Toast.makeText(this, "Creating party", Toast.LENGTH_SHORT).show();
                intent = new Intent(this, CreateParty.class);
                startActivityForResult(intent, REQUEST_CODE_CREATE);
                break;
            case R.id.joinPartyButton:
                Log.d("MainActivity", "Join party button clicked");
//                Toast.makeText(this, "Joining party", Toast.LENGTH_SHORT).show();
                intent = new Intent(this, JoinParty.class);
                startActivityForResult(intent, REQUEST_CODE_JOIN);
                break;
        }

    }

    @Override
    public void addTrack(Track track) {
        JSONObject message = new JSONObject();
        JSONObject queue_item = new JSONObject();

        try {
            message.put("type", "queue.push");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            queue_item.put("type", "spotify_track");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            queue_item.put("uri", track.uri.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            message.put("item", queue_item);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        partySocket.send(message.toString());
        Log.d("MainActivity", track.uri.toString());
    }

    public void initPlayer() {
        Config playerConfig = new Config(this, RequestSingleton.getSpotify_auth_token(), CLIENT_ID);
        Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                mPlayer = spotifyPlayer;
                mPlayer.addConnectionStateCallback(MainActivity.this);
                mPlayer.addNotificationCallback(MainActivity.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });
    }

    public void playTrack(Track item){
        mPlayer.playUri(null, item.uri, 0, 0);
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
        bottomNavigation.setDefaultBackgroundColor(fetchColor(R.color.colorTopBottomBar));
        bottomNavigation.setAccentColor(fetchColor(R.color.colorPrimaryDark));
        bottomNavigation.setInactiveColor(fetchColor(R.color.colorBottomNavigationInactiveColored));

        //  Enables Reveal effect
        bottomNavigation.setColored(false);

        //  Displays item Title always (for selected and non-selected items)
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
    }


    /**
     * Adds (items) {@link AHBottomNavigationItem} to {@link AHBottomNavigation}
     * Also assigns a distinct color to each Bottom Navigation item, used for the color ripple.
     */
    private void addBottomNavigationItems() {
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.title_party, R.drawable.ic_home_white_24dp, R.color.colorTopBottomBar);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.title_queue, R.drawable.ic_reorder_white_48dp, R.color.colorTopBottomBar);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.title_search, R.drawable.ic_search_white_48dp, R.color.colorTopBottomBar);

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


    public String getAuthToken() {
        String jwt_token = null;


        try {
            jwt_token = JWTUtils.decoded(RequestSingleton.getJWT_token());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject auth = new JSONObject();

        try {
            auth = new JSONObject(jwt_token);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
            auth_token = auth.getString("access_token");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        return auth_token;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("MainActivity", "Login failed");
    }


    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
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
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

}

class JWTUtils {

    public static String decoded(String JWTEncoded) throws Exception {
        try {
            String[] split = JWTEncoded.split("\\.");
            Log.d("JWT_DECODED", "Header: " + getJson(split[0]));
            Log.d("JWT_DECODED", "Body: " + getJson(split[1]));
            return getJson(split[1]);
        } catch (UnsupportedEncodingException e) {
            //Error
        }
        String string = null;
        return string;
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}

class PartySocket extends WebSocketClient {

    SpotifyApi spotifyApi;


    private SpotifyService mSpotifyApi = null;

    public PartySocket(String accessToken, URI serverUri, Draft draft, Map<String, String> httpHeaders, int connectTimeout) {
        super(serverUri, draft, httpHeaders, connectTimeout);

        spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(accessToken);
        mSpotifyApi = spotifyApi.getService();
    }

    public PartySocket(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("opened connection");
        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // The codecodes are documented in class org.java_websocket.framing.CloseFrame
        System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }
}
