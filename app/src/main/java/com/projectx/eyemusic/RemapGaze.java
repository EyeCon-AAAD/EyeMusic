package com.projectx.eyemusic;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.projectx.eyemusic.Fragments.PlayerFragment;
import com.projectx.eyemusic.Fragments.PlaylistFragment;
import com.projectx.eyemusic.Fragments.TracksFragment;
import com.projectx.eyemusic.Model.GazePoint;

public class RemapGaze {
    static ImageButton prev_highlight_button;
    static RecyclerView prev_highlight_rc;

    //player fragment
    GazePoint[] locations_player;
    ImageButton[] references_player;

    //back and player menu buttons
    GazePoint[] locations_menu_button;
    ImageButton[] references_menu_button;

    // up and down buttons and recycler view in playlist fragment
    GazePoint[] locations_playlist;
    ImageButton[] references_playlist;
    int[] location_rv_playlist;
    RecyclerView reference_rv_playlist;

    // up and down buttons and recycler view in tracks fragment
    GazePoint[] locations_track;
    ImageButton[] references_track;
    int[] location_rv_track;
    RecyclerView reference_rv_track;


    public RemapGaze() {
        locations_player = PlayerFragment.getLocationButtons();
        references_player = PlayerFragment.getLocationReferences();

        locations_menu_button = MainActivity.getLocationsMenuButtons();
        references_menu_button = MainActivity.getReferencesMenuButtons();

        locations_playlist = PlaylistFragment.getLocations_button();
        references_playlist = (ImageButton[]) PlaylistFragment.getReferences_button();
        location_rv_playlist = PlaylistFragment.getLocation_rv();
        reference_rv_playlist = PlaylistFragment.getReference_rv();

        locations_track = TracksFragment.getLocations_button();
        references_track = (ImageButton[]) TracksFragment.getReferences_button();
        location_rv_track = TracksFragment.getLocation_rv();
        reference_rv_track = TracksFragment.getReference_rv();

    }
    public Boolean needRemap(){
        //user in player fragment
        if(MainActivity.playerFragment != null && !MainActivity.playerFragment.isHidden()){
            return Boolean.TRUE;
        }else if (MainActivity.currentFragment != null){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }


    public GazePoint remap(GazePoint point){
        if(MainActivity.playerFragment != null && !MainActivity.playerFragment.isHidden()){ // in the player fragment
            return playerFragmentRemap(point);
        }
        else if (MainActivity.currentFragment != null && MainActivity.currentFragment.getTag().equals("Tracks Fragment")){
            Log.d("fragmentDetect", "remap: its in the tracks");
            return trackFragmentRemap(point);
        }
        else if (MainActivity.currentFragment != null){
            Log.d("fragmentDetect", "remap: its in the playlist");
            return playlistFragmentRemap(point);
        }

        return null;
    }


    private GazePoint playlistFragmentRemap(GazePoint point){
        //if the point is inside the recycle view
        if(point.getY()>=location_rv_playlist[0] && point.getY()<=location_rv_playlist[1]){
            //remove the previous highlights
            removePrevColorFilter(null);
            removeRCColorFilter(reference_rv_playlist);

            //hightlight the new one
            if(references_playlist!=null){
                reference_rv_playlist.post(()->{
                    reference_rv_playlist.getBackground().setColorFilter(Color.parseColor("#8800FF00"), PorterDuff.Mode.DARKEN);
                });
            }

            //set new prevs
            prev_highlight_rc = reference_rv_playlist;
            prev_highlight_button = null;
            return point;
        }

        //if it is near the buttons
        GazePoint[] all_locations_playlist;
        ImageButton[] all_references_playlist;

        int playlist_length = locations_playlist.length;
        int menu_button_length = locations_menu_button.length;

        all_locations_playlist = new GazePoint[playlist_length+menu_button_length];
        System.arraycopy(locations_playlist, 0, all_locations_playlist, 0, playlist_length);
        System.arraycopy(locations_menu_button, 0, all_locations_playlist, playlist_length, menu_button_length);

        all_references_playlist = new ImageButton[playlist_length+ menu_button_length];
        System.arraycopy(references_playlist, 0, all_references_playlist, 0, playlist_length);
        System.arraycopy(references_menu_button, 0, all_references_playlist, playlist_length, menu_button_length);

        int nearest_index =  getNearestIndex(all_locations_playlist, point);

        // if a near button found
        if(nearest_index!=-1){
            removePrevColorFilter(all_references_playlist[nearest_index]);
            removeRCColorFilter(null);

            all_references_playlist[nearest_index].post(()->{
                all_references_playlist[nearest_index].setColorFilter(Color.parseColor("#8800FF00"));
            });

            prev_highlight_button = all_references_playlist[nearest_index];
            prev_highlight_rc = null;

            return all_locations_playlist[nearest_index];
        }
        // if a near button not found
        else{
            removePrevColorFilter(null);
            removeRCColorFilter(null);

            prev_highlight_button = null;
            prev_highlight_rc = null;

            return null;
        }
    }

    private GazePoint trackFragmentRemap(GazePoint point){
        //if the point is inside the recycle view
        if(point.getY()>=location_rv_track[0] && point.getY()<=location_rv_track[1]){
            //remove the previous highlights
            removePrevColorFilter(null);
            removeRCColorFilter(reference_rv_track);

            //hightlight the new one
            if(reference_rv_track!=null){
                reference_rv_track.post(()->{
                    reference_rv_track.getBackground().setColorFilter(Color.parseColor("#8800FF00"), PorterDuff.Mode.DARKEN);
                });
            }

            //set new prevs
            prev_highlight_rc = reference_rv_track;
            prev_highlight_button = null;
            return point;
        }

        //if it is near the buttons
        GazePoint[] all_locations_track;
        ImageButton[] all_references_track;

        int track_length = locations_track.length;
        int menu_button_length = locations_menu_button.length;

        all_locations_track = new GazePoint[track_length+menu_button_length];
        System.arraycopy(locations_track, 0, all_locations_track, 0, track_length);
        System.arraycopy(locations_menu_button, 0, all_locations_track, track_length, menu_button_length);

        all_references_track = new ImageButton[track_length+ menu_button_length];
        System.arraycopy(references_track, 0, all_references_track, 0, track_length);
        System.arraycopy(references_menu_button, 0, all_references_track, track_length, menu_button_length);

        int nearest_index =  getNearestIndex(all_locations_track, point);

        // if a near button found
        if(nearest_index!=-1){
            removePrevColorFilter(all_references_track[nearest_index]);
            removeRCColorFilter(null);

            all_references_track[nearest_index].post(()->{
                all_references_track[nearest_index].setColorFilter(Color.parseColor("#8800FF00"));
            });

            prev_highlight_button = all_references_track[nearest_index];
            prev_highlight_rc = null;

            return all_locations_track[nearest_index];
        }
        // if a near button not found
        else{
            removePrevColorFilter(null);
            removeRCColorFilter(null);

            prev_highlight_button = null;
            prev_highlight_rc = null;

            return null;
        }
    }

    private GazePoint playerFragmentRemap(GazePoint point){
        GazePoint[] all_locations_player;
        ImageButton[] all_references_player;

        int player_length = locations_player.length;
        int menu_button_length = locations_menu_button.length;

        all_locations_player = new GazePoint[player_length+menu_button_length];
        System.arraycopy(locations_player, 0, all_locations_player, 0, player_length);
        System.arraycopy(locations_menu_button, 0, all_locations_player, player_length, menu_button_length);

        all_references_player = new ImageButton[player_length+ menu_button_length];
        System.arraycopy(references_player, 0, all_references_player, 0, player_length);
        System.arraycopy(references_menu_button, 0, all_references_player, player_length, menu_button_length);

        int nearest_index =  getNearestIndex(all_locations_player, point);


        if(nearest_index==-1) return null;
        else{
            //removeColorFilters(nearest_index);
            removePrevColorFilter(all_references_player[nearest_index]);
            all_references_player[nearest_index].post(()->{
                all_references_player[nearest_index].setColorFilter(Color.parseColor("#8800FF00"));
            });
            prev_highlight_button = all_references_player[nearest_index];
            return all_locations_player[nearest_index];
        }

    }
    private void removeRCColorFilter(RecyclerView exception_rc){
        if(prev_highlight_rc != null && prev_highlight_rc!=exception_rc)
            prev_highlight_rc.getBackground().setColorFilter(null);
    }

    private void removePrevColorFilter(ImageButton exception_btn){
        if(prev_highlight_button !=null && exception_btn!= prev_highlight_button){
            prev_highlight_button.setColorFilter(null);
        }
    }

//    private void removeColorFilters(int exception){
//        for (int i = 0; i< all_references.length; i++){
//            ImageButton ib = all_references[i];
//            if(ib!=null && i!=exception)
//                ib.setColorFilter(null);
//        }
//    }

    private int getNearestIndex(GazePoint[] points, GazePoint desired_point){
        double smallest_distance=-1;
        int smallest_index = -1;
        double tmp_distance;

        if(points[0] != null){
            smallest_distance = calculateDistance(points[0], desired_point);
            smallest_index = 0;
        }

        for(int i=1; i< points.length; i++){
            if(points[i] != null){
                tmp_distance = calculateDistance(points[i], desired_point);
                if(tmp_distance<smallest_distance){
                    smallest_distance = tmp_distance;
                    smallest_index = i;
                }
            }
        }

        return smallest_index;
    }

    private double calculateDistance(@NonNull GazePoint point1,@NonNull GazePoint point2){
        return Math.pow(Math.pow((point1.getX()-point2.getX()), 2) + Math.pow((point1.getY()-point2.getY()), 2), 0.5);
    }
}
