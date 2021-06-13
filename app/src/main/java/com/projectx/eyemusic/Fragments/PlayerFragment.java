package com.projectx.eyemusic.Fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.util.AttributeSet;
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
import com.projectx.eyemusic.Music.MyTrack;
import com.projectx.eyemusic.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class PlayerFragment extends Fragment {

    private static final String TAG = "PlayerFragment";
    private int played_index;
    private ArrayList<MyTrack> tracks;
    private MyTrack track;
    ImageButton btnplay;
    ImageButton btnnext;
    ImageButton btnprev;
    ImageButton btnrepeat;
    ImageButton btnshuffle;
    ImageView albumart;
    TextView trackname;
    TextView artistname;
    SeekBar seekBar;
    long tracksecond;
    int progress;
    long currenttime;
    boolean repeat;
    boolean threadrepeat;
    boolean shuffle;
    boolean paused;


    public PlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getArguments() != null) {
            played_index = this.getArguments().getInt("played");
            tracks = new ArrayList<MyTrack>();
            for (Parcelable parcelable: this.getArguments().getParcelableArrayList("tracks")){
                tracks.add((MyTrack) parcelable);
            }
            track = tracks.get(played_index);
   /*         for (int i = played_index + 1; i < tracks.size(); i++){
                MainActivity.mSpotifyAppRemote.getPlayerApi().queue(tracks.get(i).getSpotifyURI());
            }*/

        }

        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumart = view.findViewById(R.id.imgalbumart);
        Picasso.get().load(track.getImageURL()).into(albumart);
        trackname = view.findViewById(R.id.playersongname);
        trackname.setText(track.getTrackName());
        artistname = view.findViewById(R.id.playerartistname);
        artistname.setText(track.getArtistName());
        seekBar =  view.findViewById(R.id.seekbar);
        btnplay = view.findViewById(R.id.btnplay);
        btnnext = view.findViewById(R.id.btnnext);
        btnprev = view.findViewById(R.id.btnprev);
        btnrepeat = view.findViewById(R.id.btnrepeat);
        btnshuffle = view.findViewById(R.id.btnshuffle);
        paused = false;
        repeat = false;
        threadrepeat= false;
        progress = 0;

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
        buttoneffect(btnplay);


        tracksecond = tracks.get(played_index).getDuration_ms() / 100;
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
                    if (progress == 100) {
                        if (!repeat)
                            btnnext.post(new Runnable() {
                                @Override
                                public void run() {
                                    btnnext.performClick();
                                }
                            });
                        else {
                            threadrepeat = true;
                            btnprev.post(new Runnable() {
                                @Override
                                public void run() {
                                    btnprev.performClick();
                                }
                            });
                        }
                    }
                    Thread.sleep(tracksecond);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        btnnext.setOnClickListener(v -> {
            if (played_index == tracks.size() - 1)
                played_index = 0;
            else
                played_index++;
            track = tracks.get(played_index);
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
        buttoneffect(btnnext);

        btnprev.setOnClickListener(v -> {
            MainActivity.mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playback->{
               if (playback.playbackPosition > 5000 || played_index==0 || threadrepeat){
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
                   played_index--;
                   track = tracks.get(played_index);
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
        buttoneffect(btnprev);

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

    }

    public static void buttoneffect (View button){
        button.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.getBackground().setColorFilter(0x69696969,PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }
}