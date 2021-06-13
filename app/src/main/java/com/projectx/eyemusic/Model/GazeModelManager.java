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
    private static final OriginalModel gazePredictionModel = OriginalModel.getInstance();;
    private static CalibratedModel calibratedModel = null;
    private static final String TAG = "GazeModelManager";
    private final static int SCREEN_WIDTH = Utilities.getScreenWidth();
    private final static int SCREEN_HEIGHT = Utilities.getScreenHeight();
    private static boolean isCalibratedAtAll = false;

    private static Random rand  = new Random();

    public static boolean haveCalibratedModel() {
        return isCalibratedAtAll;
    }

    public static GazePoint predictOriginal(Feature1 feature){
        try{
            GazePoint p = gazePredictionModel.Predict(feature);
            Log.d(TAG, "predictOriginal: " + p.getX() + " " + p.getY());
            Log.d(TAG, "Screen (w x h) : (" + SCREEN_WIDTH + ", " + SCREEN_HEIGHT + ")");
            return p;

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static GazePoint predictCalibrated(Feature1 feature){
        GazePoint originalPredict = predictOriginal(feature);
        Log.d(TAG, "predictCalibrated: original " + originalPredict.getX() + " " + originalPredict.getY());
        if (isCalibratedAtAll){
            GazePoint calibPredict = calibratedModel.predict(originalPredict);
            Log.d(TAG, "predictCalibrated: calibrated " + calibPredict.getX() + " " + calibPredict.getY());
            return calibPredict;
        }

        else{
            Log.e(TAG, "predictCalibrated: there is no calibrated model");
            return new GazePoint(500,500);
        }
    }

    public static void updateCalibratedModel(List<Feature1> features){
        List <GazePoint> predictions = new ArrayList<GazePoint>();
        List <GazePoint> coordinates = new ArrayList<GazePoint>();
        for (Feature1 feature: features){
            predictions.add(predictOriginal(feature));
            coordinates.add(feature.getCoordinate());
        }

        CalibratedModel newCalibratedModel = new CalibratedModel(predictions, coordinates);
        if (newCalibratedModel.isTrained()) {
            isCalibratedAtAll = true;
            calibratedModel = newCalibratedModel;
            Log.d(TAG, "updateCalibratedModel: calibrated model is updated successfully.");
        }else{
            Log.d(TAG, "updateCalibratedModel: calibrated model is not updated successfully.");
        }


        Log.d(TAG, "updateCalibratedModel: finished");
    }

    public static CalibrationError getCalibrationTrainingError(){
        if (isCalibratedAtAll)
            return calibratedModel.getTrainingError();
        else return null;
    }
}
