package com.projectx.eyemusic;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.google.android.gms.common.Feature;
import com.projectx.eyemusic.Features.Feature1;
import com.projectx.eyemusic.Features.RawFeature;
import com.projectx.eyemusic.Graphics.DotGraphic;
import com.projectx.eyemusic.Graphics.GraphicOverlay;
import com.projectx.eyemusic.Model.GazeModelManager;
import com.projectx.eyemusic.Model.GazePoint;

public class PredictionThread extends HandlerThread {
    private static GraphicOverlay graphicOverlayGazeLocation;
    private static BaseActivity activity;
    private static Handler handler;

    public PredictionThread(GazeModelManager model, GraphicOverlay graphicOverlayGazeLocation, BaseActivity activity) {
        super("PredictionThread", Process.THREAD_PRIORITY_DEFAULT); // TODO: later check the priority
        PredictionThread.graphicOverlayGazeLocation = graphicOverlayGazeLocation;
        PredictionThread.activity = activity;
    }

    @Override
    protected void onLooperPrepared() {
        handler = new Handler();
    }

    public static Handler getHandler() {
        return handler;
    }

    public static class GazeRunnable implements Runnable {
        private static final String TAG = "GazeRunnable";
        private final Feature1 feature; //contains the the frames and all the landmarks and other things needed

        public GazeRunnable(Feature1 feature){
            this.feature = feature;
        }

        @Override
        public void run() {

            GazePoint prediction;
            if (GazeModelManager.haveCalibratedModel()){
                prediction = GazeModelManager.predictCalibrated(feature);
                Log.d(TAG, "Calibrated model coordinates: (" + prediction.getX() + ", " + prediction.getY() + ")");
                Log.d(TAG, "run: using calibrated model");
            }
            else{
                prediction = GazeModelManager.predictOriginal(feature);
                Log.d(TAG, "Original model coordinates: (" + prediction.getX() + ", " + prediction.getY() + ")");
                Log.d(TAG, "run: using original model");
            }
            Log.d(TAG, "SHOWN DOT: " + prediction.getX() + " " + prediction.getY());
            graphicOverlayGazeLocation.clear();
            graphicOverlayGazeLocation.add(new DotGraphic(activity, graphicOverlayGazeLocation, prediction.getX(), prediction.getY()));
            graphicOverlayGazeLocation.postInvalidate();

            if(feature.getSmileProb() > 0.8) {
                SimulatedTouch.click(500, 800); //TODO: replace the x and y by prediction.getX() and getY()
            }
        }
    }
    public static GraphicOverlay getGraphicOverlayGazeLocation() {
        return graphicOverlayGazeLocation;
    }
}
