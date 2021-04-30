package com.projectx.eyemusic.graphics;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.graphics.GraphicOverlay;
import com.projectx.eyemusic.graphics.GraphicOverlay.Graphic;

public class DotGraphic extends Graphic{
    private static final String TAG = "DotGraphic";
    private  float radius = 20.0f;
    private  int color = Color.RED;

    private Paint paint;
    private float x;
    private float y;

    public DotGraphic(Activity activity, GraphicOverlay overlay, float x, float y){
        super(overlay);

        paint = new Paint();
        paint.setColor(color);


        // TODO: change the x based on the location of the overlay
        /*int[] location = new int[2];
        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {} // This is your code
        };
        mainHandler.post(myRunnable);
        location = ((MainActivity) activity).getGraphicOverlayGazeLocationLocation();

        this.x = x;
        this.y = y - location[1];
        Log.i(TAG, "DotGraphic:Location of overlay " + location[0] + " "+ location[1]);*/

        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, radius, paint);
    }


}
