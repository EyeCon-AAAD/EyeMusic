package com.projectx.eyemusic;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
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
import com.projectx.eyemusic.VolleyRequests.RefreshTokenStringRequest;
import com.projectx.eyemusic.VolleyRequests.RefreshedAccessTokenStringRequest;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class Authentication implements AuthUtils{

    private String CLIENT_ID;
    private String CLIENT_SECRET;
    private String REDIRECT_URI;
    private final String[] SCOPES = {
            "playlist-read-private",
            "playlist-read-collaborative",
            "user-library-read",
            "app-remote-control"};
    private SharedPreferences preferences;
    private RequestQueue requestQueue;
    private Context context;
    private Response.ErrorListener errorListener;
    private Response.Listener<String> tokenStringRequest, refreshedTokenRequest;

    public Authentication(String CLIENT_ID, String CLIENT_SECRET, String REDIRECT_URI, SharedPreferences preferences, RequestQueue requestQueue, Context context) {
        this.CLIENT_ID = CLIENT_ID;
        this.CLIENT_SECRET = CLIENT_SECRET;
        this.REDIRECT_URI = REDIRECT_URI;
        this.preferences = preferences;
        this.requestQueue = requestQueue;
        this.context = context;
        this.errorListener = null;
        this.tokenStringRequest = null;
        this.refreshedTokenRequest = null;
    }

    /**
     *  Create the Volley Error Listener
     * */
    public Response.ErrorListener getErrorListener() {
        errorListener = error -> {
            //--------------- use toast messages for now ----------------------
            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                //This indicates that the request has either time out or there is no connection
                Toast.makeText(context, "Internet Connection Error!", Toast.LENGTH_SHORT).show();

            } else if (error instanceof AuthFailureError) {
                // Error indicating that there was an Authentication Failure while performing the request
                Toast.makeText(context, "Authentication Error!" +
                        "\nWhile requesting for refresh tokens", Toast.LENGTH_SHORT).show();

            } else if (error instanceof ServerError) {
                //Indicates that the server responded with a error response
                Toast.makeText(context, "Server Error!", Toast.LENGTH_SHORT).show();
                // Log error in console
                parseVolleyError(error);

            } else if (error instanceof NetworkError) {
                //Indicates that there was network error while performing the request
                Toast.makeText(context, "Network Error!", Toast.LENGTH_SHORT).show();

            } else if (error instanceof ParseError) {
                // Indicates that the server response could not be parsed
                Toast.makeText(context, "Parse Error!", Toast.LENGTH_SHORT).show();

            }
        };
        return errorListener;
    }

    /**
     *  Create the Volley Response Listener for getting the Access Token and Refresh Token
     * */
    public Response.Listener<String> getTokenStringRequest() {
        tokenStringRequest = response -> {
            try{
                JSONObject jsonResponse = new JSONObject(response);
                String accessToken = jsonResponse.getString(context.getString(R.string.jsonAccessTokenKey));
                String refreshToken = jsonResponse.getString(context.getString(R.string.jsonRefreshTokenKey));
                storeToken(accessToken, refreshToken);
            }catch (JSONException jsonException){
                jsonException.printStackTrace();
            }
        };
        return tokenStringRequest;
    }

    /**
     *  Create the Volley Response Listener for getting a new Access Token when the current one is expired
     * */
    public Response.Listener<String> getRefreshedTokenRequest() {
        refreshedTokenRequest = response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                String accessToken = jsonResponse.getString(context.getString(R.string.jsonAccessTokenKey));

                // store the new access token
                storeToken(accessToken);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        };
        return refreshedTokenRequest;
    }

    /**
     * Authenticate the user by opening Spotify Login Activity with the desired scopes.
     * The result is an Authorization Code that is used to obtain Access Tokens and/or Refresh Tokens
     * */
    @Override
    public void authenticate(Activity contextActivity, final int SPOTIFY_AUTH_CODE_REQUEST_CODE) {
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID,
                AuthorizationResponse.Type.CODE, REDIRECT_URI);
        builder.setScopes(SCOPES);
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(contextActivity, SPOTIFY_AUTH_CODE_REQUEST_CODE, request);
    }
    
    /**
     * Store the AccessCode to the disk
     * */
    @Override
    public void storeAccessCode(String accessCode) {
        preferences.edit().putString(context.getString(R.string.AccessCodeKey), accessCode).apply();
    }
    
    /**
     * Store Access Token and Refresh Token to the disk
     * */
    private void storeToken(String accessToken, String refreshToken) {
        preferences.edit().putString(context.getString(R.string.AccessTokenKey), accessToken).apply();
        preferences.edit().putString(context.getString(R.string.RefreshTokenKey), refreshToken).apply();
    }

    /**
     * Store the Access Token to the disk
     * */
    @Override
    public void storeToken(String accessToken) {
        preferences.edit().putString(context.getString(R.string.AccessTokenKey), accessToken).apply();
    }

    /**
     * Get the Access Code from the disk
     * */
    @Override
    public String getAccessCode(){
        return preferences.getString(context.getString(R.string.AccessCodeKey), context.getString(R.string.Unauthenticated));
    }

    /**
     * Get the Access Token from the disk
     * */
    @Override
    public String getAccessToken(){
        return preferences.getString(context.getString(R.string.AccessTokenKey), context.getString(R.string.Unauthenticated));
    }

    /**
     * Get the Refresh Token from the disk
     * */
    @Override
    public String getRefreshToken(){
        return preferences.getString(context.getString(R.string.RefreshTokenKey), context.getString(R.string.Unauthenticated));
    }

    /**
     * Check if the user is authenticated by determining if an Access Code is stored in the disk
     * */
    @Override
    public boolean isAuthenticated() {
        String tempAccessCode = getAccessCode();
        return !tempAccessCode.equals(context.getString(R.string.Unauthenticated));
    }

    private void parseVolleyError(VolleyError error){
        try {
            String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
            JSONObject data = new JSONObject(responseBody);
            String errorType = data.getString("error");
            String error_description = data.getString("error_description");
            Log.e(context.toString(), "{Error Type: " + errorType + ",Error description: " + error_description + "}");
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    /**
     * Make a POST request with the Access Code as a parameter to obtain the Refresh and Access Tokens
     * */
    @Override
    public void fetchTokens() {
        if(isAuthenticated()){
            // Following these instructions from Spotify
            // https://developer.spotify.com/documentation/general/guides/authorization-guide/#authorization-code-flow
            String refreshTokenURL = context.getString(R.string.SpotifyTokenEndpoint);
            String accessCode = getAccessCode();

            RefreshTokenStringRequest stringRequest = new RefreshTokenStringRequest(refreshTokenURL, CLIENT_ID, CLIENT_SECRET, accessCode, REDIRECT_URI, getTokenStringRequest(), getErrorListener());
            requestQueue.add(stringRequest);
        } else{
            String errorMessage = context.getString(R.string.UnauthorizedError);
            showError(errorMessage);
        }
    }

    /**
     * Make a POST request with the Refresh Token as a parameter to obtain the new Access Token
     * */
    @Override
    public void refreshAccessToken() {
        String refreshToken = getRefreshToken();
        if(!refreshToken.equals(context.getString(R.string.UnauthorizedError))){
            String refreshTokenURL = context.getString(R.string.SpotifyTokenEndpoint);

            RefreshedAccessTokenStringRequest newAccessTokenRequest = new RefreshedAccessTokenStringRequest(refreshTokenURL, CLIENT_ID, CLIENT_SECRET, refreshToken, getRefreshedTokenRequest(), getErrorListener());
            requestQueue.add(newAccessTokenRequest);
        } else{
            String errorMessage = context.getString(R.string.UnauthorizedError);
            showError(errorMessage);
        }
    }

    /**
     * Show error as a Toast for now
     * */
    private void showError(String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}