package com.projectx.eyemusic.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NetworkErrorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NetworkErrorFragment extends Fragment {

    private static final String ARG_PREV_FRAGMENT = "prevFragment";
    private static final String ARG_PLAYLIST_ID = "playlistID";
    private String prevFragment;
    private String playlistId;

    Button btn_retry;
    MainActivity mainActivity;

    public NetworkErrorFragment() {
        // Required empty public constructor
    }

    public static NetworkErrorFragment newInstance(String fragmentName, String playlistID) {
        NetworkErrorFragment fragment = new NetworkErrorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PREV_FRAGMENT, fragmentName);
        args.putString(ARG_PLAYLIST_ID, playlistID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            prevFragment = getArguments().getString(ARG_PREV_FRAGMENT);
            playlistId = getArguments().getString(ARG_PLAYLIST_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_network_error, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btn_retry = view.findViewById(R.id.btn_retry_connection);
        mainActivity = (MainActivity)  getActivity();

        btn_retry.setOnClickListener(v -> {
            if(prevFragment.equals(getString(R.string.playlistFragment))){
                mainActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment_container, new PlaylistFragment(), "Playlist Fragment")
                        .commit();
            }else if(prevFragment.equals(getString(R.string.tracksFragment))){
                Fragment fragment = TracksFragment.newInstance(playlistId);
                mainActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment_container, fragment, "Tracks Fragment")
                        .commit();
            }
        });
    }
}