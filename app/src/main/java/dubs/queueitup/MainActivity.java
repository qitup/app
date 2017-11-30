package dubs.queueitup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.telecom.Call;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.gson.Gson;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dubs.queueitup.Models.Party;
import dubs.queueitup.Models.QItem;
import dubs.queueitup.Models.Queue;
import dubs.queueitup.Models.TrackItem;
import dubs.queueitup.Models.User;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Track;

public class MainActivity extends AppCompatActivity implements PartyPage.OnCreatePartyButtonListener, SearchPage.OnTrackItemSelected, SpotifyPlayer.NotificationCallback, QueuePage.OnMediaPlayerAction, PartyDetailsPage.leaveParty {
    private static final String TAG = "MainActivity";
    private NoSwiperPager viewPager;
    private static final String PREFS_NAME = "QITUP";
    private AHBottomNavigation bottomNavigation;
    private BottomBarAdapter pagerAdapter;
    private static final String HOST_EMULATOR = "10.0.2.2:8081";
    private static final String CLIENT_ID = BuildConfig.clientID;
    private static final String REDIRECT_URI = "queueitup-login://callback";
    private static String baseURL = BuildConfig.scheme + "://" + getHost();
    private static final int REQUEST_CODE_LOGIN_CREATE = 1336;
    private static final int REQUEST_CODE_LOGIN = 1337;
    private static final int REQUEST_CODE_CREATE = 1338;
    private static final int REQUEST_CODE_JOIN = 1339;
    private SpotifyPlayer mPlayer;
    private PartySocket partySocket = null;
    private QueuePresenter mPresenter = null;
    private Intent intent;
    private JSONObject jwt_token;
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
        startActivityForResult(intent, REQUEST_CODE_LOGIN);

//        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
//        String auth_string = settings.getString("jwt_token", null);
//        if(auth_string != null){
//            try {
//                jwt_token =  new JSONObject(auth_string);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            if(tokenExpired()){
//                refreshToken();
//                SharedPreferences.Editor editor = settings.edit();
//                editor.putString("jwt_token",jwt_token.toString());
//                editor.commit();
//            }
//            try {
//                RequestSingleton.setJWT_token(jwt_token.getString("access_token"));
//                RequestSingleton.setSpotify_auth_token(jwt_token.getString("access_token"));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        } else {
//            Intent intent = new Intent(this, LoginActivity.class);
//            startActivityForResult(intent, REQUEST_CODE_LOGIN);
//        }

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

    public boolean tokenExpired(){
        long expiry = 0;
        try {
            expiry = jwt_token.getInt("exp") * 1000;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return System.currentTimeMillis() > expiry;
    }

    public void refreshToken(){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, baseURL + "/refresh", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Main", "Response is: " + response.toString());
                        jwt_token = response;
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

    @Override
    protected void onPause(){
        super.onPause();

        unregisterReceiver(mNetworkStateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set up the broadcast receiver for network events. Note that we also unregister
        // this receiver again in onPause()
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


    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == REQUEST_CODE_LOGIN) {
                if (resultCode == RESULT_OK) {
                    RequestSingleton.setJWT_token(data.getStringExtra("jwt_token"));
                    getSpotifyToken();
                }
            } else if (requestCode == REQUEST_CODE_CREATE) {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Successfully created party", Toast.LENGTH_SHORT).show();

                    Bundle buns = data.getExtras();
                    currentParty = (Party) buns.getParcelable("party_details");

                    Bundle args = createFragmentBundle();
                    args.putParcelable("party", currentParty);

                    try {
                        pagerAdapter.swapFragmentAt(createFragment(3, args), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    viewPager.getAdapter().notifyDataSetChanged();

                    mPresenter = ((QueuePage) pagerAdapter.getItem(1)).getPresenter();
                    ((QueuePage) pagerAdapter.getItem(1)).mediaButton.setEnabled(true);
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

                    Bundle args = createFragmentBundle();
                    args.putParcelable("party", currentParty);

                    refreshQueue();

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

    public void refreshQueue(){
        Queue queue = currentParty.getQueue();
        List<TrackItem> items = queue.getQueue_items();
        for (int i = 0; i < items.size(); i++){
            if(items.get(i).isPlaying()){
                mPresenter.addPlaying(items.get(i));
            }
        }
        mPresenter.clearData();
        mPresenter.addQueueItem(items);
    }

    public void getSpotifyToken(){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, baseURL + "/spotify/token", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Main", "Response is: " + response.toString());

                        try {
                            RequestSingleton.setSpotify_auth_token(response.getString("access_token"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                        case "queue.change":
                            JSONObject queue;
                            final JSONArray tracks;

                            try {
                                queue = response.getJSONObject("queue");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return;
                            }
                            tracks = queue.getJSONArray("items");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateQueue(tracks);
                                    Toast.makeText(getApplicationContext(), "Queue Updated", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        case "attendees.change":
                            JSONArray res;

                            try {
                                res = response.getJSONArray("attendees");
                            } catch (JSONException e){
                                e.printStackTrace();
                                return;
                            }
                            updateAttendees(res);
                            break;
                        case "host.promotion":
                            final JSONObject host;

                            try {
                                host = response.getJSONObject("host");
                            } catch (JSONException e){
                                e.printStackTrace();
                                return;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    notifyAndTransfer(host);
                                    Toast.makeText(getApplicationContext(), "You are now the host of the party!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        case "player.interrupted":
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Toast.makeText(getApplicationContext(), "Player interruption", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        case "player.play":
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((QueuePage) pagerAdapter.getItem(1)).mediaButton.setImageResource(R.drawable.pause);
                                    PlayerSingleton.getInstance(getApplicationContext()).setPlaying(1);
                                    Toast.makeText(getApplicationContext(), "Queue Played", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        case "player.pause":
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((QueuePage) pagerAdapter.getItem(1)).mediaButton.setImageResource(R.drawable.play_button);
                                    PlayerSingleton.getInstance(getApplicationContext()).setPlaying(0);
                                    Toast.makeText(getApplicationContext(), "Player paused", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void notifyAndTransfer(JSONObject host){
        try {
            currentParty.setHost(new User(host.getString("id"), host.getString("name"), host.get("avatar_url").toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        };
    }

    public void updateQueue(JSONArray tracks){
        Queue queue = currentParty.getQueue();

        List<TrackItem> tItems = queue.getQueue_items();
        TrackItem nowPlaying = null;
        for (int i = 0; i < tracks.length(); i++){
            try {
                if(i < tItems.size()){
                    if((tItems.get(i)).getUri() != tracks.getJSONObject(i).get("uri")){
                        tItems.remove(i);
                        i--;
                    }
                } else {
                    TrackItem track = new TrackItem(
                        tracks.getJSONObject(i).get("type").toString(),
                        tracks.getJSONObject(i).get("added_by").toString(),
                        tracks.getJSONObject(i).get("added_at").toString(),
                        tracks.getJSONObject(i).getJSONObject("state").getBoolean("playing"),
                        tracks.getJSONObject(i).get("uri").toString());
                    tItems.add(i, track);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        currentParty.getQueue().setQueue_items(tItems);
        refreshQueue();
    }

    public void updateAttendees(JSONArray users){
        List<User> attendees = new ArrayList<>();
        for (int i = 0; i < users.length(); i++){
            JSONObject guest = null;
            try {

                guest = users.getJSONObject(i).getJSONObject("user");
                User user = new User(guest.getString("id"), guest.get("name").toString(), guest.get("avatar_url").toString());
                attendees.add(i, user);
                Log.d("Attendee Change", guest.get("name").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        currentParty.setAttendees(attendees);
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
        return bundle;
    }

    // Fragment interface overrides

    @Override
    public void onCreateParty(View v) {
        switch (v.getId()) {
            case R.id.createPartyButton:
                if(canUserHost()){
                    Log.d("MainActivity", "Create party button clicked");
                    Intent intent = new Intent(this, CreateParty.class);
                    startActivityForResult(intent, REQUEST_CODE_CREATE);
                } else {
                    Intent intent = new Intent(this, SpotifyLoginActivity.class);
                    intent.putExtra("jwt_token", RequestSingleton.getJWT_token());

                    startActivityForResult(intent, REQUEST_CODE_LOGIN);
                }

                break;
            case R.id.joinPartyButton:
                Log.d("MainActivity", "Join party button clicked");
//                Toast.makeText(this, "Joining party", Toast.LENGTH_SHORT).show();
                intent = new Intent(this, JoinParty.class);
                startActivityForResult(intent, REQUEST_CODE_JOIN);
                break;
        }

    }

    public boolean canUserHost(){
        String token = RequestSingleton.getJWT_token();
        JSONObject claim = null;
        boolean canHost = false;
        try {
            claim = new JSONObject(JWTUtils.decoded(token));
            canHost = claim.getBoolean("can_host");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return canHost;
    }

    public boolean isUserHost(){
        String token = RequestSingleton.getJWT_token();
        JSONObject claim = null;
        String userID = null;
        try {
            claim = new JSONObject(JWTUtils.decoded(token));
            userID = claim.getString("sub");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert userID != null;
        return userID.equals(currentParty.getHost().getId());
    }

    @Override
    public void addTrack(Track track) {
        JSONObject message = new JSONObject();
        JSONObject queue_item = new JSONObject();

        String type = "spotify_track";
        String uri = track.uri;

        try {
            message.put("type", "queue.push");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            queue_item.put("type", type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            queue_item.put("uri", uri);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            message.put("item", queue_item);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        partySocket.send(message.toString());
        Log.d("MainActivity", uri);
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

    public void setNowPlaying(TrackItem item) {

        mPresenter.addPlaying(item);
    }

    @Override
    public void onMediaAction(View v) {
        if (isUserHost() && currentParty != null) {
            if (PlayerSingleton.getInstance(this).isEmpty()) {
                makePlayerRequest("play");
                ((ImageButton)v).setImageResource(R.drawable.pause);
                PlayerSingleton.getInstance(this).setPlaying(1);
//                setNowPlaying(0);
            } else {
                if (PlayerSingleton.getInstance(this).isPlaying() == 1) {
                    makePlayerRequest("pause");
                    PlayerSingleton.getInstance(this).setPlaying(0);
                    ((ImageButton)v).setImageResource(R.drawable.play_button);
                } else {
                    makePlayerRequest("play");
                    PlayerSingleton.getInstance(this).setPlaying(1);
                    ((ImageButton)v).setImageResource(R.drawable.pause);
                }
            }
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

    public void leaveRequest(String transferTo){
        String requestURL = baseURL + "/party/leave/?id=" + currentParty.getID();
        if(transferTo != null){
            requestURL = requestURL + "&transfer_to="+transferTo;
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Main", "Response is: " + response.toString());

                        try {
                            pagerAdapter.swapFragmentAt(createFragment(0, null), 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        viewPager.getAdapter().notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.wtf("Error", error.toString());
                        if (error.networkResponse.statusCode == 400) {
                            Toast.makeText(getApplicationContext(), "Host Error", Toast.LENGTH_SHORT).show();
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

    @Override
    public void leaveParty(View v) {

        String token = RequestSingleton.getJWT_token();
        JSONObject claim = null;
        String user_id = null;
        try {
            claim = new JSONObject(JWTUtils.decoded(token));
            user_id = claim.getString("sub");
        } catch (Exception e) {
            e.printStackTrace();
        }

        final List<User> users = currentParty.getAttendees();

        if(!user_id.equals(currentParty.getHost().getId()) || users.size() == 0) {
            leaveRequest(null);
        } else {
            List<String> names = new ArrayList<>();
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
            builderSingle.setIcon(R.drawable.ic_reorder_white_48dp);
            builderSingle.setTitle("Transfer Host Permissions:");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);

            for (int i=0; i < users.size(); i++){
                User user = users.get(i);
                arrayAdapter.add(user.getName());
            }

            builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, final int which) {
                    final String strName = arrayAdapter.getItem(which);
                    AlertDialog.Builder builderInner = new AlertDialog.Builder(MainActivity.this);
                    builderInner.setMessage(strName);
                    builderInner.setTitle("Transfer permissions to this user?");
                    builderInner.setPositiveButton("Transfer", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int which2) {
                            leaveRequest(users.get(which).getId());
                            dialog.dismiss();
                        }
                    });
                    builderInner.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int which) {
                            dialog.dismiss();
                        }
                    });
                    builderInner.show();
                }
            });
            builderSingle.show();
        }
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
