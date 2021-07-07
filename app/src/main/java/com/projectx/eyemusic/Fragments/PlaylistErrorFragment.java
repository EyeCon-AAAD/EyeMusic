package com.projectx.eyemusic.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.projectx.eyemusic.R;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlaylistErrorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaylistErrorFragment extends Fragment {

    // views
    Button btnRetryFetchPlaylists;

    public PlaylistErrorFragment() {
        // Required empty public constructor
    }

    public static PlaylistErrorFragment newInstance(String param1, String param2) {
        return new PlaylistErrorFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist_error, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnRetryFetchPlaylists = view.findViewById(R.id.btn_retry_fetch_playlists);
        btnRetryFetchPlaylists.setOnClickListener(v->{
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, new PlaylistFragment())
                    .commit();
        });
    }
}