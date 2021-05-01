package com.projectx.eyemusic;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.projectx.eyemusic.VolleyRequests.PlaylistRequest;
import com.projectx.eyemusic.graphics.DotGraphic;
import com.projectx.eyemusic.graphics.GraphicOverlay;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.*;

import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private String CLIENT_ID;
    private String CLIENT_SECRET;
    private String REDIRECT_URI;
    private String SPOTIFY_PACKAGE_NAME;
    private String APP_PACKAGE_NAME;
    private String PLAY_STORE_URI;
    private String REFERRER;
    private static final int SPOTIFY_TOKEN_REQUEST_CODE = 777;
    public static final int SPOTIFY_AUTH_CODE_REQUEST_CODE = 0x11;
    private final String TAG = MainActivity.class.getName();
    public static SpotifyAppRemote mSpotifyAppRemote;
    private RequestQueue requestQueue;
    private String mAccessToken;
    private String mAccessCode;

    SharedPreferences preferences = null;

    // Create an array of playlists
    ArrayList<Playlist> playlists;

    // Views
    Button btn_play, btn_pause, btn_goto_eye, btn_startGazeCaptureThread;
    TextView tv_message;
    TextView tv_artist, tv_auth_token;
    RecyclerView rv_main_playlists;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter rv_adapter;
    ProgressBar pb_main;
    boolean flag = false;
    PackageManager packageManager;

    private Authentication authentication;

    private static GraphicOverlay graphicOverlayGazeLocation;
    static int[] graphicOverlayGazeLocationLocation = new int[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);
        {
            CLIENT_ID = getString(R.string.CLIENT_ID);
            CLIENT_SECRET = getString(R.string.CLIENT_SECRET);
            REDIRECT_URI = getString(R.string.REDIRECT_URI);
            SPOTIFY_PACKAGE_NAME = getString(R.string.SPOTIFY_PACKAGE_NAME);
            APP_PACKAGE_NAME = getString(R.string.APP_PACKAGE_NAME);
            PLAY_STORE_URI = getString(R.string.PLAY_STORE_URI);
            REFERRER = getString(R.string.REFERRER);
        }
        pb_main = findViewById(R.id.pb_main);
        btn_goto_eye = findViewById(R.id.btn_main_goto_eye);
        btn_goto_eye.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, OpencvActivity.class);
            startActivity(intent);
        });

        btn_startGazeCaptureThread = findViewById(R.id.btn_main_startGazeCaptureThread);
        btn_startGazeCaptureThread.setOnClickListener(view -> {
            GazeRunnable gaze_runnable = new GazeRunnable(findViewById(R.id.graphic_overlay_gaze_location), this);
            new Thread(gaze_runnable).start();
        });
        //

        Activity mActivity = this;
        findViewById(R.id.btn_main_stimulateTouch).setOnClickListener(view -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        SimulatedTouch.click(mActivity,500, 500 );
                        //SimulatedTouch.swap(100 ,100,1000 ,500, 5);
                    } catch ( Exception e) {
                        Log.e(TAG, "When pressed simulatedTouch btn: ", e);
                    }
                }
            }).start();

        });

        // setup recycler view
        rv_main_playlists = findViewById(R.id.rv_main_playlists);
        rv_main_playlists.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        rv_main_playlists.setLayoutManager(layoutManager);

        packageManager = getPackageManager();

        // use sharedPreferences to determine first time launch
        preferences = getSharedPreferences(APP_PACKAGE_NAME, MODE_PRIVATE);

        // create  singleton request queue
        requestQueue  = Volley.newRequestQueue(MainActivity.this);

        // create Authentication Object
        authentication = new Authentication(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI, preferences, requestQueue, this);

        // TODO Improve UX flow of fetched playlists

        // fragment
//        getFragmentManager().beginTransaction().add(R.id.fragment_camera_preview, new CameraExtractionFragment()).commit();


        graphicOverlayGazeLocation = findViewById(R.id.graphic_overlay_gaze_location);
        if (graphicOverlayGazeLocation == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        graphicOverlayGazeLocation.add(new DotGraphic(this, graphicOverlayGazeLocation, 500, 500));

        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, new CameraExtractionFragment(), null)
                    .commit();
        }*/


    }

    public GraphicOverlay getGraphicOverlayGazeLocation(){
        return graphicOverlayGazeLocation;
    }
    public int[] getGraphicOverlayGazeLocationLocation() {
        return graphicOverlayGazeLocationLocation;
    }

    @Override
    public void onWindowFocusChanged (boolean hasFocus) {
        graphicOverlayGazeLocation.getLocationOnScreen(graphicOverlayGazeLocationLocation);
        Log.i(TAG, "onWindowFocusChanged:Location of overlay " + graphicOverlayGazeLocationLocation[0] + " "+ graphicOverlayGazeLocationLocation[1]);
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);
        // error occurred
        if (response.getError() != null && response.getError().isEmpty()){
            tv_auth_token.setText(response.getError());
            Log.e(TAG, response.getError());
        }
        if (requestCode == SPOTIFY_TOKEN_REQUEST_CODE){
            mAccessToken = response.getAccessToken();
            if (mAccessToken != null) {
                // store the access token
                authentication.storeToken(mAccessToken);
                showProgressBar(true);
            }
        } else if(requestCode == SPOTIFY_AUTH_CODE_REQUEST_CODE){
            mAccessCode = response.getCode();

            if (mAccessCode != null) {
                // store Access code
                authentication.storeAccessCode(mAccessCode);
                // fetch access token and refresh token and store them
                authentication.fetchTokens();
                // call on start after successfully authenticating --> only happens on first launch
                onStart();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        // check if EyeMusic is launched for the first time
        if(!isFirstTimeLaunch()){
            // order matters
            mAccessCode = authentication.getAccessCode();
            mAccessToken = authentication.getAccessToken();

            // ------------------- perform error check for when the access is denied but launch is not first time ----------
        }
        // Check if Spotify is installed each time the app is launched. Requirement!
        if(!isSpotifyInstalled(packageManager)){
            directUserToPlayStore();
        } else{
            if(authentication.isAuthenticated()){
                ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();
                // offline support is possible out of the box and doesn't require additional implementation
                // if the following conditions are met:
                // -> Application has successfully connected to Spotify over the last 24 hours
                // -> The Application uses the same REDIRECT_URI, CLIENT_ID and scopes when connecting to
                //    Spotify
                // Use the SpotifyAppRemote.Connector to connect to Spotify and get an instance of
                // SpotifyAppRemote
                //SpotifyAppRemote.disconnect(mSpotifyAppRemote);
                SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        // connection was successful
                        Toast.makeText(getApplicationContext(), "Successfully Connected", Toast.LENGTH_SHORT).show();
                        // refresh access token before fetching playlists if token has expired
                        authentication.refreshAccessToken();
                        fetchPlaylists(requestQueue, mSpotifyAppRemote);
                    }
                    @Override
                    public void onFailure(Throwable throwable) {
                        // handle connection error here
                        Toast.makeText(getApplicationContext(), "Couldn't connect", Toast.LENGTH_LONG).show();
                        Log.e(TAG, throwable.getMessage(), throwable);
                    }
                });
            }
        }
    }

    private void directUserToPlayStore() {
        // Send user to Play Store to install if available in current market
        // TO-DO: If Spotify is not in user's market --> potentially can't make use of our app
        Log.w(TAG, "Spotify isn't installed! Going to play store");
        // Alert Dialog for good UX
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this); // look up context
        builder.setMessage("Please install Spotify from Play Store then launch app again.")
                .setCancelable(true) // may change this
                .setPositiveButton("Install", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try{
                            Uri uri = Uri.parse("market://details")
                                    .buildUpon()
                                    .appendQueryParameter("id", SPOTIFY_PACKAGE_NAME)
                                    .appendQueryParameter("referrer", REFERRER)
                                    .build();
                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        }catch (android.content.ActivityNotFoundException ignored){
                            Uri uri = Uri.parse(PLAY_STORE_URI)
                                    .buildUpon()
                                    .appendQueryParameter("id", SPOTIFY_PACKAGE_NAME)
                                    .appendQueryParameter("referrer", REFERRER)
                                    .build();
                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create().show();
    }

    // has code for using the spotify remote
    private void connected(){
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!flag){
                    mSpotifyAppRemote.getPlayerApi().play("spotify:album:5pF05wJrbrIvqunE41vWP8");

                    mSpotifyAppRemote.getPlayerApi()
                            .subscribeToPlayerState()
                            .setEventCallback(playerState -> {
                                final Track track = playerState.track;
                                if(track != null){
                                    tv_message.setText(track.name);
                                    tv_artist.setText(track.artist.name);
                                }
                            });
                    flag = true;
                } else{
                    mSpotifyAppRemote.getPlayerApi().resume();
                    mSpotifyAppRemote.getPlayerApi()
                            .subscribeToPlayerState()
                            .setEventCallback(playerState -> {
                                final Track track = playerState.track;
                                if(track != null){
                                    tv_message.setText(track.name);
                                    tv_artist.setText(track.artist.name);
                                }
                            });
                }
                btn_play.setEnabled(false);
            }
        });
        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().pause();
                flag = true;
                btn_play.setEnabled(true);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        // disconnect from AppRemote
        // TODO add code for stopping play if playing
        mSpotifyAppRemote.getPlayerApi().pause();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private boolean isSpotifyInstalled(PackageManager packageManager){
        try{
            packageManager.getPackageInfo(SPOTIFY_PACKAGE_NAME,0);
            return true;
        } catch (PackageManager.NameNotFoundException e){
            return false;
        }
    }

    private boolean isFirstTimeLaunch(){
        if(preferences.getBoolean("firstTime", true)){
            // Do authentication once
            authentication.authenticate(MainActivity.this, SPOTIFY_AUTH_CODE_REQUEST_CODE);
            // set first time to false
            preferences.edit().putBoolean("firstTime", false).apply();
            return true;
        } else{
            return false;
        }
    }

    private void fetchPlaylists(RequestQueue requestQueue, SpotifyAppRemote mSpotifyAppRemote){
        // build a Volley JSON Object response listener
        Response.Listener<JSONObject> playlistsRequestListener = response -> {
            try {
                JSONArray playlistsJSONArray = response.getJSONArray("items");
                // consume playlists
                // if the user has playlists
                if(playlistsJSONArray.length() > 0){
                    playlists = new ArrayList<>(playlistsJSONArray.length());
                    String playlistNameKey = "name";
                    String playlistIdKey = "id";
                    String playlistURIkey = "uri";
                    String playlistImagesKey = "images";

                    for (int i = 0; i < playlistsJSONArray.length(); i++) {
                        // create new playlist object for each playlist in JSON Array
                        JSONObject playlistJSONObj = playlistsJSONArray.getJSONObject(i);
                        JSONArray playlistImageArray = playlistJSONObj.getJSONArray(playlistImagesKey);
                        JSONObject imageObject = playlistImageArray.getJSONObject(0);
                        String name = playlistJSONObj.getString(playlistNameKey);
                        String id = playlistJSONObj.getString(playlistIdKey);
                        String uri = playlistJSONObj.getString(playlistURIkey);
                        String imageURL = imageObject.getString("url");
                        Playlist playlist = new Playlist(name, id, uri, imageURL);

                        // add to playlists array
                        playlists.add(i,playlist);
                    }
                    showProgressBar(false);
                    // set adapter for recycler view
                    rv_adapter = new PlaylistAdapter(MainActivity.this, playlists, mSpotifyAppRemote);
                    // set the adapter for the recycler view
                    rv_main_playlists.setAdapter(rv_adapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        };
        // build URL
        String playlistRequestURL = getString(R.string.SpotifyUserPlaylistsEndpoint);

        // create Volley request
        PlaylistRequest playlistRequest = new PlaylistRequest(playlistRequestURL,null, playlistsRequestListener, authentication.getErrorListener(), preferences);
        requestQueue.add(playlistRequest);
    }

    public void showProgressBar(Boolean show) {
        if (show) {
            rv_main_playlists.setVisibility(View.GONE);
            pb_main.setVisibility(View.VISIBLE);
        } else {
            rv_main_playlists.setVisibility(View.VISIBLE);
            pb_main.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int X = (int) event.getX();
        int Y = (int) event.getY();
        int eventAction = event.getAction();
        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                Toast.makeText(this, "ACTION_DOWN "+"X: "+X+" Y: "+Y, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "action_down: "+ X+" "+Y);
                break;
            case MotionEvent.ACTION_MOVE:
                Toast.makeText(this, "MOVE "+"X: "+X+" Y: "+Y,
                        Toast.LENGTH_SHORT).show();
                Log.i(TAG, "action_move: "+ X+" "+Y);
                break;
            case MotionEvent.ACTION_UP:
                Toast.makeText(this, "ACTION_UP "+"X: "+X+" Y: "+Y, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "action_up: "+ X+" "+Y);
                break;
        }
        return false;
    }
}