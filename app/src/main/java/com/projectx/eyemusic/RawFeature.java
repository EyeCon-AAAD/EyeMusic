package com.projectx.eyemusic;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;

//TODO: complete this class
public class RawFeature {
    private int x_coordinate;
    private int y_coordinate;
    public Bitmap original;
    private Face face;
    public float smileProbability;

    public RawFeature(){};

    public RawFeature(Bitmap b, Face face) {
        this.original = b;
        this.smileProbability = smileProbability;
        /*Rect faceBoundingBox = dominantFace.getBoundingBox();
        FaceLandmark leftEyeLandmark = dominantFace.getLandmark(FaceLandmark.LEFT_EYE);
        FaceLandmark rightEyeLandmark = dominantFace.getLandmark(FaceLandmark.RIGHT_EAR);*/

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
