package com.projectx.eyemusic;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import com.projectx.eyemusic.graphics.DotGraphic;
import com.projectx.eyemusic.graphics.GraphicOverlay;

public class GazeHandlerThread extends HandlerThread {
    private static GazeModel model;
    private static GraphicOverlay graphicOverlayGazeLocation;
    private static MainActivity activity;

    private Handler handler;
    public GazeHandlerThread(GazeModel model, GraphicOverlay graphicOverlayGazeLocation, MainActivity activity) {
        super("GazeHandlerThread", Process.THREAD_PRIORITY_DEFAULT); // TODO: later check the priority
        GazeHandlerThread.model = model;
        GazeHandlerThread.graphicOverlayGazeLocation = graphicOverlayGazeLocation;
        GazeHandlerThread.activity = activity;
    }

    @Override
    protected void onLooperPrepared() {
        handler = new Handler();
    }

    public Handler getHandler() {
        return handler;
    }



    public static class GazeRunnable implements Runnable {
        private static final String TAG = "GazeRunnable";
        private final Feature feature; //contains the the frames and all the landmarks and other things needed

        public GazeRunnable(Feature feature){
            this.feature = feature;
        }

        @Override
        public void run() {
            graphicOverlayGazeLocation.clear();
            GazeModel.GazePoint prediction = model.predict(feature);
            graphicOverlayGazeLocation.add(new DotGraphic(activity, graphicOverlayGazeLocation, prediction.getX(), prediction.getY()));
            graphicOverlayGazeLocation.postInvalidate();
            if(feature.smileProbability > 0.8)
                SimulatedTouch.click(500, 800); //TODO: replace teh x and y by prediction.getX() and getY()
        }

    }

    public static GraphicOverlay getGraphicOverlayGazeLocation() {
        return graphicOverlayGazeLocation;
    }
}
