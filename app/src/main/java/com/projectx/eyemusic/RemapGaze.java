package com.projectx.eyemusic;

import android.widget.ProgressBar;

import com.projectx.eyemusic.Fragments.PlayerFragment;
import com.projectx.eyemusic.Model.GazePoint;

public class RemapGaze {
    public RemapGaze() {
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
        return point;
    }

    private GazePoint playerFragmentRemap(GazePoint point){
        GazePoint[] locations_player = PlayerFragment.getLocationButtons();
        //TODO complete
//        for(int i=0; i<5; i++){
//
//        }
        return locations_player[0];
    }
}
