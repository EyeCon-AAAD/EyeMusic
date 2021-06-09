package com.projectx.eyemusic.Features;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;

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
            Log.e(TAG, "Feature1: the faceBoundingBox is out of bounds on width");
            return;
        }
        if ( faceBoundingBox.top < 0  || faceBoundingBox.bottom > original.getHeight()){
            Log.e(TAG, "Feature1: the faceBoundingBox is out of bounds on height");
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
            Log.e(TAG, "Feature1: the squared faceBoundingBox is out of bounds on width");
            return;
        }
        if (faceBoundingBoxSquared.top<0 || faceBoundingBoxSquared.bottom > original.getHeight()){
            faceInImage = false;
            Log.e(TAG, "Feature1: the squared faceBoundingBox is out of bounds on height");
            return;
        }

        createBitmaps(4);
    }

    // all the values are checked before hand so just the cropping
    private void createBitmaps(float face2EyeRatio){
        //the face
        faceImage = Bitmap.createBitmap(original, faceBoundingBoxSquared.left, faceBoundingBoxSquared.top, faceBoundingBoxSquared.width(), faceBoundingBoxSquared.height());

        //right eye
        PointF rightEyeCenter = rightEyeLandmark.getPosition();
        int rightEyeCenterX = ((int) rightEyeCenter.x);
        int rightEyeCenterY = ((int) rightEyeCenter.y);
        int rightEyeWidth =(int)(faceBoundingBoxSquared.width()/face2EyeRatio);
        int rightEyeHeight =(int) (faceBoundingBoxSquared.height()/face2EyeRatio);
        int rightEyeLeft = rightEyeCenterX-rightEyeWidth/2;
        int rightEyeTop = rightEyeCenterY-rightEyeHeight/2;

        Log.i(TAG, "createBitmaps: left eye (left top width height)" + rightEyeLeft + " " + rightEyeTop + " " +  rightEyeWidth + " " +  rightEyeHeight);
        leftEyeImage = Bitmap.createBitmap(original, rightEyeLeft, rightEyeTop, rightEyeWidth, rightEyeHeight);

        //TODO: right left may get confused in the model
        //left eye
        PointF leftEyeCenter = leftEyeLandmark.getPosition();
        int leftEyeCenterX = ((int) leftEyeCenter.x);
        int leftEyeCenterY = ((int) leftEyeCenter.y);
        int leftEyeWidth = (int)(faceBoundingBoxSquared.width()/face2EyeRatio);
        int leftEyeHeight = (int)(faceBoundingBoxSquared.height()/face2EyeRatio);
        int leftEyeLeft = leftEyeCenterX-leftEyeWidth/2;
        int leftEyeTop = leftEyeCenterY-leftEyeHeight/2;

        Log.i(TAG, "createBitmaps: right eye (left top width height)" + leftEyeLeft + " " + leftEyeTop + " " +  leftEyeWidth + " " +  leftEyeHeight);

        rightEyeImage = Bitmap.createBitmap(original, leftEyeLeft, leftEyeTop, leftEyeWidth, leftEyeHeight);


        // the grid
        faceGrid = new int[25][25];
        float width_step = (float) (original.getWidth()/25);
        float height_step = (float) (original.getHeight()/25);
        int grid_left = (int) (faceBoundingBoxSquared.left / width_step);
        int grid_right = (int) (faceBoundingBoxSquared.right / width_step);
        int grid_top = (int) (faceBoundingBoxSquared.top / height_step);
        int grid_bottom = (int) (faceBoundingBoxSquared.bottom / height_step);

        for (int i = grid_left; i <= grid_right; i++ ){
            for (int j = grid_top; j <= grid_bottom; j++){
                if(i == 25) i =24;
                if(j == 25) j =24;
                faceGrid[i][j] = 1;
            }
        }


        int i;
        for ( i = 0; i < 25; i++ ){
            StringBuilder strbul = new StringBuilder();
            for(int a : faceGrid[i])
            {
                strbul.append(a);
                //for adding comma between elements
                strbul.append(",");
            }

            String str=strbul.toString();
            Log.i(TAG, "createBitmaps: " + str);
            Log.i(TAG, "createBitmaps: " + i);
        }
        Log.i(TAG, "createBitmaps: " + i);


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
