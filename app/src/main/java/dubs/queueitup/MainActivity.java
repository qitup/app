package dubs.queueitup;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Base64;
import android.util.Log;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.notification.AHNotification;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements PartyPage.OnCreatePartyButtonListener{

    private final int[] colors = {R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent};

    private NoSwiperPager viewPager;
    private AHBottomNavigation bottomNavigation;
    private BottomBarAdapter pagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_AUTO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setupViewPager();


//        final DummyFragment fragment = new DummyFragment();
//        Bundle bundle = new Bundle();
//        bundle.putInt("color", ContextCompat.getColor(this, colors[0]));
//        fragment.setArguments(bundle);

//        getSupportFragmentManager()
//                .beginTransaction()
//                .add(R.id.frame, fragment, DummyFragment.TAG)
//                .commit();

        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        setupBottomNavBehaviors();
        setupBottomNavStyle();


        addBottomNavigationItems();
        bottomNavigation.setCurrentItem(0);


        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
//                fragment.updateColor(ContextCompat.getColor(MainActivity.this, colors[position]));

                if (!wasSelected)
                    viewPager.setCurrentItem(position);

                // remove notification badge
                int lastItemPos = bottomNavigation.getItemsCount() - 1;
                if (position == lastItemPos)
                    bottomNavigation.setNotification(new AHNotification(), lastItemPos);

                return true;
            }
        });

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

//    private static final String HOST_EMULATOR = "10.0.2.2:8081";
//    private static final String CLIENT_ID = "SPOTIFY_ID";
//    private static final String REDIRECT_URI = "queueitup-login://callback";
//
//    private Player mPlayer;
//    private SpotifyApi api;
//    private SpotifyService spotify;
//    ViewPager simpleViewPager;
//    TabLayout tabLayout;
//    PagerAdapter adapter;
//    private FragmentManager fm;
//    public static Bundle myBundle = new Bundle();
//    Button button;
//    private String auth_token;
//    private WebView mWebview;
//    private String partyPassword;
//    private String baseURL = BuildConfig.scheme + "://" + getHost();
//    private RequestQueue requestQueue;
//    java.net.CookieManager systemCookies;
//
//
//    // Request code that will be used to verify if the result comes from correct activity
//    // Can be any integer
//    private static final int REQUEST_CODE = 1337;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_main);
//
//        // get the reference of ViewPager and TabLayout
//        simpleViewPager = (ViewPager) findViewById(R.id.simpleViewPager);
//        adapter = new PagerAdapter(getSupportFragmentManager());
//
//        fm = getSupportFragmentManager();
//
//        // Add Fragments to adapter one by one
//        adapter.addFragment(new PartyPage(), "Party");
//        adapter.addFragment(new QueuePage(), "Queue");
//        adapter.addFragment(new SearchPage(), "Search");
//
//        simpleViewPager.setAdapter(adapter);
//
//        tabLayout = (TabLayout) findViewById(R.id.simpleTabLayout);
//        tabLayout.setupWithViewPager(simpleViewPager);
//
//
////        CookieManager cookieManager = CookieManager.getInstance();
////
////        mWebview = new WebView(this);
////
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
////            cookieManager.setAcceptThirdPartyCookies(mWebview, true);
////        } else {
////            cookieManager.setAcceptCookie(true);
////            cookieManager.setAcceptThirdPartyCookies(mWebview, true);
////        }
////
////        final WebSettings settings = mWebview.getSettings();
////        settings.setAppCacheEnabled(true);
////        settings.setBuiltInZoomControls(true);
////
////        systemCookies = new java.net.CookieManager(null, CookiePolicy.ACCEPT_ALL);
////        CookieHandler.setDefault(systemCookies);
////
////        requestQueue = Volley.newRequestQueue(this);
////
////        String url = baseURL + "/auth/spotify";
////
////        mWebview.setWebViewClient(new WebViewClient() {
////            @Override
////            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
////                String request_url = request.getUrl().toString();
////
////                if (request_url.startsWith(baseURL + "/auth/spotify/callback")) {
////                    String cookie = CookieManager.getInstance().getCookie(request.getUrl().getHost());
////
////                    if (cookie != null) {
////                        String[] parts = cookie.split("=");
////                        systemCookies.getCookieStore().add(URI.create(baseURL), new HttpCookie(parts[0], parts[1]));
////                    }
////                    MainActivity.this.finishAuthentication(request_url);
////
////                    mWebview.clearCache(true);
////
////                    mWebview.onPause();
////                    mWebview.removeAllViews();
////                    mWebview.destroyDrawingCache();
////
////                    // NOTE: This pauses JavaScript execution for ALL WebViews,
////                    // do not use if you have other WebViews still alive.
////                    // If you create another WebView after calling this,
////                    // make sure to call mWebView.resumeTimers().
////                    mWebview.pauseTimers();
////
////                    mWebview.destroy();
////                    mWebview = null;
////                    return true;
////                } else {
////                    return false;
////                }
////            }
////
////            @Override
////            public void onPageStarted(WebView view, String url, Bitmap favicon) {
////                super.onPageStarted(view, url, favicon);
//////                loadingFinished = false;
////                //SHOW LOADING IF IT ISNT ALREADY VISIBLE
////            }
////
////            @Override
////            public void onPageFinished(WebView view, String url) {
//////                if(!redirect){
//////                    loadingFinished = true;
//////                }
//////
//////                if(loadingFinished && !redirect){
//////                    //HIDE LOADING IT HAS FINISHED
//////                } else{
//////                    redirect = false;
//////                }
////                super.onPageFinished(view, url);
////            }
////        });
////        mWebview.loadUrl(url);
////        setContentView(mWebview);
//
//    }
//
//    private void finishAuthentication(String exchange_url) {
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, exchange_url, null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        // Display the first 500 characters of the response string.
//                        try {
//                            auth_token = response.getString("token");
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        Log.d("MainActivity", "Response is: " + response.toString());
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e("Error", "That didn't work!" + error.toString());
//                    }
//                });
//
//        requestQueue.add(request);
//    }
//

//
//    @Override
//    public void onSubmitParty(String code){
//        EditText party_code = (EditText) findViewById(R.id.party_code);
//        Log.d("PartySetup", party_code.getText().toString());
////        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, baseURL + "/create/party", null,
////                new Response.Listener<JSONObject>() {
////                    @Override
////                    public void onResponse(JSONObject response) {
////                        // Display the first 500 characters of the response string.
////                        Log.d("MainActivity", "Response is: " + response.toString());
////                    }
////                },
////                new Response.ErrorListener() {
////                    @Override
////                    public void onErrorResponse(VolleyError error) {
////                        Log.e("Error", "That didn't work!" + error.toString());
////                    }
////                });
////
////        requestQueue.add(request);
//
//
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (getFragmentManager().getBackStackEntryCount() > 0) {
//            getFragmentManager().popBackStack();
//        } else {
//            super.onBackPressed();
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        super.onActivityResult(requestCode, resultCode, intent);
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        Spotify.destroyPlayer(this);
//        super.onDestroy();
//    }
//
//    @Override
//    public void onPlaybackEvent(PlayerEvent playerEvent) {
//        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
//
////
////        switch (playerEvent) {
////            case kSpPlaybackNotifyMetadataChanged:
////                Log.d("MainActivity", mPlayer.getMetadata().currentTrack.toString());
////                Log.d("MainActivity", auth_token);
////
////                spotify.getAlbum("7xl50xr9NDkd3i2kBbzsNZ", new Callback<Album>() {
////                    @Override
////                    public void success(Album album, Response response) {
////                        Log.d("Album success", album.name);
////                    }
////
////                    @Override
////                    public void failure(RetrofitError error) {
////                        Log.d("Album failure", error.toString());
////                    }
////                });
////
////
////                break;
////            // Handle event type as necessary
////            default:
////                break;
////        }
//    }
//
//    @Override
//    public void onPlaybackError(Error error) {
//        Log.d("MainActivity", "Playback error received: " + error.name());
//        switch (error) {
//            // Handle error type as necessary
//            default:
//                break;
//        }
//    }
//
//    @Override
//    public void onLoggedIn() {
//        Log.d("MainActivity", "User logged in");
//
////        mPlayer.playUri(null, "spotify:track:5SiEPQmziTAi5DHNhB3Wz5", 0, 0);
//    }
//
//    @Override
//    public void onLoggedOut() {
//        Log.d("MainActivity", "User logged out");
//    }
//
//    @Override
//    public void onLoginFailed(Error error) {
//
//    }
//
//    @Override
//    public void onTemporaryError() {
//        Log.d("MainActivity", "Temporary error occurred");
//    }
//
//    @Override
//    public void onConnectionMessage(String message) {
//        Log.d("MainActivity", "Received connection message: " + message);
//    }
//
//    public static String getHost() {
//        return (Build.PRODUCT).contains("sdk") ? HOST_EMULATOR : BuildConfig.HOST;
//    }


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
