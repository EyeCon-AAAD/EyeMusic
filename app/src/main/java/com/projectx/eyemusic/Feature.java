package com.projectx.eyemusic;

import android.graphics.Bitmap;

//TODO: complete this class
public class Feature {
    private int x_coordinate;
    private int y_coordinate;
    public Bitmap original;
    public float smileProbability;

    public Feature(){};

    public Feature(Bitmap b, float smileProbability) {
        this.original = b;
        this.smileProbability = smileProbability;
    }
    public String toString() {
        return "x:" + x_coordinate + " , " + "y:" + y_coordinate;
    }

    //------------------------- SETTERS ----------------------------------------
    public void setX_coordinate(int x_coordinate) {
        this.x_coordinate = x_coordinate;
    }

    public void setY_coordinate(int y_coordinate) {
        this.y_coordinate = y_coordinate;
    }

    public void setXY_coordinates(int x, int y){
        this.x_coordinate = x;
        this.y_coordinate = y;
    }

    public void setOriginal(Bitmap original) {
        this.original = original;
    }

    public void setSmileProbability(float smileProbability) {
        this.smileProbability = smileProbability;
    }

}
