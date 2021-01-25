package com.projectx.eyemusic.VolleyRequests;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RefreshedAccessTokenStringRequest extends StringRequest {
    String client_id;
    String client_secret;
    String refresh_token;
    HashMap<String, String> params;
    HashMap<String, String> headers;

    /**
     * Creates a new request with the given method.
     *
     * @param //method        the request {@link Method} to use
     * @param url           URL to fetch the string at
     * @param listener      Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public RefreshedAccessTokenStringRequest(String url, String client_id, String client_secret, String refreshToken, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(Method.POST, url, listener, errorListener);
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.refresh_token = refreshToken;
    }

    /**
     * Returns the content type of the POST or PUT body.
     */
    @Override
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded";
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
        params = new HashMap<>();
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", this.refresh_token);
        return params;
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
        String creds = String.format("%s:%s", client_id, client_secret);
        Log.d("Refresh Access Token->", "Credentials: " + creds);
        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
        headers.put("Authorization", auth);
        return headers;
    }
}
