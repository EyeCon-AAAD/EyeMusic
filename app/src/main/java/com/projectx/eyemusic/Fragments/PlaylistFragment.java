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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.Model.GazePoint;
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
    static LinearLayoutManager layoutManager;

    // static for remap
    static ImageView btnup;
    static ImageView btndown;

    // for remap
    static GazePoint[] locations_playlist = new GazePoint[4];
    static View[] references_playlist = new View[4];

    static int[] location_rv = new int[2];
    static RecyclerView reference_rv;
    public static GazePoint[] getLocationsPlaylist() {
        return locations_playlist;
    }
    public static View[] getReferencesPlaylist() {
        return references_playlist;
    }
    public static int[] getLocation_rv() {
        return location_rv;
    }
    public static RecyclerView getReference_rv() {
        return reference_rv;
    }


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
        mainActivity.resetBackCounter();


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = view.findViewById(R.id.pb_load_main);

        btnup = view.findViewById(R.id.btnup);
        // for remap
        btnup.post(() -> {
            int[] point = new int[2];
            btnup.getLocationOnScreen(point);
            int width = btnup.getWidth();
            int height = btnup.getHeight();
            locations_playlist[0] = new GazePoint(point[0]+((float)width/2), point[1]+((float)height/2));
            references_playlist[0] = btnup;
            location_rv[0] = point[1]+height;
        });


        btndown = view.findViewById(R.id.btndown);
        // for remap
        btndown.post(() -> {
            int[] point = new int[2];
            btndown.getLocationOnScreen(point);
            int width = btndown.getWidth();
            int height = btndown.getHeight();
            locations_playlist[1] = new GazePoint(point[0]+((float)width/2), point[1]+((float)height/2));
            references_playlist[1] = btndown;
            location_rv[1] = point[1];
        });
        reference_rv = view.findViewById(R.id.rv_playlists);

        initPlaylistRecyclerView(view);



    }

    private void initPlaylistRecyclerView(View view) {
        RecyclerView rv_playlists = view.findViewById(R.id.rv_playlists);
        rv_playlists.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());

        rv_playlists.setLayoutManager(layoutManager);
        // check internet connection before fetching playlists
        if(Utilities.isConnected()){
            fetchPlaylists(mainActivity.requestQueue, MainActivity.mSpotifyAppRemote, rv_playlists);
        } else{
            // show network error fragment
            Fragment fragment = NetworkErrorFragment.newInstance(getString(R.string.playlistFragment), null);
            MainActivity.currentFragment = fragment;
            Fragment curfragment = mainActivity.getSupportFragmentManager().findFragmentByTag("Playlist Fragment");
            mainActivity.getSupportFragmentManager().beginTransaction()
                    .remove(curfragment)
                    .add(R.id.main_fragment_container, fragment, "Network Error Fragment")
                    .commit();
        }
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
                    Log.d(TAG, "playlist count: " + rv_adapter.getItemCount());
                    // set the adapter for the recycler view
                    rv_playlists.setAdapter(rv_adapter);

                } else{
                    // the user doesn't have any playlists. Show appropriate UI to create playlists
                    // in Spotify
                    Fragment fragment = new PlaylistErrorFragment();
                    mainActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment_container, fragment, "Playlist Error Fragment")
                            .commit();
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
            updatePlaylistItemReferences();
        });
        MainActivity.buttoneffect(btnup);

        btndown.setOnClickListener(v -> {
            if (recyclerView.getAdapter() != null) {
                int totalItemCount = recyclerView.getAdapter().getItemCount();
                if (totalItemCount <= 0) return;
                int lastVisibleItemIndex = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisibleItemIndex >= totalItemCount) return;
                linearLayoutManager.smoothScrollToPosition(recyclerView, null, lastVisibleItemIndex + 1);
            }
            updatePlaylistItemReferences();
        });
        MainActivity.buttoneffect(btndown);


    }

    public static void updatePlaylistItemReferences(){
        // for remap
        if(layoutManager==null) return;
        int first_position = layoutManager.findFirstCompletelyVisibleItemPosition();
        Log.d(TAG, "first_position: "+first_position);
        View first_item = layoutManager.findViewByPosition(first_position);
        if(first_item!= null){
            Log.d(TAG, "first_position: not null");
            first_item.post(()->{
                int[] point = new int[2];
                first_item.getLocationOnScreen(point);
                int width = first_item.getWidth();
                int height = first_item.getHeight();
                locations_playlist[2] = new GazePoint(point[0]+((float)width/2), point[1]+((float)height/2));
                references_playlist[2] = first_item.findViewById(R.id.tv_playlist_name);
            });
        }else Log.d(TAG, "first_position: null");

        // for remap
        int last_position = layoutManager.findLastCompletelyVisibleItemPosition();
        View last_item = layoutManager.findViewByPosition(last_position);
        if(last_item!=null) {
            last_item.post(() -> {
                int[] point = new int[2];
                last_item.getLocationOnScreen(point);
                int width = last_item.getWidth();
                int height = last_item.getHeight();
                locations_playlist[3] = new GazePoint(point[0] + ((float) width / 2), point[1] + ((float) height / 2));
                references_playlist[3] = last_item.findViewById(R.id.tv_playlist_name);;
            });
        }
    }

}