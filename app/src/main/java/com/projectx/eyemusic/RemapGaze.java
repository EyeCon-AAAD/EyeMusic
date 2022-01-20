package com.projectx.eyemusic;

import android.graphics.Color;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.projectx.eyemusic.Fragments.PlayerFragment;
import com.projectx.eyemusic.Model.GazePoint;

public class RemapGaze {
    GazePoint[] locations_player;
    ImageButton[] references_player;

    static GazePoint[] locations_menu_button = MainActivity.getLocationsMenuButtons();
    static ImageButton[] references_menu_button = MainActivity.getReferencesMenuButtons();


    GazePoint[] all_locations;
    ImageButton[] all_references;
    public RemapGaze() {
        locations_player = PlayerFragment.getLocationButtons();
        references_player = PlayerFragment.getLocationReferences();

        int player_length = locations_player.length;
        int menu_button_length = locations_menu_button.length;

        all_locations = new GazePoint[player_length+menu_button_length];
        System.arraycopy(locations_player, 0, all_locations, 0, player_length);
        System.arraycopy(locations_menu_button, 0, all_locations, player_length, menu_button_length);

        all_references = new ImageButton[player_length+ menu_button_length];
        System.arraycopy(references_player, 0, all_references, 0, player_length);
        System.arraycopy(references_menu_button, 0, all_references, player_length, menu_button_length);
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
        if(MainActivity.playerFragment != null && !MainActivity.playerFragment.isHidden()){
            return playerFragmentRemap(point);
        }
        //TODO for other fragments
        return null;
    }

    private GazePoint playerFragmentRemap(GazePoint point){

        int nearest_index =  getNearestIndex(all_locations, point);


        if(nearest_index==-1) return null;
        else{
            removeColorFilters(nearest_index);
            all_references[nearest_index].post(()->{
                all_references[nearest_index].setColorFilter(Color.parseColor("#8800FF00"));
                //all_references[nearest_index].setBackgroundColor(Color.parseColor("#8800FF00"));
            });
            return all_locations[nearest_index];
        }

    }

    private void removeColorFilters(int exception){
        for (int i = 0; i< all_references.length; i++){
            ImageButton ib = all_references[i];
            if(ib!=null && i!=exception)
                ib.setColorFilter(null);
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
