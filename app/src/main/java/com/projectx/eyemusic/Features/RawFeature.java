package com.projectx.eyemusic.Features;

import android.graphics.Bitmap;

import com.google.mlkit.vision.face.Face;
import com.projectx.eyemusic.Model.GazeModelManager;
import com.projectx.eyemusic.Model.GazePoint;
import com.spotify.android.appremote.internal.Validate;

//TODO: complete this class
public class RawFeature {
    protected GazePoint coordinate;
    protected Bitmap original;
    protected Face face;
    protected float smileProb;

    public RawFeature(Bitmap b, Face face) {
        Validate.checkNotNull(b);
        Validate.checkNotNull(face);
        this.original = b;
        this.face = face;
        this.smileProb = face.getSmilingProbability();
    }
    public String toString() {
        return "x:" + coordinate.getX() + " , " + "y:" + coordinate.getY();
    }

    //------------------------- SETTERS ----------------------------------------

    public void setCoordinate(GazePoint coordinates) {
        Validate.checkNotNull(coordinates);
        this.coordinate = coordinates;
    }

    public GazePoint getCoordinate() {
        return coordinate;
    }

    public Bitmap getOriginal() {
        return original;
    }

    public Face getFace() {
        return face;
    }

    public float getSmileProb() {
        return smileProb;
    }
}
