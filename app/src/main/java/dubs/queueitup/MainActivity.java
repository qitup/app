package dubs.queueitup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.telecom.Call;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dubs.queueitup.Models.Party;
import dubs.queueitup.Models.QItem;
import dubs.queueitup.Models.Queue;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Track;

public class MainActivity extends AppCompatActivity implements PartyPage.OnCreatePartyButtonListener, SearchPage.OnTrackItemSelected, SpotifyPlayer.NotificationCallback, ConnectionStateCallback, QueuePage.OnQueueItemSelected, QueuePage.OnMediaPlayerAction {
    private static final String TAG = "MainActivity";
    private NoSwiperPager viewPager;
    private AHBottomNavigation bottomNavigation;
    private BottomBarAdapter pagerAdapter;
    private static final String HOST_EMULATOR = "10.0.2.2:8081";
    private static final String CLIENT_ID = BuildConfig.clientID;
    private static final String REDIRECT_URI = "queueitup-login://callback";
    private static String baseURL = BuildConfig.scheme + "://" + getHost();
    private static final int REQUEST_CODE = 1337;
    private static final int REQUEST_CODE_CREATE = 1338;
    private static final int REQUEST_CODE_JOIN = 1339;
    private SpotifyPlayer mPlayer;
    private PartySocket partySocket = null;
    private QueuePresenter mPresenter = null;
    private String auth_token;
    private Intent intent;
    private String currentAccessToken = null;
    private String currentClientId = null;
    private Party currentParty = null;

    private ConnectionEventsHandler connectionEventsHandler = new ConnectionEventsHandler("player.connection");
    private PlayerEventsHandler playerEventsHandler = new PlayerEventsHandler("player.event");

    /**
     * Used to get notifications from the system about the current network state in order
     * to pass them along to
     * {@link SpotifyPlayer#setConnectivityStatus(Player.OperationCallback, Connectivity)}
     * Note that this implies <pre>android.permission.ACCESS_NETWORK_STATE</pre> must be
     * declared in the manifest. Not setting the correct network state in the SDK may
     * result in strange behavior.
     */
    private BroadcastReceiver mNetworkStateReceiver;

    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            Log.d(TAG,"OK!");
        }

        @Override
        public void onError(Error error) {
            Log.e(TAG,"ERROR:" + error);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("PACKAGE ID", getApplicationInfo().packageName);

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
        startActivityForResult(intent, REQUEST_CODE);

        playerEventsHandler.setCallback(this);
        connectionEventsHandler.setCallback(this);

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

    @Override
    protected void onPause(){
        super.onPause();

        unregisterReceiver(mNetworkStateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set up the broadcast receiver for network events. Note that we also unregister
        // this receiver again in onPause().
        mNetworkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mPlayer != null) {
                    Connectivity connectivity = getNetworkConnectivity(getBaseContext());
                    Log.d(TAG, "Network state changed: " + connectivity.toString());
                    mPlayer.setConnectivityStatus(mOperationCallback, connectivity);
                }
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkStateReceiver, filter);

        if (mPlayer != null) {
            mPlayer.addConnectionStateCallback(this.connectionEventsHandler);
            mPlayer.addNotificationCallback(this.playerEventsHandler);
        }
    }

    public void testEmitter(){
        playerEventsHandler.onPlaybackEvent(PlayerEvent.kSpPlaybackNotifyMetadataChanged);
        playerEventsHandler.onPlaybackEvent(PlayerEvent.kSpPlaybackNotifyTrackChanged);
        playerEventsHandler.onPlaybackEvent(PlayerEvent.kSpPlaybackNotifyPlay);
        playerEventsHandler.onPlaybackEvent(PlayerEvent.kSpPlaybackNotifyPause);
        playerEventsHandler.onPlaybackEvent(PlayerEvent.kSpPlaybackNotifyLostPermission);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    RequestSingleton.setJWT_token(data.getStringExtra("jwt_token"));
                    RequestSingleton.setSpotify_auth_token(data.getStringExtra("access_token"));
                    //                SharedPreferences.Editor editor = sharedPref.edit();
                    //                editor.putString("auth_token", data.getStringExtra("auth_token"));
                    //                editor.apply();
                }
            } else if (requestCode == REQUEST_CODE_CREATE) {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Successfully created party", Toast.LENGTH_SHORT).show();

                    Bundle buns = data.getExtras();
                    Party party = (Party) buns.getParcelable("party_details");

                    Bundle args = createFragmentBundle();
                    args.putParcelable("party", party);

                    try {
                        pagerAdapter.swapFragmentAt(createFragment(3, args), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    viewPager.getAdapter().notifyDataSetChanged();

                    mPresenter = ((QueuePage) pagerAdapter.getItem(1)).getPresenter();
                    try {
                        partySocket = newPartySocket(new URI(data.getStringExtra("socket_url")));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    partySocket.connect();
                    PartySingleton.getInstance(this).setSocket(partySocket);
                }
            } else if (requestCode == REQUEST_CODE_JOIN) {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Successfully joined party", Toast.LENGTH_SHORT).show();
                    mPresenter = ((QueuePage) pagerAdapter.getItem(1)).getPresenter();

                    Bundle buns = data.getExtras();
                    currentParty = buns.getParcelable("party_details");
                    Queue queue = buns.getParcelable("queue");

                    Bundle args = createFragmentBundle();
                    args.putParcelable("party", currentParty);

                    List<QItem> items = queue.getQueue_items();

                    for (int i = 0; i < items.size(); i++) {
                        String uri = items.get(i).getUri();
                        String[] parts = uri.split(":");
                        final String id = parts[parts.length - 1];
                        mPresenter.addQueueItem(id);
                    }

                    try {
                        pagerAdapter.swapFragmentAt(createFragment(3, args), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    viewPager.getAdapter().notifyDataSetChanged();

                    try {
                        partySocket = newPartySocket(new URI(buns.getString("socket_url")));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    partySocket.connect();
                    PartySingleton.getInstance(this).setSocket(partySocket);
                }
            }
        }
    }

    private PartySocket newPartySocket(URI uri) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("Authorization", "Bearer " + RequestSingleton.getJWT_token());

        return new PartySocket(uri, new Draft_6455(), params, 3000) {
            @Override
            public void onMessage(String message) {
                JSONObject response;

                try {
                    response = new JSONObject(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                try {
                    switch (response.getString("type")) {
                        case "queue.push":
                            JSONObject track;
                            String uri;
                            try {
                                track = response.getJSONObject("item");
                                uri = track.getString("uri");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return;
                            }

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

    public static String getBaseURL(){
        return baseURL;
    }

    private void setupViewPager() {
        viewPager = (NoSwiperPager) findViewById(R.id.viewpager);
        viewPager.setPagingEnabled(true);
        pagerAdapter = new BottomBarAdapter(getSupportFragmentManager());

        try {
            pagerAdapter.addFragments(createFragment(0, createFragmentBundle()));
            pagerAdapter.addFragments(createFragment(1, createFragmentBundle()));
            pagerAdapter.addFragments(createFragment(2, createFragmentBundle()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        viewPager.setAdapter(pagerAdapter);
    }

    @NonNull
    private Fragment createFragment(int type, Bundle arguments) throws Exception {
        Fragment fragment;

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
            case 3:
                fragment = new PartyDetailsPage();
                break;
            default:
                throw new Exception("Invalid fragment type");
        }

        fragment.setArguments(arguments);

        return fragment;
    }

    @NonNull
    private Bundle createFragmentBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("color", R.color.textColorDefault);
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
        Log.d("MainActivity", track.uri);
    }

    private void pause(){
        final SpotifyPlayer player = this.mPlayer;
        if (player == null) {
            Log.wtf(TAG, "SpotifyPlayer instance was null in pause.");

            JSONObject descr = this.makeError(
                    "unknown",
                    "Received null as SpotifyPlayer in pause method."
            );
            return;
        }

        player.pause(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
//                callbackContext.success();
            }

            @Override
            public void onError(Error error) {
                Log.e(TAG, "Pause failure: " + error.toString());

                JSONObject descr = MainActivity.this.makeError(
                        "pause_failed",
                        error.toString()
                );
//                callbackContext.error(descr);
            }
        });
    }

    private void resume(){
        final SpotifyPlayer player = this.mPlayer;
        if (player == null) {
            Log.wtf(TAG, "SpotifyPlayer instance was null in resume.");

            JSONObject descr = this.makeError(
                    "unknown",
                    "Received null as SpotifyPlayer in resume method."
            );
            return;
        }

        player.resume(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
//                callbackContext.success();
            }

            @Override
            public void onError(Error error) {
                Log.e(TAG, "Resume failure: " + error.toString());

                JSONObject descr = MainActivity.this.makeError(
                        "resume_failed",
                        error.toString()
                );
//                callbackContext.error(descr);
            }
        });
    }

    private void play(final String clientId,
                      final String accessToken,
                      final String trackUri,
                      final int fromPosition) {
        SpotifyPlayer player = this.mPlayer;

        if (player == null) {
            this.initAndPlay(
                    clientId,
                    accessToken,
                    trackUri,
                    fromPosition
            );
        } else if (!Objects.equals(clientId, this.currentClientId)) {
            this.logout(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.initAndPlay(
                            clientId,
                            accessToken,
                            trackUri,
                            fromPosition
                    );
                }
            });
        } else if (!Objects.equals(accessToken, this.currentAccessToken)) {
            this.logout(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.loginAndPlay(
                            accessToken,
                            trackUri,
                            fromPosition
                    );
                }
            });
        } else {
            this.doPlay(trackUri, fromPosition);
        }
    }

    private void initAndPlay(
            final String clientId,
            final String accessToken,
            final String trackUri,
            final int fromPosition
    ) {
        Config playerConfig = new Config(
                getApplicationContext(),
                null,
                clientId
        );

        Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                mPlayer = spotifyPlayer;
                PlayerSingleton.getInstance(getApplicationContext()).setPlayer(spotifyPlayer);
                mPlayer.setConnectivityStatus(mOperationCallback, getNetworkConnectivity(MainActivity.this));
                MainActivity.this.loginAndPlay(accessToken, trackUri, fromPosition);
//                testEmitter();
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Player init failure.", throwable);

                MainActivity.this.currentClientId = null;
                JSONObject descr = MainActivity.this.makeError(
                        "player_init_failed",
                        throwable.getMessage()
                );
            }
        });
    }

    /**
     * Registering for connectivity changes in Android does not actually deliver them to
     * us in the delivered intent.
     *
     * @param context Android context
     * @return Connectivity state to be passed to the SDK
     */
    private Connectivity getNetworkConnectivity(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            return Connectivity.OFFLINE;
        }
    }

    private void loginAndPlay(
            final String accessToken,
            final String trackUri,
            final int fromPosition
    ) {
        final SpotifyPlayer player = this.mPlayer;
        if (player == null) {
            Log.wtf(TAG, "SpotifyPlayer instance was null in loginAndPlay.");

            JSONObject descr = this.makeError(
                    "unknown",
                    "Received null as SpotifyPlayer in login method."
            );
            return;
        }

        player.addConnectionStateCallback(this.connectionEventsHandler);
        player.addNotificationCallback(this.playerEventsHandler);

        this.connectionEventsHandler.onLoggedIn(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                MainActivity.this.currentAccessToken = accessToken;

                MainActivity.this.doPlay(
                        trackUri,
                        fromPosition
                );
//                testEmitter();
            }

            @Override
            public void onError(Error error) {
                Log.e(TAG, "Login failure: " + error.toString());

                MainActivity.this.currentAccessToken = null;
                JSONObject descr = MainActivity.this.makeError(
                        "login_failed",
                        error.toString()
                );
            }
        });

        player.login(accessToken);
    }

    private void doPlay(
            final String trackUri,
            final int fromPosition) {

        final SpotifyPlayer player = this.mPlayer;
        if (player == null) {
            Log.wtf(TAG, "SpotifyPlayer instance was null in doPlay.");

            JSONObject descr = this.makeError(
                    "unknown",
                    "Received null as SpotifyPlayer in play method."
            );
            return;
        }

        player.playUri(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
//                callbackContext.success();
            }

            @Override
            public void onError(Error error) {
                Log.e(TAG, "Playback failure: " + error.toString());

                JSONObject descr = MainActivity.this.makeError(
                        "playback_failed",
                        error.toString()
                );
//                callbackContext.error(descr);
            }
        }, trackUri, 0, fromPosition);
    }

    private void logout(final Runnable callback) {
        final SpotifyPlayer player = this.mPlayer;
        if (player == null) {
            callback.run();
            return;
        }

        Runnable cb = new Runnable() {
            @Override
            public void run() {
                player.removeConnectionStateCallback(MainActivity.this.connectionEventsHandler);
                player.removeNotificationCallback(MainActivity.this.playerEventsHandler);

                callback.run();
            }
        };

        if (player.isLoggedIn()) {
            this.connectionEventsHandler.onLoggedOut(cb);
            player.logout();
        } else {
            cb.run();
        }
    }

    private JSONObject makeError(String type, String msg) {
        try {
            final JSONObject obj = new JSONObject();
            obj.put("type", type);
            obj.put("msg", msg);
            return obj;
        } catch (JSONException e) {
            Log.wtf(TAG, "Got a JSONException during error creation.", e);
            return null;
        }
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


    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
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

    @Override
    public void onSelected(Track item, int position) {
//        play(CLIENT_ID, currentAccessToken, item.uri, 0);
        mPresenter.removeQueueItem(position);

        String uri = item.uri;
        String[] parts = uri.split(":");
        final String id = parts[parts.length - 1];

        mPresenter.addPlaying(id);

//        makePlayerRequest("play", item);
    }

    @Override
    public void onMediaAction(View v) {
        switch (v.getId()){
            case R.id.play_button:
                makePlayerRequest("play");
                break;
            case R.id.pause_button:
                makePlayerRequest("pause");
                break;
        }
    }

    public void makePlayerRequest(String action){


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, baseURL + "/party/player/"+action+"?id="+currentParty.getID(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Main", "Response is: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.wtf("Error", error.toString());
                        if (error.networkResponse.statusCode == 400) {
                            Toast.makeText(getApplicationContext(), "Party not found", Toast.LENGTH_SHORT).show();
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

    public PartySocket(URI serverUri, Draft draft, Map<String, String> httpHeaders, int connectTimeout) {
        super(serverUri, draft, httpHeaders, connectTimeout);
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
