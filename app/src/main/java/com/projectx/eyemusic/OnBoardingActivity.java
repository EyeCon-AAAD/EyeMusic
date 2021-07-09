package com.projectx.eyemusic;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.projectx.eyemusic.Authentication.Authentication;
import com.projectx.eyemusic.Fragments.OnBoarding.SplashFragment;
import com.projectx.eyemusic.Fragments.OnBoarding.SpotifyConnectionFragment;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class OnBoardingActivity extends AppCompatActivity {
    private static final String TAG = "OnBoardingActivity";
    public Authentication authentication;
    private static final int SPOTIFY_AUTH_CODE_REQUEST_CODE = 0x11;
    public SharedPreferences preferences;
    private static String APP_PACKAGE_NAME;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        APP_PACKAGE_NAME = getString(R.string.APP_PACKAGE_NAME);
        // shared preferences
        preferences = getSharedPreferences(APP_PACKAGE_NAME, Context.MODE_PRIVATE);
    }

    // use deprecated onActivityResult cause of Spotify Libraries
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // get response
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

        if (response.getError() != null && response.getError().isEmpty()) {
            Log.e(TAG, response.getError());
        }
        else if(requestCode == SPOTIFY_AUTH_CODE_REQUEST_CODE){
            String mAccessCode = response.getCode();

            if (mAccessCode != null) {
                // store Access code
                authentication.storeAccessCode(mAccessCode);
                // fetch access token and refresh token and store them
                authentication.fetchTokens();
                // go to next fragment after receiving & storing tokens
                SpotifyConnectionFragment.viewPager2.setCurrentItem(1);
            }
        }
    }
}