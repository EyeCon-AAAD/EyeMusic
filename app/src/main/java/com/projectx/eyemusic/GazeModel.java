package com.projectx.eyemusic;

import java.util.Random;

public class GazeModel {
    private static final String TAG = "GazeModel";
    private final int SCREEN_WIDTH = Utilities.getScreenWidth();
    private final int SCREEN_HEIGHT = Utilities.getScreenHeight();
    Random rand;

    GazeModel(){
        rand = new Random();
    }

    public  GazePoint predict(RawFeature feature){

        int x = rand.nextInt(SCREEN_WIDTH);
        int y = rand.nextInt(SCREEN_HEIGHT);
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
