package dubs.queueitup;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.notification.AHNotification;
import com.spotify.sdk.android.player.Player;

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
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements PartyPage.OnCreatePartyButtonListener, SearchPage.searchTextEntered {

    private NoSwiperPager viewPager;
    private AHBottomNavigation bottomNavigation;
    private BottomBarAdapter pagerAdapter;
    private static final String HOST_EMULATOR = "10.0.2.2:8081";
    private static final String CLIENT_ID = "SPOTIFY_ID";
    private static final String REDIRECT_URI = "queueitup-login://callback";
    private static final int REQUEST_CODE = 1337;


    private Player mPlayer;
    private SpotifyApi api;
    private PartySocket partySocket = null;
    private SpotifyService spotify;
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
        startActivityForResult(intent, REQUEST_CODE);

//        sharedPref = getPreferences(Context.MODE_PRIVATE);
//        auth_token = sharedPref.getString("auth_token", null);
//
//        if(auth_token == null){
//            Intent intent = new Intent(this, LoginActivity.class);
//            startActivityForResult(intent, REQUEST_CODE);
//        } else {
//            Log.d("Main", auth_token);
//        }



        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
//                fragment.updateColor(ContextCompat.getColor(MainActivity.this, colors[position]));

                if (!wasSelected)
                    viewPager.setCurrentItem(position);

                // remove notification badge
                int lastItemPos = bottomNavigation.getItemsCount() - 1;
                if (notificationVisible && position == lastItemPos)
                    bottomNavigation.setNotification(new AHNotification(), lastItemPos);

                return true;
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        android.app.FragmentManager fm = getFragmentManager();
//        Fragment frag;
//        android.app.FragmentTransaction ft = fm.beginTransaction();
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (data.getIntExtra("result_code", -1) == 1337) {
                if (resultCode == RESULT_OK) {
                    RequestSingleton.setJWT_token(data.getStringExtra("auth_token"));
                    RequestSingleton.setSpotify_auth_token(getAuthToken());
                    //                SharedPreferences.Editor editor = sharedPref.edit();
                    //                editor.putString("auth_token", data.getStringExtra("auth_token"));
                    //                editor.apply();
                }
            } else if (data.getIntExtra("result_code", -1) == 1338) {
                if (resultCode == RESULT_OK) {
                    Log.d("Mainactivity", "WOOHOO");
                    Toast.makeText(this, "Successfully created party", Toast.LENGTH_SHORT).show();
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + RequestSingleton.getJWT_token());
                    try {
                        partySocket = new PartySocket(new URI(data.getStringExtra("socket_url")), new Draft_6455(), params, 30);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    partySocket.connect();
                }
            } else if (data.getIntExtra("result_code", -1) == 1339) {
                if (resultCode == RESULT_OK) {
                    Log.d("Mainactivity", "WOOHOO2");
                    Toast.makeText(this, "Successfully joined party", Toast.LENGTH_SHORT).show();
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + RequestSingleton.getJWT_token());
                    try {
                        partySocket = new PartySocket(new URI(data.getStringExtra("socket_url")), new Draft_6455(), params, 30);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    partySocket.connect();
                }
            }
        }
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
    public void onCreateParty(View v){
        switch (v.getId()){
            case R.id.createPartyButton:
                Log.d("MainActivity", "Create party button clicked");
                intent = new Intent(this, CreateParty.class);
                startActivityForResult(intent, 1338);
                break;
            case R.id.joinPartyButton:
                Log.d("MainActivity", "Join party button clicked");
                intent = new Intent(this, JoinParty.class);
                startActivityForResult(intent, 1339);
                break;
        }

    }

    @Override
    public void searchSpotify(String search_string){
        SpotifyApi api = new SpotifyApi();

        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, 0);
        options.put(SpotifyService.LIMIT, 20);

        api.setAccessToken(getAuthToken());

        SpotifyService spotify = api.getService();

//        spotify.searchTracks(search_string, options, new SpotifyCallback<TracksPager>() {
//            @Override
//            public void success(TracksPager tracksPager, Response response) {
//                listener.onComplete(tracksPager.tracks.items);
//            }
//
//            @Override
//            public void failure(SpotifyError error) {
//                listener.onError(error);
//            }
//        });
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


    public String getAuthToken(){
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

    private static String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}

class PartySocket extends WebSocketClient {

    public PartySocket(URI serverUri , Draft draft,  Map<String,String> httpHeaders, int connectTimeout) {
        super( serverUri, draft, httpHeaders, connectTimeout);
    }

    public PartySocket( URI serverURI ) {
        super( serverURI );
    }

    @Override
    public void onOpen( ServerHandshake handshakedata ) {
        System.out.println( "opened connection" );
        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }

    @Override
    public void onMessage( String message ) {
        Log.d("MainActivity" , message );
    }

    @Override
    public void onClose( int code, String reason, boolean remote ) {
        // The codecodes are documented in class org.java_websocket.framing.CloseFrame
        System.out.println( "Connection closed by " + ( remote ? "remote peer" : "us" ) + " Code: " + code + " Reason: " + reason );
    }

    @Override
    public void onError( Exception ex ) {
        ex.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }


}
