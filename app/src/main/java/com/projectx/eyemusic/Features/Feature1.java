package com.projectx.eyemusic.Features;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;
import com.projectx.eyemusic.R;


// TODO: complete the cropping
public class Feature1 extends RawFeature {
    Rect faceBoundingBox;
    Rect faceBoundingBoxSquared;
    FaceLandmark leftEyeLandmark;
    FaceLandmark rightEyeLandmark;

    private Bitmap faceImage;
    private Bitmap faceGrid;
    private Bitmap leftEyeImage;
    private Bitmap rightEyeImage;

    private boolean isFaceInImage;

    public Feature1(Bitmap b, Face face) {
        super(b, face);
        faceBoundingBox = face.getBoundingBox();
        leftEyeLandmark = face.getLandmark(FaceLandmark.LEFT_EYE);
        rightEyeLandmark = face.getLandmark(FaceLandmark.RIGHT_EAR);

        //initializing
        isFaceInImage = true;

        //checking of the bounding box is inside the picture
        if (faceBoundingBox.left < 0  || faceBoundingBox.top<0){
            isFaceInImage = false;
        }
        if (faceBoundingBox.right >= original.getWidth() || faceBoundingBox.bottom >= original.getHeight()){
            isFaceInImage = false;
        }


        //make the face Bounding Box square
        int width = original.getWidth();
        int height = original.getHeight();


        //create the bitmaps -> make the error if the the shapes are out of bound
    }

    // all the values are checked before hand so just the croping
    private void createBitmaps(int face2EyeRatio){
        faceImage = Bitmap.createBitmap(original, faceBoundingBoxSquared.left, faceBoundingBoxSquared.top, faceBoundingBoxSquared.width(), faceBoundingBoxSquared.height());

        PointF leftEyeCenter = leftEyeLandmark.getPosition();
        int leftEyeCenterX = ((int) leftEyeCenter.x);
        int leftEyeCenterY = ((int) leftEyeCenter.y);
        int leftEyeWidth = faceBoundingBoxSquared.width()/face2EyeRatio;
        int leftEyeHeight = faceBoundingBoxSquared.height()/face2EyeRatio;
        int leftEyeLeft = leftEyeCenterX-leftEyeWidth/2;
        int leftEyeTop = leftEyeCenterY-leftEyeHeight/2;
        rightEyeImage = Bitmap.createBitmap(original, leftEyeLeft, leftEyeTop, leftEyeWidth, leftEyeHeight);

        PointF rightEyeCenter = rightEyeLandmark.getPosition();
        int rightEyeCenterX = ((int) rightEyeCenter.x);
        int rightEyeCenterY = ((int) rightEyeCenter.y);
        int rightEyeWidth = faceBoundingBoxSquared.width()/face2EyeRatio;
        int rightEyeHeight = faceBoundingBoxSquared.height()/face2EyeRatio;
        int rightEyeLeft = rightEyeCenterX-rightEyeWidth/2;
        int rightEyeTop = rightEyeCenterY-rightEyeHeight/2;
        rightEyeImage = Bitmap.createBitmap(original, rightEyeLeft, rightEyeTop, rightEyeWidth, rightEyeHeight);
    }

}
