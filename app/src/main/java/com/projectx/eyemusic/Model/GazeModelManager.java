package com.projectx.eyemusic.Model;

import android.util.Log;

import com.google.android.gms.common.Feature;
import com.projectx.eyemusic.Features.Feature1;
import com.projectx.eyemusic.Features.RawFeature;
import com.projectx.eyemusic.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GazeModelManager {
    private static Object originalModel;
    private static CalibratedModel calibratedModel = new CalibratedModel();
    private static final String TAG = "GazeModelManager";
    private final static int SCREEN_WIDTH = Utilities.getScreenWidth();
    private final static int SCREEN_HEIGHT = Utilities.getScreenHeight();
    private static boolean isCalibratedAtAll = false;

    private static Random rand  = new Random();

    public static boolean haveCalibratedModel() {
        return isCalibratedAtAll;
    }

    // TODO: complete
    public static GazePoint predictOriginal(Feature1 feature){
        int x = rand.nextInt(SCREEN_WIDTH);
        int y = rand.nextInt(SCREEN_HEIGHT);
        return new GazePoint(x, y);
    }

    // TODO: complete
    public static GazePoint predictCalibrated(Feature1 feature){
        GazePoint originalPredict = predictOriginal(feature);
        if (isCalibratedAtAll)
            return calibratedModel.predict(originalPredict);
        else{
            Log.e(TAG, "predictCalibrated: there is no calibrated model");
            return null;
        }
    }

    // TODO: complete
    public static void updateCalibratedModel(List<Feature1> features){
        List <GazePoint> predictions = new ArrayList<GazePoint>();
        List <GazePoint> coordinates = new ArrayList<GazePoint>();
        for (Feature1 feature: features){
            predictions.add(predictOriginal(feature));
            coordinates.add(feature.getCoordinate());
        }

        calibratedModel = new CalibratedModel(predictions, coordinates);
    }

    public static CalibrationError getCalibrationTrainingError(){
        return calibratedModel.getTrainingError();
    }

}
