package com.projectx.eyemusic;

import android.graphics.Bitmap;

//TODO: complete this class
public class Feature {

    public Bitmap original;
    public float smileProbability;

    public Feature(){};

    public Feature(Bitmap b, float smileProbability) {
        this.original = b;
        this.smileProbability = smileProbability;
    }

}
