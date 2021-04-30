package com.projectx.eyemusic;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Random;

public class GazeModel {
    private static final String TAG = "GazeModel";

    Random rand;
    DisplayMetrics displayMetrics;
    Context context;

    GazeModel(){
        //this.context = context;
        //context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        rand = new Random();
        displayMetrics = new DisplayMetrics();


    }

    public  GazePoint predict(){
        int width = Utilities.getScreenWidth();
        int height = Utilities.getScreenHeight();

        Log.i(TAG, "screenWidthInPixels: " + width);
        Log.i(TAG, "screenHeightInPixels: " + height);
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
