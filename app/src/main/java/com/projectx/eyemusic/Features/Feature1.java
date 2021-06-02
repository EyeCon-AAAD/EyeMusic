package com.projectx.eyemusic.Features;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;
import com.projectx.eyemusic.R;


// TODO: complete the cropping
public class Feature1 extends RawFeature {
    Rect faceBoundingBox;
    FaceLandmark leftEyeLandmark;
    FaceLandmark rightEyeLandmark;

    private Bitmap faceImage;
    private Bitmap faceGrid;
    private Bitmap eyeLeftImage;
    private Bitmap eyeRightImage;

    public Feature1(Bitmap b, Face face) {
        super(b, face);
        faceBoundingBox = face.getBoundingBox();
        leftEyeLandmark = face.getLandmark(FaceLandmark.LEFT_EYE);
        rightEyeLandmark = face.getLandmark(FaceLandmark.RIGHT_EYE);
    }

    public Bitmap getFaceImage() {
        return faceImage;
    }

    public Bitmap getFaceGrid() {
        return faceGrid;
    }

    public Bitmap getEyeLeftImage() {
        return eyeLeftImage;
    }

    public Bitmap getEyeRightImage() {
        return eyeRightImage;
    }
}

