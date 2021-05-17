package com.projectx.eyemusic;

import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;

import com.projectx.eyemusic.graphics.DotGraphic;
import com.projectx.eyemusic.graphics.GraphicOverlay;

import java.util.ArrayList;
import java.util.List;

public class CalibrationRunnable implements Runnable {
    private static final String TAG = "CalibrationRunnable";
    private GraphicOverlay graphicOverlayCalibration;
    private MainActivity activity;
    private static RawFeature newRawFeature; //contains the the frames and all the landmarks and other things needed
    private static boolean newFeatureCaptured;
    private RawFeature capturedFeature;
    private  List<Point> points;
    private volatile List<RawFeature> rawFeatures; //volatile -> so that two thread do not use the cashed value
    private final static int SCREEN_WIDTH = Utilities.getScreenWidth();
    private final static int SCREEN_HEIGHT = Utilities.getScreenHeight();


    CalibrationRunnable(GraphicOverlay overlayGaze, MainActivity activity){
        graphicOverlayCalibration= overlayGaze;
        this.activity =  activity;
        this.newFeatureCaptured = true; // meaning that the new feature has not come
        this.newRawFeature = null;
        this.points = new ArrayList<Point>();
        this.rawFeatures = new ArrayList<RawFeature>();

        produceDots(3, 6);
        printPoints();
    }

    @Override
    public void run() {
        int i = 1;
        int size_points = points.size();
        for(Point point : points){
            graphicOverlayCalibration.clear();
            DotGraphic dot = new DotGraphic(activity, graphicOverlayCalibration, point.x, point.y);
            dot.setColor(Color.BLUE);
            dot.setRadius(50f);
            graphicOverlayCalibration.add(dot);
            graphicOverlayCalibration.postInvalidate();

            //giving time to the user to look at the dot
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //capturing the feature
            while (newFeatureCaptured){
                Log.d("Calibration", "run: the new feature has not been come yet" );
            }
            while(newRawFeature == null){
                Log.d("Calibration", "run: the new feature is null" );
            }
            capturedFeature = newRawFeature;
            newFeatureCaptured = true;

            //TODO: save the rawFeatures somewhere
            capturedFeature.setXY_coordinates(point.x, point.y);
            rawFeatures.add(capturedFeature);
            Log.d("Calibration", "+++++run: the new feature is captured, run:" + i +"/" + size_points +" feature:"+ capturedFeature);
            i++;
        }
        graphicOverlayCalibration.clear();
        //calibration is finished
        Log.d("Calibration", "run: finished");
        activity.calibrationFinished();
    }

    private void produceDots(int no_x, int no_y){
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
                points.add(new Point((int) x, (int) y));
            }
        }

    }

    public List<RawFeature> getRawFeatures() {
        return rawFeatures;
    }

    public static boolean setNewFeature(RawFeature f){
        newRawFeature = f;
        newFeatureCaptured = false;
        Log.d("Calibration", "-----the new feature has arrived: ");
        return true;
    }

    public void printPoints(){
        for(Point point: points){
            Log.d("Calibration", "stored point: " + point);
        }
    }
}
