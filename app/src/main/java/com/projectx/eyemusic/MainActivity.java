package com.projectx.eyemusic;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.projectx.eyemusic.VolleyRequests.PlaylistRequest;
import com.projectx.eyemusic.VolleyRequests.RefreshTokenStringRequest;
import com.projectx.eyemusic.VolleyRequests.RefreshedAccessTokenStringRequest;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.*;

import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
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
    private String mRefreshToken;
    private final String[] SCOPES = {
            "playlist-read-private",
            "playlist-read-collaborative",
            "user-library-read",
            "app-remote-control"};

    SharedPreferences preferences = null;

    // Create an array of playlists
    ArrayList<Playlist> playlists;

    // Views
    Button btn_play, btn_pause;
    TextView tv_message;
    TextView tv_artist, tv_auth_token;
    RecyclerView rv_main_playlists;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter rv_adapter;
    ProgressBar pb_main;
    boolean flag = false;
    PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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


    }

    private void authenticateCode(){
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID,
                AuthorizationResponse.Type.CODE, REDIRECT_URI);
        builder.setScopes(SCOPES);

        AuthorizationRequest request = builder.build();
        Log.d(TAG, request.toString());
        AuthorizationClient.openLoginActivity(MainActivity.this, SPOTIFY_AUTH_CODE_REQUEST_CODE, request);
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

            // do what we want with the token
            if (mAccessToken != null){Log.d(TAG, mAccessToken);}

            // tv_auth_token.setText(mAccessToken);
            // store the access token
            preferences.edit().putString("accessToken", response.getAccessToken()).commit();
            Log.d(TAG, "response token: " + response.getAccessToken());

            // fetch playlists after getting token
            showProgressBar(true);
            //fetchPlaylists(requestQueue, mSpotifyAppRemote);


        } else if(requestCode == SPOTIFY_AUTH_CODE_REQUEST_CODE){
            mAccessCode = response.getCode();

            if (mAccessCode != null){Log.d(TAG, mAccessCode);}
            Toast.makeText(getApplicationContext(), "Access Code ====> " + mAccessCode, Toast.LENGTH_LONG);
            // store Access code
            preferences.edit().putString("accessCode", mAccessCode).commit();

            // fetch access token and refresh token and store them
            Log.d(TAG, "Access Code before fetching token => " + mAccessCode);
            fetchTokens(requestQueue);

            // call om start after successfully authenticating
            onStart();

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        // check if EyeMusic is launched for the first time
        if(!isFirstTimeLaunch()){
            mAccessToken = preferences.getString("accessToken", "Access token not found");
            mAccessCode = preferences.getString("accessCode", "Access code not found");


            // ------------------- perform error check for when the access is denied but launch is not first time ----------
        }
        // Check if Spotify is installed each time the app is launched. Requirement!
        if(!isSpotifyInstalled(packageManager)){
            // Send user to Play Store to install if available in current market
            // TO-DO: If Spotify is not in user's market --> potentially can't of our app
            Log.w(TAG, "Spotify isn't installed! Going to play store");
            // Alert Dialog for good UX
            Context context;
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

        } else{
            // spotify is installed. Now try and connect
            // set the connection parameters
            // only connect if authenticated
            if(isAuthenticated()){
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
                        Log.d(TAG, "Connected Successfully");

                        // Interact with AppRemote
                        //connected();
                        // refresh Tokens before fetching playlists
                        refreshTokens(requestQueue);

                        fetchPlaylists(requestQueue, mSpotifyAppRemote);
                        //fetchTokens(requestQueue);
                        //Log.d(TAG, mAccessToken);
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
    protected void onStop() {
        super.onStop();
        // disconnect from AppRemote
        // add code for stopping play if playing
        //SpotifyAppRemote.disconnect(mSpotifyAppRemote);
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
            //authenticate();
            authenticateCode();
            // set first time to false
            preferences.edit().putBoolean("firstTime", false).commit();
            return true;
        } else{
            return false;
        }
    }

    private void fetchPlaylists(RequestQueue requestQueue, SpotifyAppRemote mSpotifyAppRemote){
        // build a Volley JSON Object response listener
        Response.Listener<JSONObject> playlistsRequestListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray playlistsJSONArray = response.getJSONArray("items");
                    Log.d(TAG, "playlist request -> " + playlistsJSONArray.toString());
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
                        Log.d(TAG, "Playlists: " + playlists.toString());
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

            }

        };

        // build a Volley JSON Error listener
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e(TAG, error.toString());
                //showProgressBar(false);

                //--------------- use toast messages for now ----------------------
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    //This indicates that the request has either time out or there is no connection
                    Toast.makeText(getApplicationContext(), "Internet Connection Error!", Toast.LENGTH_SHORT).show();

                } else if (error instanceof AuthFailureError) {
                    // Error indicating that there was an Authentication Failure while performing the request
                    Toast.makeText(getApplicationContext(), "Authentication Error!" +
                            "\nRefresh Token", Toast.LENGTH_SHORT).show();

                } else if (error instanceof ServerError) {
                    //Indicates that the server responded with a error response
                    Toast.makeText(getApplicationContext(), "Server Error!", Toast.LENGTH_SHORT).show();

                } else if (error instanceof NetworkError) {
                    //Indicates that there was network error while performing the request
                    Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();

                } else if (error instanceof ParseError) {
                    // Indicates that the server response could not be parsed
                    Toast.makeText(getApplicationContext(), "Parse Error!", Toast.LENGTH_SHORT).show();

                }
            }
        };

        // build URL
        String playlistRequestURL = getString(R.string.SpotifyUserPlaylistsEndpoint);

        // create headers as JSON object
        JSONObject jsonHeader = new JSONObject();
        try {
            jsonHeader.put("Accept", "application/json");
            jsonHeader.put("Content-Type", "application/json");
            jsonHeader.put("Authorization", "Bearer " + mAccessToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // create Volley request
        PlaylistRequest playlistRequest = new PlaylistRequest(playlistRequestURL,null, playlistsRequestListener, errorListener, preferences);
        requestQueue.add(playlistRequest);
    }

    private void fetchTokens(RequestQueue requestQueue) {
        // in order to get access token and refresh token, we need to make a post request using the access code
        mAccessCode = preferences.getString("accessCode", "No access code");

        if(!mAccessCode.equals("No access code")){
            // We got the access code in storage now use it to fetch auth token and refresh token
            // Use Volley POST request with the following in the Body. As prescribed in Spotify docs:
            // https://developer.spotify.com/documentation/general/guides/authorization-guide/#authorization-code-flow

            // create a Response Listener and an Error Listener
            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //--------------- use toast messages for now ----------------------
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        //This indicates that the request has either time out or there is no connection
                        Toast.makeText(getApplicationContext(), "Internet Connection Error!", Toast.LENGTH_SHORT).show();

                    } else if (error instanceof AuthFailureError) {
                        // Error indicating that there was an Authentication Failure while performing the request
                        Toast.makeText(getApplicationContext(), "Authentication Error!" +
                                "\nWhile requesting for refresh tokens", Toast.LENGTH_SHORT).show();

                    } else if (error instanceof ServerError) {
                        //Indicates that the server responded with a error response
                        Toast.makeText(getApplicationContext(), "Server Error!", Toast.LENGTH_SHORT).show();
                        // Log error in console
                        parseVolleyError(error);

                    } else if (error instanceof NetworkError) {
                        //Indicates that there was network error while performing the request
                        Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();

                    } else if (error instanceof ParseError) {
                        // Indicates that the server response could not be parsed
                        Toast.makeText(getApplicationContext(), "Parse Error!", Toast.LENGTH_SHORT).show();

                    }
                }
            };
            Response.Listener<String> tokenStringRequest = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Response=> " +response);
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        mAccessToken = responseJson.getString("access_token");
                        mRefreshToken = responseJson.getString("refresh_token");

                        // store the tokens
                        preferences.edit().putString("accessToken", mAccessToken).commit();
                        preferences.edit().putString("refreshToken", mRefreshToken).commit();
                        Log.d(TAG, "Access Token-> " + mAccessToken);
                        Log.d(TAG, "Refresh Token-> " + mRefreshToken);

                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }
                }
            };
            String refreshTokenURL = getString(R.string.SpotifyTokenEndpoint);
            RefreshTokenStringRequest stringRequest = new RefreshTokenStringRequest(refreshTokenURL, CLIENT_ID, CLIENT_SECRET, mAccessCode, REDIRECT_URI, tokenStringRequest, errorListener);
            requestQueue.add(stringRequest);
        } else{
            Toast.makeText(getApplicationContext(), "No Access code stored, check authorization", Toast.LENGTH_SHORT).show();
        }

    }

    private void refreshTokens(RequestQueue requestQueue){
        mRefreshToken = preferences.getString("refreshToken", "No refresh token");
        if(!mRefreshToken.equals("No refresh token")){
            // create a Response Listener and an Error Listener
            Response.ErrorListener errorListener = error -> {
                //--------------- use toast messages for now ----------------------
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    //This indicates that the request has either time out or there is no connection
                    Toast.makeText(getApplicationContext(), "Internet Connection Error!", Toast.LENGTH_SHORT).show();

                } else if (error instanceof AuthFailureError) {
                    // Error indicating that there was an Authentication Failure while performing the request
                    Toast.makeText(getApplicationContext(), "Authentication Error!" +
                            "\nWhile requesting for refresh tokens", Toast.LENGTH_SHORT).show();

                } else if (error instanceof ServerError) {
                    //Indicates that the server responded with a error response
                    Toast.makeText(getApplicationContext(), "Server Error!", Toast.LENGTH_SHORT).show();
                    // Log error in console
                    parseVolleyError(error);

                } else if (error instanceof NetworkError) {
                    //Indicates that there was network error while performing the request
                    Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();

                } else if (error instanceof ParseError) {
                    // Indicates that the server response could not be parsed
                    Toast.makeText(getApplicationContext(), "Parse Error!", Toast.LENGTH_SHORT).show();

                }
            };
            Response.Listener<String> refreshListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Response=> " +response);
                    try {
                        // get refreshed Access Token and store it
                        JSONObject responseJSON = new JSONObject(response);
                        mAccessToken = responseJSON.getString("access_token");

                        // store the new access token
                        preferences.edit().putString("accessToken", mAccessToken).commit();

                        Log.d(TAG, "Refreshed Access Token-> " + mAccessToken);

                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }
                }
            };
            String refreshTokenURL = getString(R.string.SpotifyTokenEndpoint);
            RefreshedAccessTokenStringRequest accessTokenStringRequest = new RefreshedAccessTokenStringRequest(refreshTokenURL, CLIENT_ID, CLIENT_SECRET, mRefreshToken, refreshListener, errorListener);
            requestQueue.add(accessTokenStringRequest);
        }else {Toast.makeText(getApplicationContext(), "No refresh Token stored, check authorization", Toast.LENGTH_SHORT).show();}
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

    public boolean isAuthenticated(){
        String tempAccessCode = preferences.getString("accessCode", "No Access Code");
        return !tempAccessCode.equals("No Access Code");
    }

    public void parseVolleyError(VolleyError error){
        try {
          String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
          JSONObject data = new JSONObject(responseBody);
          String errorType = data.getString("error");
          String error_description = data.getString("error_description");
          Log.e(TAG, "{Error Type: " + errorType + ",Error description: " + error_description + "}");
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }
}