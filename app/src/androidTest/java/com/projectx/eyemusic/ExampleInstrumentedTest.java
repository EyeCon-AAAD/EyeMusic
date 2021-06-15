package com.projectx.eyemusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.IntRange;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.projectx.eyemusic.Model.GazePoint;
import com.projectx.eyemusic.Model.OriginalModel;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private static final String TAG = "ExampleInstrumentedTest";
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.projectx.eyemusic", appContext.getPackageName());
    }

    @Test
    public void Test1() throws IOException {
        /*
        1st Sample in training_set:
        (x,y) = (3.289959 , 0.9122675)

        1st Sample in validation set:
        (x,y) = (1.9415725, -1.5549603)
        First sample in training set
        */
        GazePoint truePoint = new GazePoint(3.289959f , 0.9122675f);

        InputStream inFace = getClass().getClassLoader().getResourceAsStream("train0_face.png");
        Bitmap faceImage = BitmapFactory.decodeStream(inFace);

        InputStream inLeft = getClass().getClassLoader().getResourceAsStream("train0_left.png");
        Bitmap leftImage = BitmapFactory.decodeStream(inLeft);

        InputStream inRight = getClass().getClassLoader().getResourceAsStream("train0_right.png");
        Bitmap rightImage = BitmapFactory.decodeStream(inRight);

        InputStream inGrid = getClass().getClassLoader().getResourceAsStream("train0_mask.txt");
        Scanner s = new Scanner(inGrid).useDelimiter(",");
        int[][] faceGrid = new int[25][25];
        for (int i = 0; i< 25; i++){
            for (int j = 0; j<25; j++){
                int element = s.nextInt();
                faceGrid[i][j] = element;
            }
        }

        logFaceGrid(TAG, faceGrid);

        OriginalModel model;
        GazePoint predictedPoint = null;
        try {
            model = new OriginalModel();
            predictedPoint = model.Predict(faceImage, rightImage, leftImage, faceGrid);
            Log.d(TAG, "Test1: prediction result: (x y)" + predictedPoint.getX() + " " + predictedPoint.getY());
        }catch(Exception e){
            Log.d(TAG, "Test1: ", e);
        }

        Assert.assertEquals(truePoint.getX(), predictedPoint.getX(), 1f);
        Assert.assertEquals(truePoint.getY(), predictedPoint.getY(), 1f);

    }

    void logFaceGrid(String TAG, int[][] faceGrid){
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
            Log.i(TAG, "faceGrid: " + str);
            Log.i("nothing", "nothing: " + i);
        }
    }

}