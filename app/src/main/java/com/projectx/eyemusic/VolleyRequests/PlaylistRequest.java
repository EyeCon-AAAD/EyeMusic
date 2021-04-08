package com.projectx.eyemusic.VolleyRequests;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.projectx.eyemusic.MainActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class PlaylistRequest extends JsonObjectRequest {
    private Map<String,String> headers;
    SharedPreferences preferences = null;

    /**
     * Constructor which defaults to <code>GET</code> if <code>jsonRequest</code> is <code>null
     * </code> , <code>POST</code> otherwise.
     *
     * @param url
     * @param jsonRequest
     * @param listener
     * @param errorListener
     */
    public PlaylistRequest(String url, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener, SharedPreferences preferences1) {
        super(url, jsonRequest, listener, errorListener);
        preferences = preferences1;
        Log.d("Playlist request", "Request sent to Spotify");
    }

    /**
     * Returns a Map of parameters to be used for a POST or PUT request. Can throw {@link
     * AuthFailureError} as authentication may be required to provide these values.
     *
     * <p>Note that you can directly override {@link #getBody()} for custom data.
     *
     * @throws AuthFailureError in the event of auth failure
     */
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Log.d("PlaylistRequest", headers.toString());
        return headers;
    }

    /**
     * Returns a list of extra HTTP headers to go along with this request. Can throw {@link
     * AuthFailureError} as authentication may be required to provide these values.
     *
     * @throws AuthFailureError In the event of auth failure
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        headers =new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        //preferences = getApplication().getSharedPreferences(APP_PACKAGE_NAME, MODE_PRIVATE);
        String mAccessToken = preferences.getString("accessToken", "Access token not found");
        headers.put("Authorization", "Bearer " + mAccessToken);
        return headers;
    }
}
