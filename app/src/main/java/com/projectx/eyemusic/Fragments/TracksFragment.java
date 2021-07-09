package com.projectx.eyemusic.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.Music.MyTrack;
import com.projectx.eyemusic.Music.TracksAdapter;
import com.projectx.eyemusic.R;
import com.projectx.eyemusic.Utilities;
import com.projectx.eyemusic.VolleyRequests.TracksRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TracksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TracksFragment extends Fragment {

    private static final String ARG_PLAYLIST_ID = "playlistId";
    private String playlistID;
    private static final String TAG = "TracksFragment";
    public MainActivity mainActivity;
    ProgressBar progressBar;
    ImageView btnup;
    ImageView btndown;

    public TracksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param playlistID Playlist ID.
     * @return A new instance of fragment TracksFragment.
     */
    public static TracksFragment newInstance(String playlistID) {
        TracksFragment fragment = new TracksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLAYLIST_ID, playlistID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playlistID = getArguments().getString(ARG_PLAYLIST_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tracks, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = view.findViewById(R.id.pb_load_main);
        btnup = view.findViewById(R.id.btnup);
        btndown = view.findViewById(R.id.btndown);
        initTracksRecyclerView(view);
    }

    private void initTracksRecyclerView(View view) {
        RecyclerView rv_tracks = view.findViewById(R.id.rv_tracks);
        rv_tracks.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_tracks.setLayoutManager(layoutManager);
        // check internet connection before fetching tracks
        if(Utilities.isConnected()){
            fetchPlaylistTracks(playlistID, mainActivity.requestQueue, rv_tracks);
        }else{
            // show network error fragment
            Fragment fragment = NetworkErrorFragment.newInstance(getString(R.string.tracksFragment),
                    playlistID);
            MainActivity.currentFragment = fragment;
            Fragment curfragment = mainActivity.getSupportFragmentManager().findFragmentByTag("Tracks Fragment");
            mainActivity.getSupportFragmentManager().beginTransaction()
                    .remove(curfragment)
                    .add(R.id.main_fragment_container, fragment, "Network Error Fragment")
                    .commit();
        }
        scrollControls(layoutManager, rv_tracks);
    }

    public void fetchPlaylistTracks(String playlistId, RequestQueue requestQueue, RecyclerView rv_tracks){

        Response.Listener<JSONObject> listener = response -> {
            Utilities.showProgressBar(progressBar, rv_tracks, false);
            ArrayList<MyTrack> tracks;
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
                TracksAdapter tracksAdapter = new TracksAdapter(tracks, getContext(),MainActivity.mSpotifyAppRemote);
                rv_tracks.setAdapter(tracksAdapter);

                // may use this later for better and easier deserialization
                /*Gson gson = new GsonBuilder().create();
                Track track = gson.fromJson(itemObject.getJSONObject("track").toString(), Track.class);*/

            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }


        };
        String URL = getString(R.string.SpotifyPlaylistItemsEndpoint) + "/" + playlistId
                + "/tracks?offset=0&limit=100&market=from_token";
        TracksRequest tracksRequest = new TracksRequest(Request.Method.GET, URL, mainActivity.preferences,
                null, listener,mainActivity.authentication.getErrorListener("Fetch Tracks"));
        Utilities.showProgressBar(progressBar, rv_tracks, true);
        requestQueue.add(tracksRequest);

    }

    private void scrollControls(LinearLayoutManager linearLayoutManager, RecyclerView recyclerView){
        btnup.setOnClickListener(v -> {
            int firstVisibleItemIndex = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
            if (firstVisibleItemIndex > 0) {
                linearLayoutManager.smoothScrollToPosition(recyclerView,null,firstVisibleItemIndex-1);
            }
        });
        MainActivity.buttoneffect(btnup);

        btndown.setOnClickListener(v -> {
            if(recyclerView.getAdapter() != null) {
                int totalItemCount = recyclerView.getAdapter().getItemCount();
                if (totalItemCount <= 0) return;
                int lastVisibleItemIndex = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisibleItemIndex >= totalItemCount) return;
                linearLayoutManager.smoothScrollToPosition(recyclerView, null, lastVisibleItemIndex + 1);
            }
        });
        MainActivity.buttoneffect(btndown);
    }

}