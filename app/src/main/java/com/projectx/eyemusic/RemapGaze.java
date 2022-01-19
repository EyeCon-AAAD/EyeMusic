package com.projectx.eyemusic;

import android.util.Log;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.projectx.eyemusic.Fragments.PlayerFragment;
import com.projectx.eyemusic.Model.GazePoint;

public class RemapGaze {
    GazePoint[] locations_player;
    GazePoint[] locations_menu_button = MainActivity.getLocations_menu_buttons();
    public RemapGaze() {
        locations_player = PlayerFragment.getLocationButtons();
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
        GazePoint[] all_locations = new GazePoint[locations_player.length+locations_menu_button.length];
        System.arraycopy(locations_player, 0, all_locations, 0, locations_player.length);
        System.arraycopy(locations_menu_button, 0, all_locations, locations_player.length, locations_menu_button.length);

        return getNearest(all_locations, point);
    }

    private GazePoint getNearest(GazePoint[] points, GazePoint desired_point){
        double smallest_distance=-1;
        int smallest_index = -1;
        double tmp_distance;

        if(points[0] != null){
            smallest_distance = calculateDistance(points[0], desired_point);
            smallest_index = 0;
        }

        for(int i=1; i<5; i++){
            if(points[i] != null){
                tmp_distance = calculateDistance(points[i], desired_point);
                if(tmp_distance<smallest_distance){
                    smallest_distance = tmp_distance;
                    smallest_index = i;
                }
            }
        }

        if(smallest_index == -1) return null;
        return points[smallest_index];
    }

    private double calculateDistance(@NonNull GazePoint point1,@NonNull GazePoint point2){
        return Math.pow(Math.pow((point1.getX()-point2.getX()), 2) + Math.pow((point1.getY()-point2.getY()), 2), 0.5);
    }
}
