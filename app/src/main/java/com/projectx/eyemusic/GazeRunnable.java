package com.projectx.eyemusic;


import android.app.Activity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.projectx.eyemusic.graphics.DotGraphic;
import com.projectx.eyemusic.graphics.GraphicOverlay;

public class GazeRunnable extends AppCompatActivity implements Runnable {
    private static final String TAG = "GazeRunnable";

    private GraphicOverlay graphicOverlayGazeLocation;
    private GazeModel model;
    private Activity activity;
    //Button btn = findViewById(R.id.btn_main_startGazeCaptureThread);

    GazeRunnable(GraphicOverlay overlayGaze, Activity activity){
        model = new GazeModel();
        graphicOverlayGazeLocation = overlayGaze;
    }

    @Override
    public void run() {
        int i = 0;
        while (true){
            try {
                GazeModel.GazePoint prediction = model.predict();
                graphicOverlayGazeLocation.add(new DotGraphic(activity, graphicOverlayGazeLocation, prediction.getX(), prediction.getY()));
                Thread.sleep(10);
                graphicOverlayGazeLocation.clear();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "run: " + i++);

        }
    }

}
