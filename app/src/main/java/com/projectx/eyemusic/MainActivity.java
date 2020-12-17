package com.projectx.eyemusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.types.Track;

public class MainActivity extends AppCompatActivity {
    private static final String CLIENT_ID = "f23b98eceee94735bddd9bab5b2d8280";
    private static final String REDIRECT_URI = "http://com.projectx.eyemusic/callback";
    private static final String SPOTIFY_PACKAGE_NAME = "com.spotify.music";
    private static final String PLAY_STORE_URI = "https://play.google.com/store/apps/details";
    private static final String REFERRER = "adjust_campaign=com.projectx.eyemusic&adjust_tracker=ndjczk&utm_source=adjust_preinstall";
    private final String TAG = MainActivity.class.getName();
    private SpotifyAppRemote mSpotifyAppRemote;

    Button btn_play, btn_pause;
    TextView tv_message;
    TextView tv_artist;
    boolean flag = false;
    PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_play = findViewById(R.id.btn_main_play);
        btn_pause = findViewById(R.id.btn_main_pause);
        tv_message = findViewById(R.id.tv_main_message);
        packageManager = getPackageManager();


    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if Spotify is installed each time the app is launched. Requirement!
        if(!isSpotifyInstalled(packageManager)){
            // Send user to Play Store to install if available in current market
            // TO-DO: If Spotify is not in user's market --> potentially can't of our app
            Log.w(TAG, "Spotify isn't installed! Going to play store");
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
        } else{
            // spotify is installed. Now try and connect
            // set the connection parameters
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
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
            SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
                @Override
                public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote;
                    // connection was successful
                    Toast.makeText(getApplicationContext(), "Successfully Connected", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Connected Successfully");

                    // Interact with AppRemote
                    connected();
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
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private boolean isSpotifyInstalled(PackageManager packageManager){
        try{
            packageManager.getPackageInfo(MainActivity.SPOTIFY_PACKAGE_NAME,0);
            return true;
        } catch (PackageManager.NameNotFoundException e){
            return false;
        }
    }

}