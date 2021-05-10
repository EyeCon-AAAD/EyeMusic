package com.projectx.eyemusic;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.projectx.eyemusic.graphics.DotGraphic;

// in case needed but so far it is just a Handler class with no special fictionalises for executing the messages
public class GazeHandler extends Handler {
    private static final String TAG = "GazeHandler";
    public static final int PREDICT_TASK = 1;

    @Override
    public void handleMessage(@NonNull Message msg) {
        /*switch (msg.what){
            case PREDICT_TASK:

                graphicOverlayGazeLocation.clear();
                GazeModel.GazePoint prediction = model.predict(currentFeature);
                graphicOverlayGazeLocation.add(new DotGraphic(activity, graphicOverlayGazeLocation, prediction.getX(), prediction.getY()));
                graphicOverlayGazeLocation.postInvalidate();

                if(currentFeature.smileProbability > 0.8)
                    SimulatedTouch.click(500, 800); //TODO: replace teh x and y by predictio.getX() and getY()

                Log.d(TAG, "run: " + msg.arg1);
        }*/
    }
}
