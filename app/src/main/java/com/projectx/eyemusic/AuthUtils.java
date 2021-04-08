package com.projectx.eyemusic;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.RequestQueue;

public interface AuthUtils {
    void authenticate(Activity contextActivity, final int SPOTIFY_AUTH_CODE_REQUEST_CODE);

    void storeAccessCode(String accessCode);

    void storeToken(String accessToken);

    String getAccessCode();

    String getAccessToken();

    String getRefreshToken();

    void fetchTokens();

    void refreshAccessToken();

    boolean isAuthenticated();

    boolean isAccessTokenExpired();
}
