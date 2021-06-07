package com.projectx.eyemusic.Features;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.ColorInt;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;
import com.projectx.eyemusic.R;

import java.util.Random;


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

    public Feature1(){

    }


    // test for now with solid color bitmaps
    public Bitmap getFaceImage() {
        return createTestBitmap(64, 64, null);
    }

    public Bitmap getFaceGrid() {
        return null;
    }

    public Bitmap getEyeLeftImage() {
        return createTestBitmap(64, 64, null);
    }

    public Bitmap getEyeRightImage() {
        return createTestBitmap(64, 64,null);
    }

    // create bitmaps of solid color for testing inference
    private Bitmap createTestBitmap(int w, int h, @ColorInt Integer color){
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        if (color == null) {
            int[] colors = new int[] { Color.BLUE, Color.GREEN, Color.RED,
                    Color.YELLOW, Color.WHITE };
            Random rgen = new Random();
            color = colors[rgen.nextInt(colors.length - 1)];
        }

        canvas.drawColor(color);
        return bitmap;
    }
}

