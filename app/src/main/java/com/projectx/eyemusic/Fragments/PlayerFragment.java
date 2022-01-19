package com.projectx.eyemusic.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.Model.GazePoint;
import com.projectx.eyemusic.Music.MyTrack;
import com.projectx.eyemusic.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;


public class PlayerFragment extends Fragment {

    private static final String TAG = "PlayerFragment";
    private int played_index;
    private int shuffled_played_index;
    private ArrayList<MyTrack> tracks;
    private ArrayList<MyTrack> shuffled_tracks;
    private MyTrack track;

    long tracksecond;
    int progress;
    long currenttime;
    boolean repeat;
    boolean threadrepeat;
    boolean shuffle;
    boolean paused;

    ImageView albumart;
    TextView trackname;
    TextView artistname;
    ImageView tintView;
    SeekBar seekBar;

    static ImageButton btnplay;
    static ImageButton btnnext;
    static ImageButton btnprev;
    static ImageButton btnrepeat;
    static ImageButton btnshuffle;
    static GazePoint[] locations = new GazePoint[5];

    public PlayerFragment() {
        // Required empty public constructor
    }

    public static GazePoint[] getLocationButtons(){
        return locations;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // initialize view
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        // assign variables
        btnplay = view.findViewById(R.id.btnplay);
        btnnext = view.findViewById(R.id.btnnext);
        btnprev = view.findViewById(R.id.btnprev);
        btnrepeat = view.findViewById(R.id.btnrepeat);
        btnshuffle = view.findViewById(R.id.btnshuffle);


        if (getArguments() != null) {
            played_index = this.getArguments().getInt("played");
            tracks = new ArrayList<MyTrack>();

            for (Parcelable parcelable: this.getArguments().getParcelableArrayList("tracks")){
                tracks.add((MyTrack) parcelable);
            }
            track = tracks.get(played_index);
            shuffled_tracks = new ArrayList<MyTrack>(tracks);


        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumart = view.findViewById(R.id.iv_backart);
        Picasso.get().load(track.getImageURL()).into(albumart);
        trackname = view.findViewById(R.id.playersongname);
        trackname.setText(track.getTrackName());
        artistname = view.findViewById(R.id.playerartistname);
        artistname.setText(track.getArtistName());
        tintView = view.findViewById(R.id.iv_tint);
        seekBar =  view.findViewById(R.id.seekbar);


        paused = false;
        repeat = false;
        threadrepeat= false;
        progress = 0;
        tracksecond = track.getDuration_ms() / 100;
        seekBar.setVisibility(View.INVISIBLE);
        seekBar.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        new Thread((Runnable) () -> {
            while (progress < 100) {
                try {
                    while (paused);
                    seekBar.setProgress(progress);
                    progress++;
                    Thread.sleep(tracksecond);
                    if (progress == 100) {
                        if (!repeat) {
                            btnnext.post(() -> btnnext.performClick());
                            progress = 0;
                        }
                        else {
                            threadrepeat = true;
                            btnprev.post(() -> btnprev.performClick());
                            progress = 0;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        btnplay.setOnClickListener(v -> MainActivity.mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
            if (playerState.isPaused){
                MainActivity.mSpotifyAppRemote.getPlayerApi().resume();
                btnplay.setImageResource(R.drawable.ic_pause);

                paused = false;
            }
            else{
                MainActivity.mSpotifyAppRemote.getPlayerApi().pause();
                btnplay.setImageResource(R.drawable.ic_play);
                paused = true;
            }
        }));
        MainActivity.buttoneffect(btnplay);

        btnnext.setOnClickListener(v -> {
            if (shuffle){
                shuffled_played_index++;
                if (shuffled_played_index == tracks.size() - 1){
                    shuffled_played_index = 0;
                }
                track = shuffled_tracks.get(shuffled_played_index);
            }
            else{
                played_index++;
                if (played_index == tracks.size() - 1){
                    played_index = 0;
                }
                track = tracks.get(played_index);
            }
            MainActivity.mSpotifyAppRemote.getPlayerApi().play(track.getSpotifyURI());
            Picasso.get().load(track.getImageURL()).into(albumart);
            trackname.setText(track.getTrackName());
            artistname.setText(track.getArtistName());
            seekBar.setProgress(0);
            progress = 0;
            tracksecond = track.getDuration_ms()/100;
            btnplay.setImageResource(R.drawable.ic_pause);
            paused = false;

        });
        MainActivity.buttoneffect(btnnext);

        btnprev.setOnClickListener(v -> {
            MainActivity.mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playback->{
               int index;
                if (shuffle)
                    index = shuffled_played_index;
                else
                    index  = played_index;
               if (playback.playbackPosition > 5000 || index==0 || threadrepeat){
                   MainActivity.mSpotifyAppRemote.getPlayerApi().play(track.getSpotifyURI());
                   seekBar.setProgress(0);
                   progress = 0;
                   if (paused) {
                       btnplay.setImageResource(R.drawable.ic_pause);
                       paused = false;
                   }
                   if (threadrepeat) threadrepeat = false;
               }
               else{
                   if (shuffle){
                       shuffled_played_index--;
                       track = shuffled_tracks.get(shuffled_played_index);
                   }
                   else{
                       played_index--;
                       track = tracks.get(played_index);
                   }
                   MainActivity.mSpotifyAppRemote.getPlayerApi().play(track.getSpotifyURI());
                   Picasso.get().load(track.getImageURL()).into(albumart);
                   trackname.setText(track.getTrackName());
                   artistname.setText(track.getArtistName());
                   seekBar.setProgress(0);
                   progress = 0;
                   tracksecond = track.getDuration_ms()/100;
                   btnplay.setImageResource(R.drawable.ic_pause);
                   paused = false;
               }
            });
        });
        MainActivity.buttoneffect(btnprev);

        btnrepeat.setOnClickListener(v -> {
            if (!repeat){
                btnrepeat.setImageResource(R.drawable.ic_repeat_glow);
                repeat = true;
            }
            else{
                btnrepeat.setImageResource(R.drawable.ic_repeat);
                repeat = false;
            }
        });
        MainActivity.buttoneffect(btnrepeat);

        btnshuffle.setOnClickListener(v -> {
            if (!shuffle){
                btnshuffle.setImageResource(R.drawable.ic_shuffle_glow);
                shuffle = true;
                Collections.shuffle(shuffled_tracks);
                int i = shuffled_tracks.indexOf(track);
                Collections.swap(shuffled_tracks,0,i);
                shuffled_played_index = 0;
            }
            else{
                btnshuffle.setImageResource(R.drawable.ic_shuffle);
                shuffle = false;
                played_index = tracks.indexOf(track);
            }
        });
        MainActivity.buttoneffect(btnshuffle);

        //remap
        int[] tmp_location = new int[2];

        btnplay.getLocationOnScreen(tmp_location);
        locations[0] = new GazePoint(tmp_location[0], tmp_location[1]);

        btnnext.getLocationOnScreen(tmp_location);
        locations[1] = new GazePoint(tmp_location[0], tmp_location[1]);

        btnprev.getLocationOnScreen(tmp_location);
        locations[2] = new GazePoint(tmp_location[0], tmp_location[1]);

        btnrepeat.getLocationOnScreen(tmp_location);
        locations[3] = new GazePoint(tmp_location[0], tmp_location[1]);

        btnshuffle.getLocationOnScreen(tmp_location);
        locations[4] = new GazePoint(tmp_location[0], tmp_location[1]);
    }

}