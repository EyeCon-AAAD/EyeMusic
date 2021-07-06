package com.projectx.eyemusic;

import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.Feature;
import com.projectx.eyemusic.Features.Feature1;
import com.projectx.eyemusic.Features.FeatureExtractor;
import com.projectx.eyemusic.Features.RawFeature;
import com.projectx.eyemusic.Graphics.DotGraphic;
import com.projectx.eyemusic.Graphics.GraphicOverlay;
import com.projectx.eyemusic.Model.CalibrationError;
import com.projectx.eyemusic.Model.GazeModelManager;
import com.projectx.eyemusic.Model.GazePoint;

import java.util.ArrayList;
import java.util.List;

public class CalibrationRunnable implements Runnable {
    private static final String TAG = "CalibrationRunnable";
    private GraphicOverlay graphicOverlayCalibration;
    private CalibrationActivity activity;

    private static boolean newFeatureCaptured;
    private static Feature1 newFeature; //contains the the frames and all the landmarks and other things needed
    private Feature1 capturedFeature;

    private  List<GazePoint> points;
    private volatile List<Feature1> features; //volatile -> so that two thread do not use the cashed value
    private final static int SCREEN_WIDTH = Utilities.getScreenWidth();
    private final static int SCREEN_HEIGHT = Utilities.getScreenHeight();

    CalibrationRunnable(GraphicOverlay overlayGaze, CalibrationActivity activity){
        graphicOverlayCalibration= overlayGaze;
        this.activity =  activity;
        this.newFeatureCaptured = true; // meaning that the new feature has not come
        this.newFeature = null;
        this.features = new ArrayList<Feature1>();

        this.points = produceDots(3, 6);
        printPoints();
    }

    @Override
    public void run() {
        int i = 1;
        int size_points = points.size();
        for(GazePoint point : points){
            graphicOverlayCalibration.clear();
            DotGraphic dot = new DotGraphic(activity, graphicOverlayCalibration, point.getX(), point.getY());
            dot.setColor(Color.BLUE);
            dot.setRadius(50f);
            graphicOverlayCalibration.add(dot);
            graphicOverlayCalibration.postInvalidate();

            //giving time to the user to look at the dot
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //capturing the feature
            while (newFeatureCaptured){ //goes through here when its true meaning the feature is used so wants for feature
                Log.d(TAG, "CalibrationNewFeature: the new feature has not been come yet (in the while loop)" );
            }
            while(newFeature == null){
                Log.d(TAG, "CalibrationNewFeature: the new feature is null (in the while loop)" );
            }

            capturedFeature = newFeature;
            setNewFeatureCaptured(true);

            capturedFeature.setCoordinate(point);
            features.add(capturedFeature);
            Log.d(TAG, "CalibrationNewFeature: the new feature is added, run:" + i +"/" + size_points +" feature:"+ capturedFeature);
            i++;
        }

        //finishing the calibration
        graphicOverlayCalibration.clear();

        try{
            //TODO: show the message that the model is being calibrated
            //updating the model
            GazeModelManager.updateCalibratedModel(features);

            // no need for the features captured so far
            features.clear();

            //checking if the calibrated model is trained or not
            boolean success = GazeModelManager.getRecentCalibrationSuccess();
            if (success){
                //TODO: show the message
                Log.d(TAG, "CalibrationResult: the model is updated");
                //Show the training error
                CalibrationError calibError = GazeModelManager.getCalibrationTrainingError();
                //TODO: show the message
                Log.d(TAG, "CalibrationResult: calibration training error (X Y XY): "
                        + calibError.getX_error() + " " + calibError.getY_error() + " " + calibError.getXY_error());

                //TODO: show the message that they have to look at the screen
                //waiting for the person to look
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                int dotColors[] = new int[]{Color.RED, Color.YELLOW, Color.GREEN , Color.BLUE, Color.BLACK};

                for(int count = 0; count<5; count++){
                    // show the dot in the screen for showing the calibration test error
                    graphicOverlayCalibration.clear();
                    DotGraphic dot = new DotGraphic(activity, graphicOverlayCalibration, SCREEN_WIDTH/2, SCREEN_HEIGHT/2);
                    dot.setColor(dotColors[count]);
                    dot.setRadius(50f);
                    graphicOverlayCalibration.add(dot);
                    graphicOverlayCalibration.postInvalidate();

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //capturing the feature
                    while (newFeatureCaptured){ //goes through here when its true meaning the feature is used so wants for feature
                        Log.d(TAG, "CalibrationNewFeature: the new feature has not been come yet (in the while loop)" );
                    }
                    while(newFeature == null){
                        Log.d(TAG, "CalibrationNewFeature: the new feature is null (in the while loop)" );
                    }

                    //features are collected
                    capturedFeature = newFeature;
                    setNewFeatureCaptured(true);
                    capturedFeature.setCoordinate(new GazePoint(SCREEN_WIDTH/2, SCREEN_HEIGHT/2));
                    features.add(capturedFeature);
                    Log.d(TAG, "CalibrationNewFeature: the new feature is added, run:" + i +"/" + size_points +" feature:"+ capturedFeature);

                }
                List<GazePoint> calibPredictions = new ArrayList<GazePoint>();
                List <GazePoint> coordinates = new ArrayList<GazePoint>();
                for (Feature1 feature: features){
                    calibPredictions.add(GazeModelManager.predictCalibrated(feature));
                    coordinates.add(feature.getCoordinate());
                }

                //TODO: show the test error
                CalibrationError testError = new CalibrationError(coordinates, calibPredictions);
                Log.d(TAG, "CalibrationResult: calibration test error (X Y XY): "
                        + testError.getX_error() + " " + testError.getY_error() + " " + testError.getXY_error());

            }else {
                Log.d(TAG, "CalibrationResult: the model could not be updated");
            }
        }catch (Exception e){
            Log.e(TAG, "CalibrationResult: ", e);
        }

        //finishing the calibration
        graphicOverlayCalibration.clear();
        FeatureExtractor.setCalibrationMode(false);
        activity.setCalibration(false);
        activity.calibrationFinished();
        Log.d(TAG, "Calibration: finished");
    }

    private static List<GazePoint> produceDots(int no_x, int no_y){
        List<GazePoint> points = new ArrayList<GazePoint>();
        double margin_x = SCREEN_WIDTH * 0.05;
        double margin_y = SCREEN_HEIGHT * 0.05;

        double start_x = margin_x;
        double start_y = margin_y;

        double step_x = (SCREEN_WIDTH - (margin_x*2)) /  (no_x - 1);
        double step_y = (SCREEN_HEIGHT - (margin_y*2)) / (no_y - 1);

        double x, y;
        for (int i = 0; i < no_x; i++) {
            for (int j = 0; j < no_y; j++) {
                x = start_x + step_x * i;
                y = start_y + step_y * j;
                points.add(new GazePoint((float) x,(float) y));
            }
        }
        return points;
    }

    public static boolean setNewFeature(Feature1 f){
        newFeature = f;
        setNewFeatureCaptured(false);
        Log.d("CalibrationNewFeature", "-----the new feature has arrived: ");
        return true;
    }

    private static synchronized void setNewFeatureCaptured(boolean value){
        newFeatureCaptured = value;
    }

    public void printPoints(){
        for(GazePoint point: points){
            Log.d("Calibration", "stored point: " + point.getX() + " " + point.getY());
        }
    }

}