package com.projectx.eyemusic;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.projectx.eyemusic.VolleyRequests.TracksRequest;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TracksActivity extends AppCompatActivity {
    ArrayList<MyTrack> tracks;
    Toolbar toolbar;
    RecyclerView rv_tracks;
    RecyclerView.LayoutManager rvLayoutManager;
    RecyclerView.Adapter rvAdapter;
    RelativeLayout relativeLayout;
    String TAG = TracksActivity.class.getName();

    SharedPreferences preferences = null;
    private String APP_PACKAGE_NAME;
    SpotifyAppRemote spotifyAppRemote;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_tracks);
        toolbar = findViewById(R.id.tracks_actionbar);
        relativeLayout = findViewById(R.id.relativeLayoutTracks);
        rv_tracks = findViewById(R.id.rv_tracks);
        rv_tracks.setHasFixedSize(true);
        rvLayoutManager= new LinearLayoutManager(TracksActivity.this);
        rv_tracks.setLayoutManager(rvLayoutManager);
        setSupportActionBar(toolbar);

        APP_PACKAGE_NAME = getString(R.string.APP_PACKAGE_NAME);
        spotifyAppRemote = MainActivity.mSpotifyAppRemote;

        // get playlist details from parent activity
        Intent intent = getIntent();
        String playlistName = intent.getStringExtra("playlistName");
        String playlistId =  intent.getStringExtra("playlistId");

        // change title of the action bar
        getSupportActionBar().setTitle(playlistName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        preferences = getSharedPreferences(APP_PACKAGE_NAME, MODE_PRIVATE);
        RequestQueue requestQueue  = Volley.newRequestQueue(TracksActivity.this);
        fetchPlaylistTracks(playlistId, requestQueue);


    }

    public void showProgressBar(Boolean show) {
        if (show) {
            rv_tracks.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);
        } else {
            rv_tracks.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: ");
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void fetchPlaylistTracks(String playlistId, RequestQueue requestQueue){
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgressBar(false);
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
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                showProgressBar(false);
                try {
                    JSONArray items = response.getJSONArray("items");
                    tracks = new ArrayList<>(items.length());
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject trackObject = items.getJSONObject(i).getJSONObject("track");
                        String artistName = trackObject.getJSONArray("artists").getJSONObject(0).getString("name");
                        String trackName = trackObject.getString("name");
                        String imageURL = trackObject.getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url");
                        String spotifyURI = trackObject.getString("uri");
                        Long duration_ms = trackObject.getLong("duration_ms");

                        MyTrack myTrack = new MyTrack(artistName, trackName, imageURL, spotifyURI, duration_ms);
                        tracks.add(myTrack);
                    }

                    // fill recycler view
                    rvAdapter = new TracksAdapter(tracks, getApplicationContext(),MainActivity.mSpotifyAppRemote);
                    rv_tracks.setAdapter(rvAdapter);

                    // may use this later for better and easier deserialization
                    /*Gson gson = new GsonBuilder().create();
                    Track track = gson.fromJson(itemObject.getJSONObject("track").toString(), Track.class);*/

                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }


            }
        };
        String URL = getString(R.string.SpotifyPlaylistItemsEndpoint) + "/" + playlistId + "/tracks?offset=0&limit=100&market=from_token";
        TracksRequest tracksRequest = new TracksRequest(Request.Method.GET, URL, preferences, null, listener, errorListener);
        showProgressBar(true);
        requestQueue.add(tracksRequest);

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