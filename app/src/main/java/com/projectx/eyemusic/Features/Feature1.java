package com.projectx.eyemusic.Features;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;
import com.spotify.android.appremote.internal.Validate;

import java.util.Locale;


public class Feature1 extends RawFeature {
    private static final String TAG = "Feature1";
    Rect faceBoundingBox;
    Rect faceBoundingBoxSquared;
    FaceLandmark leftEyeLandmark;
    FaceLandmark rightEyeLandmark;

    private Bitmap faceImage;
    private int[][] faceGrid;
    private Bitmap leftEyeImage;
    private Bitmap rightEyeImage;

    private boolean faceInImage;

    public Feature1(Bitmap b, Face face) {
        super(b, face);
        Validate.checkNotNull(b);
        Validate.checkNotNull(face);

        faceBoundingBox = face.getBoundingBox();
        leftEyeLandmark = face.getLandmark(FaceLandmark.LEFT_EYE);
        rightEyeLandmark = face.getLandmark(FaceLandmark.RIGHT_EYE);

        Log.i(TAG, "Feature1: face box (left top right bottom)" + faceBoundingBox.flattenToString());
        Log.i(TAG, "Feature1: leftEyeLandmark " + String.format(Locale.US, "x: %f , y: %f", leftEyeLandmark.getPosition().x, leftEyeLandmark.getPosition().y));
        Log.i(TAG, "Feature1: rightEyeLandmark " + String.format(Locale.US, "x: %f , y: %f", rightEyeLandmark.getPosition().x, rightEyeLandmark.getPosition().y));
        Log.i(TAG, "Feature1: original picture" + String.format(Locale.US, "width: %d , height: %d", original.getWidth(), original.getHeight()));
        //initializing
        faceInImage = true;

        //checking of the bounding box is inside the picture
        if (faceBoundingBox.left < 0  ||  faceBoundingBox.right > original.getWidth()){
            faceInImage = false;
            Log.i(TAG, "Feature1: the faceBoundingBox is out of bounds on width");
            return;
        }
        if ( faceBoundingBox.top < 0  || faceBoundingBox.bottom > original.getHeight()){
            Log.i(TAG, "Feature1: the faceBoundingBox is out of bounds on height");
            faceInImage = false;
            return;
        }

        faceBoundingBoxSquared = new Rect();
        faceBoundingBoxSquared.left = faceBoundingBox.left;
        faceBoundingBoxSquared.top = faceBoundingBox.top;

        //make the face Bounding Box square
        if (faceBoundingBox.width() > faceBoundingBox.height() ){
            faceBoundingBoxSquared.right = faceBoundingBox.left + faceBoundingBox.width();
            faceBoundingBoxSquared.bottom = faceBoundingBox.top + faceBoundingBox.width();
        }else{
            faceBoundingBoxSquared.right = faceBoundingBox.left + faceBoundingBox.height();
            faceBoundingBoxSquared.bottom = faceBoundingBox.top + faceBoundingBox.height();
        }

        Log.i(TAG, "Feature1: squared face box (left top right bottom)" + faceBoundingBoxSquared.flattenToString());

        //checking of the bounding box is inside the picture
        if (faceBoundingBoxSquared.left < 0  || faceBoundingBoxSquared.right > original.getWidth()){
            faceInImage = false;
            Log.i(TAG, "Feature1: the squared faceBoundingBox is out of bounds on width");
            return;
        }
        if (faceBoundingBoxSquared.top<0 || faceBoundingBoxSquared.bottom > original.getHeight()){
            faceInImage = false;
            Log.i(TAG, "Feature1: the squared faceBoundingBox is out of bounds on height");
            return;
        }

        createBitmaps(3.5f);
    }

    // all the values are checked before hand so just the cropping
    private void createBitmaps(float face2EyeRatio){
        //the face
        Bitmap faceImageOriginal = Bitmap.createBitmap(original, faceBoundingBoxSquared.left, faceBoundingBoxSquared.top, faceBoundingBoxSquared.width(), faceBoundingBoxSquared.height());
        faceImage = Bitmap.createScaledBitmap(faceImageOriginal, 64, 64, true); //true-> uses bilinear filter
        //right eye
        PointF rightEyeCenter = rightEyeLandmark.getPosition();
        int rightEyeCenterX = ((int) rightEyeCenter.x);
        int rightEyeCenterY = ((int) rightEyeCenter.y);
        int rightEyeWidth =(int)(faceBoundingBoxSquared.width()/face2EyeRatio);
        int rightEyeHeight =(int) (faceBoundingBoxSquared.height()/face2EyeRatio);
        int rightEyeLeft = rightEyeCenterX-rightEyeWidth/2;
        int rightEyeTop = rightEyeCenterY-rightEyeHeight/2;
        int rightEyeRight = rightEyeLeft + rightEyeWidth;
        int rightEyeBottom = rightEyeTop + rightEyeHeight;

        Log.i(TAG, "createBitmaps: right eye (left top width height)" + rightEyeLeft + " " + rightEyeTop + " " +  rightEyeWidth + " " +  rightEyeHeight);
        if (rightEyeLeft < 0 || rightEyeRight >= original.getWidth()){
            faceInImage = false;
            Log.i(TAG, "Feature1: the right eye is out of bounds on width");
            return;
        }
        if (rightEyeTop < 0 || rightEyeBottom >= original.getHeight()){
            faceInImage = false;
            Log.i(TAG, "Feature1: the right eye is out of bounds on heights");
            return;
        }
        Bitmap leftEyeImageOriginal = Bitmap.createBitmap(original, rightEyeLeft, rightEyeTop, rightEyeWidth, rightEyeHeight);
        leftEyeImage = Bitmap.createScaledBitmap(leftEyeImageOriginal, 64, 64, true); //true-> uses bilinear filter

        //left eye
        PointF leftEyeCenter = leftEyeLandmark.getPosition();
        int leftEyeCenterX = ((int) leftEyeCenter.x);
        int leftEyeCenterY = ((int) leftEyeCenter.y);
        int leftEyeWidth = (int)(faceBoundingBoxSquared.width()/face2EyeRatio);
        int leftEyeHeight = (int)(faceBoundingBoxSquared.height()/face2EyeRatio);
        int leftEyeLeft = leftEyeCenterX-leftEyeWidth/2;
        int leftEyeTop = leftEyeCenterY-leftEyeHeight/2;
        int leftEyeRight = leftEyeLeft + leftEyeWidth;
        int leftEyeBottom = leftEyeTop + leftEyeHeight;

        Log.i(TAG, "createBitmaps: left eye (left top width height)" + leftEyeLeft + " " + leftEyeTop + " " +  leftEyeWidth + " " +  leftEyeHeight);
        if (leftEyeLeft < 0 || leftEyeRight >= original.getWidth()){
            faceInImage = false;
            Log.i(TAG, "Feature1: the left eye is out of bounds on width");
            return;
        }
        if (leftEyeTop < 0 || leftEyeBottom >= original.getHeight()){
            faceInImage = false;
            Log.i(TAG, "Feature1: the left eye is out of bounds on heights");
            return;
        }
        Bitmap rightEyeImageOriginal = Bitmap.createBitmap(original, leftEyeLeft, leftEyeTop, leftEyeWidth, leftEyeHeight);
        rightEyeImage = Bitmap.createScaledBitmap(rightEyeImageOriginal, 64, 64, true); //true-> uses bilinear filter

        // the grid
        faceGrid = new int[25][25];
        float width_step = (float) (original.getWidth()/25);
        float height_step = (float) (original.getHeight()/25);
        int grid_left = (int) (faceBoundingBoxSquared.left / width_step);
        int grid_right = (int) (faceBoundingBoxSquared.right / width_step);
        int grid_top = (int) (faceBoundingBoxSquared.top / height_step);
        int grid_bottom = (int) (faceBoundingBoxSquared.bottom / height_step);

        Log.i(TAG, "createBitmaps: grid left right top bottom " + grid_left + " " +grid_right + " " + grid_top + " " + grid_bottom);
        if(grid_bottom == 25) grid_bottom =24;
        if(grid_right == 25) grid_right = 24;

        for (int i = grid_top; i <= grid_bottom; i++ ){
            for (int j = grid_left; j <= grid_right; j++){
                faceGrid[i][j] = 1;
            }
        }
        logFaceGrid();
    }

    private void logFaceGrid(){
        int i;
        for ( i = 0; i < 25; i++ ){
            StringBuilder strbul = new StringBuilder();
            for(int a : faceGrid[i])
            {
                if (a == 1)
                    strbul.append('#');
                else
                    strbul.append('.');
                //for adding comma between elements
                strbul.append(",");
            }

            String str=strbul.toString();
            Log.i(TAG, "logFaceGrid: " + str);
            Log.i(TAG, "createBitmaps: " + i);
        }
        Log.i(TAG, "logFaceGrid: ---------------------------------------------------------------");
    }


    public Bitmap getFaceImage() {
        if (faceInImage)
            return faceImage;
        return null;
    }

    public Bitmap getLeftEyeImage() {
        if (faceInImage)
            return leftEyeImage;
        return null;
    }

    public Bitmap getRightEyeImage() {
        if (faceInImage)
            return rightEyeImage;
        return null;
    }

    public int[][] getFaceGrid() {
        if (faceInImage)
            return faceGrid;
        return null;
    }

    public boolean isFaceInImage() {
        return faceInImage;
    }
}
