package dubs.queueitup;

import android.app.ActionBar;
import android.app.FragmentTransaction;
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
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spotify.sdk.android.authentication.*;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

import static com.spotify.sdk.android.player.PlayerEvent.*;

public class MainActivity extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback
{



    private static final String CLIENT_ID = "SPOTIFY_ID";
    private static final String REDIRECT_URI = "queueitup-login://callback";

    private Player mPlayer;
    private SpotifyApi api;
    private SpotifyService spotify;
    ViewPager simpleViewPager;
    TabLayout tabLayout;


    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MainActivity", "HostName:"+(BuildConfig.host_name));

        setContentView(R.layout.activity_main);

        // get the reference of ViewPager and TabLayout
        simpleViewPager = (ViewPager) findViewById(R.id.simpleViewPager);
        tabLayout = (TabLayout) findViewById(R.id.simpleTabLayout);

        TabLayout.Tab firstTab = tabLayout.newTab();
        firstTab.setText("Party"); // set the Text for the first Tab
        firstTab.setIcon(R.drawable.ic_search_white_48dp); // set an icon for the tab

        tabLayout.addTab(firstTab); // add  the tab at in the TabLayout

        TabLayout.Tab secondTab = tabLayout.newTab();
        secondTab.setText("Queue"); // set the Text for the second Tab
        secondTab.setIcon(R.drawable.ic_reorder_white_48dp); // set an icon for the second tab
        tabLayout.addTab(secondTab); // add  the tab  in the TabLayout

        TabLayout.Tab thirdTab = tabLayout.newTab();
        thirdTab.setText("Search"); // set the Text for the first Tab
        thirdTab.setIcon(R.drawable.ic_search_white_48dp); // set an icon for the first tab
        tabLayout.addTab(thirdTab); // add  the tab at in the TabLayout

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        simpleViewPager.setAdapter(adapter);
        // addOnPageChangeListener event change the tab on slide
        simpleViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));


        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = BuildConfig.scheme+BuildConfig.host_name;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("MainActivity", "Response is: "+ response.substring(0,500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", "That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

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


}
