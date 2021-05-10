package com.projectx.eyemusic;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Random;

public class GazeModel {
    private static final String TAG = "GazeModel";
    private final int width = Utilities.getScreenWidth();
    private final int height = Utilities.getScreenHeight();

    Random rand;
    DisplayMetrics displayMetrics;
    Context context;

    GazeModel(){
        rand = new Random();
    }

    public  GazePoint predict(Feature feature){

        int x = rand.nextInt(width);
        int y = rand.nextInt(height);
        return new GazePoint(x, y);
    }

    public static class GazePoint{
        float x;
        float y;

        GazePoint(float x, float y){
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }
}
