package com.projectx.eyemusic;

import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.Feature;
import com.projectx.eyemusic.Features.Feature1;
import com.projectx.eyemusic.Features.RawFeature;
import com.projectx.eyemusic.Graphics.DotGraphic;
import com.projectx.eyemusic.Graphics.GraphicOverlay;
import com.projectx.eyemusic.Model.GazeModelManager;
import com.projectx.eyemusic.Model.GazePoint;

import org.checkerframework.checker.units.qual.min;

public class PredictionThread extends HandlerThread {
    private static GraphicOverlay graphicOverlayGazeLocation;
    private static BaseActivity activity;
    private static Handler handler;

    private final static Integer SCREEN_WIDTH = Utilities.getScreenWidth();
    private final static Integer SCREEN_HEIGHT = Utilities.getScreenHeight();

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

                float shown_X = LimitToScreen(prediction.getX(), 0f, Float.valueOf(SCREEN_WIDTH));
                float shown_Y = LimitToScreen(prediction.getY(), 0f, Float.valueOf(SCREEN_HEIGHT));

                Log.d(TAG, "SHOWN DOT: " + shown_X + " " + shown_Y);

                //remap the gaze location
                float remap_X = shown_X;
                float remap_Y = shown_Y;
                Boolean remaped = Boolean.FALSE;
                RemapGaze remaper = new RemapGaze();
                if(remaper.needRemap()){
                    GazePoint remaped_point = remaper.remap(new GazePoint(shown_X, shown_Y));
                    if(remaped_point != null){
                        Log.d("remap", "run: remaped_point not null");
                        remap_X = remaped_point.getX();
                        remap_Y = remaped_point.getY();
                        remaped = Boolean.TRUE;
                    }

                }

                //showing
                graphicOverlayGazeLocation.clear();

                DotGraphic dot = new DotGraphic(activity, graphicOverlayGazeLocation, shown_X, shown_Y);
                graphicOverlayGazeLocation.add(dot);

                if(remaped){
                    DotGraphic remaped_dot = new DotGraphic(activity, graphicOverlayGazeLocation, remap_X, remap_Y);
                    remaped_dot.setColor(Color.GREEN);
                    if(feature.getSmileProb() > 0.8) remaped_dot.setColor(Color.parseColor("#FF013220")); //dark green
                    graphicOverlayGazeLocation.add(remaped_dot);
                }

                if(feature.getSmileProb() > 0.8) dot.setColor(Color.parseColor("#FF800000")); //dark red

                graphicOverlayGazeLocation.postInvalidate();

                //clicking
                if(feature.getSmileProb() > 0.8) {
                    try{
                        SimulatedTouch.click(remap_X, remap_Y);
                        sleep(1700);
                    }catch(Exception e){
                        Log.e(TAG, "on click: ", e);
                        e.printStackTrace();
                    }

                }
            }
            else{
                Toast.makeText(App.getContext(), "There is no Calibrated Model", Toast.LENGTH_SHORT).show();
                prediction = GazeModelManager.predictOriginal(feature);
                Log.d(TAG, "Original model coordinates: (" + prediction.getX() + ", " + prediction.getY() + ")");
                Log.d(TAG, "run: using original model");
            }

        }

        private Float LimitToScreen(Float coordinate, Float min, Float max){
            if (coordinate < min) return min;
            if (coordinate > max) return max;
            return coordinate;
        }
    }
    public static GraphicOverlay getGraphicOverlayGazeLocation() {
        return graphicOverlayGazeLocation;
    }


}
