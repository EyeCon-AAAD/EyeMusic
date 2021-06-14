package com.projectx.eyemusic.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.Music.Playlist;
import com.projectx.eyemusic.Music.PlaylistAdapter;
import com.projectx.eyemusic.R;
import com.projectx.eyemusic.Utilities;
import com.projectx.eyemusic.VolleyRequests.PlaylistRequest;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlaylistFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaylistFragment extends Fragment {

    private static final String TAG = "PlaylistFragment";
    public MainActivity mainActivity;
    ProgressBar progressBar;
    ImageView btnup;
    ImageView btndown;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlaylistFragment.
     */
    public static PlaylistFragment newInstance() {
        return new PlaylistFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = view.findViewById(R.id.pb_load_main);
        btnup = view.findViewById(R.id.btnup);
        btndown = view.findViewById(R.id.btndown);
        initPlaylistRecyclerView(view);
    }

    private void initPlaylistRecyclerView(View view) {
        RecyclerView rv_playlists = view.findViewById(R.id.rv_playlists);
        rv_playlists.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_playlists.setLayoutManager(layoutManager);
        fetchPlaylists(mainActivity.requestQueue, MainActivity.mSpotifyAppRemote, rv_playlists);
        scrollControls(layoutManager, rv_playlists);
    }

    private void fetchPlaylists(RequestQueue requestQueue, SpotifyAppRemote mSpotifyAppRemote, RecyclerView rv_playlists){

        // build a Volley JSON Object response listener
        Response.Listener<JSONObject> playlistsRequestListener = response -> {
            // Create an array of playlists
            ArrayList<Playlist> playlists;

            try {
                JSONArray playlistsJSONArray = response.getJSONArray("items");
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
                    Utilities.showProgressBar(progressBar, rv_playlists, false);
                    // set adapter for recycler view
                    PlaylistAdapter rv_adapter = new PlaylistAdapter(getContext(), playlists, mSpotifyAppRemote);
                    // set the adapter for the recycler view
                    rv_playlists.setAdapter(rv_adapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        };
        // build URL
        String playlistRequestURL = getString(R.string.SpotifyUserPlaylistsEndpoint);

        // create Volley request
        PlaylistRequest playlistRequest = new PlaylistRequest(playlistRequestURL,null,
                playlistsRequestListener, mainActivity.authentication.getErrorListener("Fetch Playlists"), mainActivity.preferences);
        requestQueue.add(playlistRequest);
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
            int totalItemCount = recyclerView.getAdapter().getItemCount();
            if (totalItemCount <= 0) return;
            int lastVisibleItemIndex = linearLayoutManager.findLastCompletelyVisibleItemPosition();

            if (lastVisibleItemIndex >= totalItemCount) return;
            linearLayoutManager.smoothScrollToPosition(recyclerView,null,lastVisibleItemIndex+1);
        });
        MainActivity.buttoneffect(btndown);
    }

}