package com.projectx.eyemusic.VolleyRequests;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TracksRequest extends JsonObjectRequest {
    Map<String, String> headers;
    SharedPreferences preferences = null;
    /**
     * Creates a new request.
     *
     * @param method        the HTTP method to use
     * @param url           URL to fetch the JSON from
     * @param jsonRequest   A {@link JSONObject} to post with the request. Null indicates no
     *                      parameters will be posted along with request.
     * @param listener      Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public TracksRequest(int method, String url, SharedPreferences preferences,@Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.preferences = preferences;
        Log.d("Track request->", "sent to Spotify");

    }

    /**
     * Returns a list of extra HTTP headers to go along with this request. Can throw {@link
     * AuthFailureError} as authentication may be required to provide these values.
     *
     * @throws AuthFailureError In the event of auth failure
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        headers = new HashMap<>();
        String mAccessToken = preferences.getString("accessToken", "Access token not found");
        headers.put("Authorization", "Bearer " + mAccessToken);
        return headers;
    }
}
