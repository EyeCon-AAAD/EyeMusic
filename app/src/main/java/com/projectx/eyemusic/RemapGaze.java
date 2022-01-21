package com.projectx.eyemusic;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.Image;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.projectx.eyemusic.Fragments.PlayerFragment;
import com.projectx.eyemusic.Fragments.PlaylistFragment;
import com.projectx.eyemusic.Fragments.TracksFragment;
import com.projectx.eyemusic.Model.GazePoint;
import com.projectx.eyemusic.Music.PlaylistAdapter;

import java.util.ArrayList;

public class RemapGaze {
    private static final String TAG = "RemapGaze";
    static View prev_highlight = null;
    static Boolean prev_text = Boolean.FALSE;
    static RecyclerView prev_highlight_rc = null;

    //player fragment
    GazePoint[] locations_player;
    View[] references_player;

    //back and player menu buttons
    GazePoint[] locations_menu_button;
    View[] references_menu_button;

    // up and down buttons and recycler view in playlist fragment
    GazePoint[] locations_playlist;
    View[] references_playlist;
    int[] location_rv_playlist;
    RecyclerView reference_rv_playlist;

    //playlist items
    View[] references_playlist_items;

    // up and down buttons and recycler view in tracks fragment
    GazePoint[] locations_track;
    View[] references_track;
    int[] location_rv_track;
    RecyclerView reference_rv_track;


    public RemapGaze() {
        locations_player = PlayerFragment.getLocationButtons();
        references_player = PlayerFragment.getLocationReferences();

        locations_menu_button = MainActivity.getLocationsMenuButtons();
        references_menu_button = MainActivity.getReferencesMenuButtons();

        PlaylistFragment.updatePlaylistItemReferences();
        locations_playlist = PlaylistFragment.getLocationsPlaylist();
        references_playlist = PlaylistFragment.getReferencesPlaylist();
        location_rv_playlist = PlaylistFragment.getLocation_rv();
        reference_rv_playlist = PlaylistFragment.getReference_rv();

        ArrayList<View> items = PlaylistAdapter.getReferencesPlaylistItems();
        references_playlist_items = new View[items.size()];
        references_playlist_items = items.toArray(references_playlist_items);


        locations_track = TracksFragment.getLocations_button();
        references_track = TracksFragment.getReferences_button();
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
        /*if(point.getY()>=location_rv_playlist[0] && point.getY()<=location_rv_playlist[1]){
            //remove the previous highlights
            removePrevColorFilter(null);
            removeRCColorFilter(reference_rv_playlist);

            //hightlight the new one
            if(references_playlist!=null){
                reference_rv_playlist.post(()->{
                    reference_rv_playlist.getBackground().setColorFilter(Color.parseColor("#8800FF00"), PorterDuff.Mode.LIGHTEN);
                });
            }

            //set new prevs
            prev_highlight_rc = reference_rv_playlist;
            prev_highlight = null;
            return point;
        }*/

        //if it is near the buttons
        GazePoint[] all_locations_playlist;
        View[] all_references_playlist;

        int playlist_length = locations_playlist.length;
        int menu_button_length = locations_menu_button.length;
        int playlist_items_length = references_playlist_items.length;

        all_locations_playlist = new GazePoint[playlist_length+menu_button_length];
        System.arraycopy(locations_playlist, 0, all_locations_playlist, 0, playlist_length);
        System.arraycopy(locations_menu_button, 0, all_locations_playlist, playlist_length, menu_button_length);

        all_references_playlist = new View[playlist_length+ menu_button_length+playlist_items_length];
        System.arraycopy(references_playlist, 0, all_references_playlist, 0, playlist_length);
        System.arraycopy(references_menu_button, 0, all_references_playlist, playlist_length, menu_button_length);
        System.arraycopy(references_playlist_items, 0, all_references_playlist, menu_button_length, playlist_items_length);

        int nearest_index =  getNearestIndex(all_locations_playlist, point);
        Log.d(TAG, "nearest_index: " + nearest_index);
        // if a near button found
        if(nearest_index!=-1){
            removePrevColorFilter(all_references_playlist[nearest_index]);
            removeRCColorFilter(null);

            if(nearest_index == 2 || nearest_index== 3){
                prev_text = Boolean.TRUE;
                all_references_playlist[nearest_index].post(()->{
                    addColorFilter(all_references_playlist[nearest_index], Boolean.TRUE);
                    ((TextView)all_references_playlist[nearest_index]).setTextColor(Color.GREEN);

                });
            }else{
                prev_text = Boolean.FALSE;
                all_references_playlist[nearest_index].post(()->{
                    addColorFilter(all_references_playlist[nearest_index], Boolean.FALSE);
                });
            }


            prev_highlight = all_references_playlist[nearest_index];
            prev_highlight_rc = null;

            return all_locations_playlist[nearest_index];
        }
        // if a near button not found
        else{
            removePrevColorFilter(null);
            removeRCColorFilter(null);

            prev_highlight = null;
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
                    reference_rv_track.getBackground().setColorFilter(Color.parseColor("#DD00FF00"), PorterDuff.Mode.LIGHTEN);
                });
            }

            //set new prevs
            prev_highlight_rc = reference_rv_track;
            prev_highlight = null;
            return point;
        }

        //if it is near the buttons
        GazePoint[] all_locations_track;
        View[] all_references_track;

        int track_length = locations_track.length;
        int menu_button_length = locations_menu_button.length;

        all_locations_track = new GazePoint[track_length+menu_button_length];
        System.arraycopy(locations_track, 0, all_locations_track, 0, track_length);
        System.arraycopy(locations_menu_button, 0, all_locations_track, track_length, menu_button_length);

        all_references_track = new View[track_length+ menu_button_length];
        System.arraycopy(references_track, 0, all_references_track, 0, track_length);
        System.arraycopy(references_menu_button, 0, all_references_track, track_length, menu_button_length);

        int nearest_index =  getNearestIndex(all_locations_track, point);

        // if a near button found
        if(nearest_index!=-1){
            removePrevColorFilter(all_references_track[nearest_index]);
            removeRCColorFilter(null);

            all_references_track[nearest_index].post(()->{
                addColorFilter(all_references_track[nearest_index], Boolean.FALSE);
            });

            prev_highlight = all_references_track[nearest_index];
            prev_highlight_rc = null;

            return all_locations_track[nearest_index];
        }
        // if a near button not found
        else{
            removePrevColorFilter(null);
            removeRCColorFilter(null);

            prev_highlight = null;
            prev_highlight_rc = null;

            return null;
        }
    }

    private GazePoint playerFragmentRemap(GazePoint point){
        GazePoint[] all_locations_player;
        View[] all_references_player;

        int player_length = locations_player.length;
        int menu_button_length = locations_menu_button.length;

        all_locations_player = new GazePoint[player_length+menu_button_length];
        System.arraycopy(locations_player, 0, all_locations_player, 0, player_length);
        System.arraycopy(locations_menu_button, 0, all_locations_player, player_length, menu_button_length);

        all_references_player = new View[player_length+ menu_button_length];
        System.arraycopy(references_player, 0, all_references_player, 0, player_length);
        System.arraycopy(references_menu_button, 0, all_references_player, player_length, menu_button_length);

        int nearest_index =  getNearestIndex(all_locations_player, point);


        if(nearest_index==-1) return null;
        else{
            //removeColorFilters(nearest_index);
            removePrevColorFilter(all_references_player[nearest_index]);
            all_references_player[nearest_index].post(()->{
                addColorFilter(all_references_player[nearest_index], Boolean.FALSE);
            });
            prev_highlight = all_references_player[nearest_index];
            return all_locations_player[nearest_index];
        }

    }

    private void addColorFilter(View v, Boolean is_text_view){
        if(is_text_view){
            ((TextView) v).setTextColor(Color.parseColor("#8800FF00"));
        }else{
            if(v.getForeground()!=null){
                v.getForeground().setColorFilter(Color.parseColor("#8800FF00"), PorterDuff.Mode.LIGHTEN);
            }else if (v.getBackground()!=null){
                v.getBackground().setColorFilter(Color.parseColor("#8800FF00"), PorterDuff.Mode.DARKEN);
            }else{
                ImageButton imageButton = (ImageButton) v;
                imageButton.setColorFilter(Color.parseColor("#8800FF00"));
            }
        }

    }

    private void removeRCColorFilter(RecyclerView exception_rc){
        if(prev_highlight_rc != null && prev_highlight_rc!=exception_rc)
            prev_highlight_rc.getBackground().setColorFilter(null);
    }

    private void removePrevColorFilter(View exception_btn){
        if(prev_text && prev_highlight !=null && exception_btn!= prev_highlight){
            ((TextView) prev_highlight).setTextColor(Color.WHITE);
            return;
        }
        //else
        if(prev_highlight !=null && exception_btn!= prev_highlight){
            if(prev_highlight.getForeground() != null){
                prev_highlight.getForeground().setColorFilter(null);
            }else if (prev_highlight.getBackground() != null){
                prev_highlight.getBackground().setColorFilter(null);
            }else{
                ImageButton imageButton = (ImageButton) prev_highlight;
                imageButton.setColorFilter(null);
            }
        }
    }


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
